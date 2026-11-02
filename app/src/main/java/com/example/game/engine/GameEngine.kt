package com.example.game.engine

import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.CombatSnapshot
import com.example.game.combat.EnemyType
import com.example.game.combat.TacticalSkillType
import com.example.game.combat.WavePhase
import com.example.game.economy.EconomyCalculator
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class EngineStepEvents(
    val goldEarnedDelta: Double = 0.0,
    val infantryKilledDelta: Int = 0,
    val tanksKilledDelta: Int = 0,
    val aircraftKilledDelta: Int = 0,
    val bossesKilledDelta: Int = 0,
    val waveCompletedNumber: Int? = null,
    val shouldPersistSave: Boolean = false
)

/**
 * Pure, testable simulation engine for IRON FRONT.
 * Coordinates the battle loop, tactical commands, and state machine.
 */
class GameEngine(
    private val random: Random = Random(42)
) {
    private var nextEntityId = 1L
    private var spawnQueue = ArrayDeque<EnemyType>()
    private var spawnCooldownSec = 0f
    private var barracksSpawnTimer = 0f
    private var turretFireTimer = 0f
    private var rocketSiloTimer = 0f
    private var airFleetSortieTimer = 0f

    var snapshot: CombatSnapshot = CombatSnapshot()
        private set

    private fun nextId(): Long = nextEntityId++

    fun initializeForWave(save: PlayerSaveEntity, startPhase: WavePhase = WavePhase.PREPARING) {
        val maxBaseHp = CombatSimulationHelper.calculateMaxBaseHp(save)
        val maxShieldHp = CombatSimulationHelper.calculateMaxShieldHp(save)
        val maxCharges = 2 + (save.permSupportSkillLv / 2)
        val queue = CombatSimulationHelper.buildWaveSpawnQueue(save.currentWave)
        spawnQueue.clear()
        spawnQueue.addAll(queue)

        val initialAllies = CombatSimulationHelper.buildInitialAlliedSquad(save) { nextId() }

        snapshot = snapshot.copy(
            waveNumber = save.currentWave,
            wavePhase = startPhase,
            prepareCountdownSec = if (startPhase == WavePhase.PREPARING) 5.0f else 0f,
            baseMaxHp = maxBaseHp,
            baseCurrentHp = maxBaseHp,
            shieldMaxHp = maxShieldHp,
            shieldCurrentHp = maxShieldHp,
            enemiesRemainingToSpawn = spawnQueue.size,
            totalWaveEnemies = max(1, spawnQueue.size),
            enemiesDefeatedInWave = 0,
            enemies = emptyList(),
            alliedSoldiers = initialAllies,
            projectiles = emptyList(),
            airSorties = emptyList(),
            explosions = emptyList(),
            smokeTrail = emptyList(),
            rewardOptions = emptyList(),
            maxAirstrikeCharges = maxCharges,
            airstrikeCharges = max(snapshot.airstrikeCharges, maxCharges),
            lastBannerMessage = "DALGA ${save.currentWave} HAZIRLANIYOR"
        )
    }

    fun syncStatsFromSave(save: PlayerSaveEntity) {
        val newMaxHp = CombatSimulationHelper.calculateMaxBaseHp(save)
        val newMaxShield = CombatSimulationHelper.calculateMaxShieldHp(save)
        val hpRatio = if (snapshot.baseMaxHp > 0) (snapshot.baseCurrentHp / snapshot.baseMaxHp) else 1.0
        val shieldRatio = if (snapshot.shieldMaxHp > 0) (snapshot.shieldCurrentHp / snapshot.shieldMaxHp) else 1.0
        val maxCharges = 2 + (save.permSupportSkillLv / 2)

        snapshot = snapshot.copy(
            baseMaxHp = newMaxHp,
            baseCurrentHp = (newMaxHp * hpRatio).coerceIn(1.0, newMaxHp),
            shieldMaxHp = newMaxShield,
            shieldCurrentHp = (newMaxShield * shieldRatio).coerceIn(0.0, newMaxShield),
            maxAirstrikeCharges = maxCharges
        )
    }

    fun selectTargetingSkill(skill: TacticalSkillType?) {
        if (skill == null) {
            snapshot = snapshot.copy(activeTargetingSkill = null)
            return
        }
        when (skill) {
            TacticalSkillType.AIRSTRIKE -> {
                if (snapshot.airstrikeCharges > 0 || snapshot.airstrikeCooldownRemaining <= 0f) {
                    snapshot = snapshot.copy(
                        activeTargetingSkill = if (snapshot.activeTargetingSkill == skill) null else skill,
                        lastBannerMessage = "HEDEF BÖLGEYE DOKUNUN: HAVA BOMBARDIMANI"
                    )
                }
            }
            TacticalSkillType.ARTILLERY_BARRAGE -> {
                if (snapshot.artilleryCooldownRemaining <= 0f) {
                    snapshot = snapshot.copy(
                        activeTargetingSkill = if (snapshot.activeTargetingSkill == skill) null else skill,
                        lastBannerMessage = "HEDEF BÖLGEYE DOKUNUN: TOPÇU ATEŞİ"
                    )
                }
            }
            TacticalSkillType.SHIELD_OVERCHARGE -> {
                triggerShieldOvercharge(permSkillLv = 0)
            }
        }
    }

    fun triggerShieldOvercharge(permSkillLv: Int) {
        snapshot = CombatTacticalSkillsHandler.triggerShieldOvercharge(
            snapshot = snapshot,
            permSkillLv = permSkillLv,
            nextIdProvider = { nextId() }
        )
    }

    fun executeBattlefieldTap(
        normX: Float,
        normY: Float,
        save: PlayerSaveEntity
    ): Boolean {
        val (updatedSnap, executed) = CombatTacticalSkillsHandler.executeBattlefieldTap(
            snapshot = snapshot,
            normX = normX,
            normY = normY,
            save = save,
            nextIdProvider = { nextId() }
        )
        snapshot = updatedSnap
        return executed
    }

    fun step(
        dtSec: Float,
        save: PlayerSaveEntity,
        nowWallTimeMs: Long = System.currentTimeMillis()
    ): EngineStepEvents {
        val clampedDt = dtSec.coerceIn(0.0f, 0.15f)
        if (clampedDt <= 0f) return EngineStepEvents()

        val passiveRate = EconomyCalculator.calculateTotalPassiveGoldPerSec(save, nowWallTimeMs)
        var goldDelta = passiveRate * clampedDt.toDouble()
        var waveCompleted: Int? = null
        var shouldPersist = false

        val newRadarAngle = (snapshot.radarAngleDeg + clampedDt * 95f) % 360f
        var airCooldown = max(0f, snapshot.airstrikeCooldownRemaining - clampedDt)
        var airCharges = snapshot.airstrikeCharges
        if (airCooldown <= 0f && airCharges < snapshot.maxAirstrikeCharges) {
            airCharges += 1
            if (airCharges < snapshot.maxAirstrikeCharges) {
                airCooldown = max(6f, TacticalSkillType.AIRSTRIKE.baseCooldownSec - save.permSupportSkillLv * 1.1f)
            }
        }
        val artCooldown = max(0f, snapshot.artilleryCooldownRemaining - clampedDt)
        val shieldCooldown = max(0f, snapshot.shieldBoostCooldownRemaining - clampedDt)

        val updatedExplosions = snapshot.explosions.mapNotNull { exp ->
            val nextP = exp.progress + clampedDt * 2.2f
            if (nextP >= 1f) null else exp.copy(progress = nextP)
        }.toMutableList()

        val updatedSmoke = snapshot.smokeTrail.mapNotNull { smk ->
            val nextAlpha = smk.alpha - clampedDt * 1.4f
            if (nextAlpha <= 0.03f) null else smk.copy(alpha = nextAlpha, size = smk.size + clampedDt * 5f)
        }.toMutableList()

        val updatedFloatingTexts = snapshot.floatingTexts.mapNotNull { ft ->
            val nextP = ft.progress + clampedDt * 1.25f
            if (nextP >= 1f) null else ft.copy(progress = nextP, y = ft.y - clampedDt * 0.05f)
        }.toMutableList()

        val shieldRegenPerSec = (snapshot.shieldMaxHp * 0.025) * (1.0 + save.wallShieldCapacityLv * 0.08)
        var currentShield = min(snapshot.shieldMaxHp, snapshot.shieldCurrentHp + shieldRegenPerSec * clampedDt)
        var currentBaseHp = snapshot.baseCurrentHp

        // Phase transitions
        when (snapshot.wavePhase) {
            WavePhase.PREPARING -> {
                val nextCountdown = snapshot.prepareCountdownSec - clampedDt
                if (nextCountdown <= 0f) {
                    snapshot = snapshot.copy(
                        wavePhase = WavePhase.FIGHTING,
                        prepareCountdownSec = 0f,
                        radarAngleDeg = newRadarAngle,
                        airstrikeCooldownRemaining = airCooldown,
                        airstrikeCharges = airCharges,
                        artilleryCooldownRemaining = artCooldown,
                        shieldBoostCooldownRemaining = shieldCooldown,
                        explosions = updatedExplosions,
                        smokeTrail = updatedSmoke,
                        floatingTexts = updatedFloatingTexts,
                        lastBannerMessage = if (snapshot.waveNumber % 5 == 0) {
                            "DİKKAT: DALGA ${snapshot.waveNumber} BOSS SALDIRISI!"
                        } else {
                            "DALGA ${snapshot.waveNumber} BAŞLADI — CEPHEYİ SAVUN!"
                        }
                    )
                } else {
                    snapshot = snapshot.copy(
                        prepareCountdownSec = nextCountdown,
                        radarAngleDeg = newRadarAngle,
                        airstrikeCooldownRemaining = airCooldown,
                        airstrikeCharges = airCharges,
                        artilleryCooldownRemaining = artCooldown,
                        shieldBoostCooldownRemaining = shieldCooldown,
                        shieldCurrentHp = currentShield,
                        explosions = updatedExplosions,
                        smokeTrail = updatedSmoke,
                        floatingTexts = updatedFloatingTexts
                    )
                }
                return EngineStepEvents(goldEarnedDelta = goldDelta)
            }

            WavePhase.REWARD_SELECTION -> {
                snapshot = snapshot.copy(
                    radarAngleDeg = newRadarAngle,
                    airstrikeCooldownRemaining = airCooldown,
                    airstrikeCharges = airCharges,
                    artilleryCooldownRemaining = artCooldown,
                    shieldBoostCooldownRemaining = shieldCooldown,
                    explosions = updatedExplosions,
                    smokeTrail = updatedSmoke,
                    floatingTexts = updatedFloatingTexts
                )
                return EngineStepEvents(goldEarnedDelta = goldDelta)
            }

            WavePhase.COMPLETED -> {
                initializeForWave(save, WavePhase.PREPARING)
                return EngineStepEvents(goldEarnedDelta = goldDelta)
            }

            WavePhase.FIGHTING -> {
                // Handled below
            }
        }

        // --- FIGHTING PHASE SIMULATION ---
        val enemies = snapshot.enemies.toMutableList()
        val allies = snapshot.alliedSoldiers.toMutableList()
        val projectiles = snapshot.projectiles.toMutableList()
        var turretFlash = max(0f, snapshot.turretMuzzleFlashTimer - clampedDt)
        var siloFlash = max(0f, snapshot.siloLaunchFlashTimer - clampedDt)

        // 1. Spawn enemies from queue
        spawnCooldownSec -= clampedDt
        if (spawnQueue.isNotEmpty() && spawnCooldownSec <= 0f) {
            val nextType = spawnQueue.removeFirst()
            val spawned = CombatSimulationHelper.createEnemyUnit(nextId(), nextType, snapshot.waveNumber, random)
            enemies.add(spawned)
            spawnCooldownSec = when (nextType) {
                EnemyType.INFANTRY -> 0.85f
                EnemyType.ARMORED_TANK -> 1.55f
                EnemyType.STEALTH_JET -> 1.30f
                EnemyType.MEGA_BOSS_MECH -> 2.20f
            }
        }

        // 2. Spawn allied soldiers from Barracks
        val maxAlliedUnits = min(10, 2 + save.barracksMaxUnitsLv + (save.barracksLevel / 3))
        val spawnInterval = max(1.0f, 3.8f - save.barracksSpawnRateLv * 0.22f)
        barracksSpawnTimer += clampedDt
        if (allies.size < maxAlliedUnits && barracksSpawnTimer >= spawnInterval) {
            barracksSpawnTimer = 0f
            allies.add(CombatSimulationHelper.createAlliedSoldier(save, allies.size, nextId()))
        }

        val globalDmgMult = CombatSimulationHelper.calculateGlobalDamageMultiplier(save, nowWallTimeMs)

        // 3. Allied Soldiers combat
        val updatedAllies = CombatSystemsSimulation.updateAlliedSoldiers(
            allies = allies,
            enemies = enemies,
            projectiles = projectiles,
            clampedDt = clampedDt,
            globalDmgMult = globalDmgMult,
            save = save,
            nextIdProvider = { nextId() }
        )

        // 4. Automated Border Wall Turrets
        val (newTurretTimer, flashT) = CombatDefensesSimulation.updateTurrets(
            currentTimer = turretFireTimer,
            clampedDt = clampedDt,
            enemies = enemies,
            save = save,
            globalDmgMult = globalDmgMult,
            projectiles = projectiles,
            nextIdProvider = { nextId() }
        )
        turretFireTimer = newTurretTimer
        if (flashT > 0f) turretFlash = flashT

        // 5. Automated Rocket Silo
        val (newSiloTimer, flashS) = CombatDefensesSimulation.updateRocketSilo(
            currentTimer = rocketSiloTimer,
            clampedDt = clampedDt,
            enemies = enemies,
            save = save,
            globalDmgMult = globalDmgMult,
            projectiles = projectiles,
            nextIdProvider = { nextId() }
        )
        rocketSiloTimer = newSiloTimer
        if (flashS > 0f) siloFlash = flashS

        // 6 & 7. Air Fleet Hangar Sorties
        val airSortieResult = CombatDefensesSimulation.updateAirSortiesAndBombs(
            currentTimer = airFleetSortieTimer,
            sorties = snapshot.airSorties,
            enemies = enemies,
            clampedDt = clampedDt,
            save = save,
            globalDmgMult = globalDmgMult,
            random = random,
            projectiles = projectiles,
            nextIdProvider = { nextId() }
        )
        airFleetSortieTimer = airSortieResult.updatedTimer

        // 8. Projectiles movement and impacts
        val projResult = CombatSystemsSimulation.updateProjectilesAndImpacts(
            projectiles = projectiles,
            enemies = enemies,
            updatedExplosions = updatedExplosions,
            updatedSmoke = updatedSmoke,
            updatedFloatingTexts = updatedFloatingTexts,
            clampedDt = clampedDt,
            save = save,
            initialBaseHp = currentBaseHp,
            initialShieldHp = currentShield,
            nextIdProvider = { nextId() }
        )
        currentBaseHp = projResult.updatedBaseHp
        currentShield = projResult.updatedShieldHp

        // 9. Update enemies movement, attacks, deaths
        val enemyResult = CombatSystemsSimulation.updateEnemiesAndCombat(
            enemies = enemies,
            projectiles = projResult.survivingProjectiles.toMutableList(),
            updatedFloatingTexts = updatedFloatingTexts,
            clampedDt = clampedDt,
            waveNumber = snapshot.waveNumber,
            save = save,
            nowWallTimeMs = nowWallTimeMs,
            nextIdProvider = { nextId() }
        )
        goldDelta += enemyResult.goldDelta
        val newDefeatedInWave = snapshot.enemiesDefeatedInWave + enemyResult.defeatedCountDelta

        // 10. Base Defeat check
        if (currentBaseHp <= 0.0) {
            initializeForWave(save, WavePhase.PREPARING)
            snapshot = snapshot.copy(
                lastBannerMessage = "SAVUNMA HATTI YARILDI — DALGA ${save.currentWave} YENİDEN BAŞLIYOR!"
            )
            return EngineStepEvents(
                goldEarnedDelta = goldDelta,
                infantryKilledDelta = enemyResult.infKills,
                tanksKilledDelta = enemyResult.tankKills,
                aircraftKilledDelta = enemyResult.airKills,
                bossesKilledDelta = enemyResult.bossKills,
                shouldPersistSave = true
            )
        }

        // 11. Wave Completion check
        if (spawnQueue.isEmpty() && enemyResult.survivingEnemies.isEmpty()) {
            val rewards = CombatSimulationHelper.generateWaveRewardOptions(snapshot.waveNumber, save)
            waveCompleted = snapshot.waveNumber
            shouldPersist = true
            snapshot = snapshot.copy(
                wavePhase = WavePhase.REWARD_SELECTION,
                baseCurrentHp = currentBaseHp,
                shieldCurrentHp = currentShield,
                enemiesRemainingToSpawn = 0,
                enemiesDefeatedInWave = snapshot.totalWaveEnemies,
                enemies = emptyList(),
                alliedSoldiers = updatedAllies,
                projectiles = emptyList(),
                airSorties = airSortieResult.activeSorties,
                explosions = updatedExplosions,
                smokeTrail = updatedSmoke,
                floatingTexts = updatedFloatingTexts,
                rewardOptions = rewards,
                radarAngleDeg = newRadarAngle,
                airstrikeCooldownRemaining = airCooldown,
                airstrikeCharges = airCharges,
                artilleryCooldownRemaining = artCooldown,
                shieldBoostCooldownRemaining = shieldCooldown,
                turretMuzzleFlashTimer = turretFlash,
                siloLaunchFlashTimer = siloFlash,
                lastBannerMessage = "DALGA ${snapshot.waveNumber} TAMAMLANDI! TAKTİKSEL İKMAL SEÇİN"
            )
            return EngineStepEvents(
                goldEarnedDelta = goldDelta,
                infantryKilledDelta = enemyResult.infKills,
                tanksKilledDelta = enemyResult.tankKills,
                aircraftKilledDelta = enemyResult.airKills,
                bossesKilledDelta = enemyResult.bossKills,
                waveCompletedNumber = waveCompleted,
                shouldPersistSave = true
            )
        }

        snapshot = snapshot.copy(
            baseCurrentHp = currentBaseHp,
            shieldCurrentHp = currentShield,
            enemiesRemainingToSpawn = spawnQueue.size,
            enemiesDefeatedInWave = newDefeatedInWave,
            enemies = enemyResult.survivingEnemies,
            alliedSoldiers = updatedAllies,
            projectiles = projResult.survivingProjectiles,
            airSorties = airSortieResult.activeSorties,
            explosions = updatedExplosions,
            smokeTrail = updatedSmoke,
            floatingTexts = updatedFloatingTexts,
            radarAngleDeg = newRadarAngle,
            airstrikeCooldownRemaining = airCooldown,
            airstrikeCharges = airCharges,
            artilleryCooldownRemaining = artCooldown,
            shieldBoostCooldownRemaining = shieldCooldown,
            turretMuzzleFlashTimer = turretFlash,
            siloLaunchFlashTimer = siloFlash
        )

        return EngineStepEvents(
            goldEarnedDelta = goldDelta,
            infantryKilledDelta = enemyResult.infKills,
            tanksKilledDelta = enemyResult.tankKills,
            aircraftKilledDelta = enemyResult.airKills,
            bossesKilledDelta = enemyResult.bossKills,
            shouldPersistSave = shouldPersist
        )
    }
}
