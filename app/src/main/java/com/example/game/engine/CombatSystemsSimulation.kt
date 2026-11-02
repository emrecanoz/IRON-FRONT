package com.example.game.engine

import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.AlliedSoldier
import com.example.game.combat.EnemyType
import com.example.game.combat.EnemyUnit
import com.example.game.combat.ExplosionEffect
import com.example.game.combat.FloatingDamageText
import com.example.game.combat.Projectile
import com.example.game.combat.ProjectileKind
import com.example.game.combat.SmokeParticle
import com.example.game.economy.EconomyCalculator
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class ProjectileResolutionResult(
    val survivingProjectiles: List<Projectile>,
    val updatedBaseHp: Double,
    val updatedShieldHp: Double
)

data class EnemyResolutionResult(
    val survivingEnemies: List<EnemyUnit>,
    val defeatedCountDelta: Int,
    val goldDelta: Double,
    val infKills: Int,
    val tankKills: Int,
    val airKills: Int,
    val bossKills: Int
)

/**
 * Handles sub-system simulation ticks: soldiers, weapon systems, projectiles and enemy movements.
 */
object CombatSystemsSimulation {

    fun updateAlliedSoldiers(
        allies: List<AlliedSoldier>,
        enemies: List<EnemyUnit>,
        projectiles: MutableList<Projectile>,
        clampedDt: Float,
        globalDmgMult: Double,
        save: PlayerSaveEntity,
        nextIdProvider: () -> Long
    ): List<AlliedSoldier> {
        val updatedAllies = mutableListOf<AlliedSoldier>()
        for (soldier in allies) {
            val targetPostX = 0.29f + ((soldier.id % 4) * 0.032f)
            val movedX = if (soldier.x < targetPostX) {
                min(targetPostX, soldier.x + clampedDt * 0.07f)
            } else {
                soldier.x
            }
            var fireCd = max(0f, soldier.fireCooldownSec - clampedDt)
            var muzzleFlash = max(0f, soldier.muzzleFlashTimer - clampedDt)

            val groundTarget = enemies
                .filter { !it.type.isFlying && it.currentHp > 0.0 && it.x <= 0.85f }
                .minByOrNull { it.x }

            if (groundTarget != null && fireCd <= 0f) {
                fireCd = max(0.35f, 0.95f - save.barracksSpawnRateLv * 0.04f)
                muzzleFlash = 0.14f
                projectiles.add(
                    Projectile(
                        id = nextIdProvider(),
                        kind = ProjectileKind.ALLY_BULLET,
                        startX = movedX + 0.02f,
                        startY = soldier.laneY,
                        currentX = movedX + 0.02f,
                        currentY = soldier.laneY,
                        targetX = groundTarget.x,
                        targetY = groundTarget.laneY,
                        progress = 0f,
                        speed = 2.6f,
                        damage = soldier.damage * globalDmgMult,
                        blastRadius = 0.02f,
                        arcHeight = 0.01f
                    )
                )
            }

            if (soldier.currentHp > 0.0) {
                updatedAllies.add(
                    soldier.copy(
                        x = movedX,
                        fireCooldownSec = fireCd,
                        walkPhase = (soldier.walkPhase + clampedDt * 6f) % 6.28f,
                        muzzleFlashTimer = muzzleFlash
                    )
                )
            }
        }
        return updatedAllies
    }

    fun updateProjectilesAndImpacts(
        projectiles: List<Projectile>,
        enemies: MutableList<EnemyUnit>,
        updatedExplosions: MutableList<ExplosionEffect>,
        updatedSmoke: MutableList<SmokeParticle>,
        updatedFloatingTexts: MutableList<FloatingDamageText>,
        clampedDt: Float,
        save: PlayerSaveEntity,
        initialBaseHp: Double,
        initialShieldHp: Double,
        nextIdProvider: () -> Long
    ): ProjectileResolutionResult {
        val surviving = mutableListOf<Projectile>()
        var currentBaseHp = initialBaseHp
        var currentShield = initialShieldHp

        for (proj in projectiles) {
            val nextProg = proj.progress + proj.speed * clampedDt
            val curX = proj.startX + (proj.targetX - proj.startX) * nextProg.coerceIn(0f, 1f)
            val linearY = proj.startY + (proj.targetY - proj.startY) * nextProg.coerceIn(0f, 1f)
            val arcOffset = sin(nextProg.coerceIn(0f, 1f) * Math.PI.toFloat()) * proj.arcHeight
            val curY = linearY - arcOffset

            if (proj.kind == ProjectileKind.HOMING_ROCKET || proj.kind == ProjectileKind.ARTILLERY_SHELL) {
                if (updatedSmoke.size < 45) {
                    updatedSmoke.add(
                        SmokeParticle(
                            x = curX,
                            y = curY,
                            alpha = 0.65f,
                            size = if (proj.kind == ProjectileKind.HOMING_ROCKET) 6f else 8f
                        )
                    )
                }
            }

            if (nextProg < 1f) {
                surviving.add(
                    proj.copy(
                        currentX = curX,
                        currentY = curY,
                        progress = nextProg
                    )
                )
            } else {
                when (proj.kind) {
                    ProjectileKind.ENEMY_SHELL, ProjectileKind.BOSS_PLASMA -> {
                        val rawIncoming = proj.damage
                        val wallMitigation = (1.0 - (save.wallDamageReductionLv * 0.035).coerceAtMost(0.60))
                        val flatArmor = save.wallArmorLv * 2.5
                        val effectiveDmg = max(2.0, (rawIncoming * wallMitigation) - flatArmor)

                        if (currentShield > 0.0) {
                            val absorbed = min(currentShield, effectiveDmg)
                            currentShield -= absorbed
                            val leftover = effectiveDmg - absorbed
                            currentBaseHp = max(0.0, currentBaseHp - leftover)
                            if (updatedExplosions.size < 25) {
                                updatedExplosions.add(
                                    ExplosionEffect(
                                        id = nextIdProvider(),
                                        x = proj.targetX,
                                        y = proj.targetY,
                                        radius = 0.06f,
                                        progress = 0f,
                                        isBigRocketOrBomb = false,
                                        isShieldHit = true
                                    )
                                )
                            }
                        } else {
                            currentBaseHp = max(0.0, currentBaseHp - effectiveDmg)
                            if (updatedExplosions.size < 25) {
                                updatedExplosions.add(
                                    ExplosionEffect(
                                        id = nextIdProvider(),
                                        x = proj.targetX,
                                        y = proj.targetY,
                                        radius = 0.07f,
                                        progress = 0f,
                                        isBigRocketOrBomb = proj.kind == ProjectileKind.BOSS_PLASMA
                                    )
                                )
                            }
                        }
                    }

                    else -> {
                        val isBig = proj.kind == ProjectileKind.HOMING_ROCKET ||
                            proj.kind == ProjectileKind.AIR_BOMB ||
                            proj.kind == ProjectileKind.ARTILLERY_SHELL

                        if (updatedExplosions.size < 28) {
                            updatedExplosions.add(
                                ExplosionEffect(
                                    id = nextIdProvider(),
                                    x = proj.targetX,
                                    y = proj.targetY,
                                    radius = if (isBig) proj.blastRadius else 0.04f,
                                    progress = 0f,
                                    isBigRocketOrBomb = isBig
                                )
                            )
                        }

                        var hitAny = false
                        for (idx in enemies.indices) {
                            val enemy = enemies[idx]
                            if (enemy.currentHp <= 0.0) continue
                            if (enemy.type.isFlying && proj.kind == ProjectileKind.ALLY_BULLET) continue

                            val dist = hypot(enemy.x - proj.targetX, (enemy.laneY - proj.targetY) * 0.65f)
                            val hitThreshold = max(0.055f, proj.blastRadius)
                            if (dist <= hitThreshold || (!isBig && !hitAny && abs(enemy.x - proj.targetX) < 0.075f)) {
                                hitAny = true
                                val antiArmorBonus = if (proj.kind == ProjectileKind.HOMING_ROCKET &&
                                    (enemy.type == EnemyType.ARMORED_TANK || enemy.type == EnemyType.MEGA_BOSS_MECH)
                                ) {
                                    1.45
                                } else {
                                    1.0
                                }
                                val armorFactor = if (proj.kind == ProjectileKind.HOMING_ROCKET) {
                                    1.0
                                } else {
                                    1.0 - enemy.type.armorReduction
                                }
                                val finalDmg = max(1.0, proj.damage * antiArmorBonus * armorFactor)
                                val newHp = enemy.currentHp - finalDmg
                                enemies[idx] = enemy.copy(
                                    currentHp = newHp,
                                    hitFlashTimer = 0.14f
                                )
                                if (updatedFloatingTexts.size < 18) {
                                    updatedFloatingTexts.add(
                                        FloatingDamageText(
                                            id = nextIdProvider(),
                                            x = enemy.x,
                                            y = if (enemy.type.isFlying) 0.26f else enemy.laneY - 0.06f,
                                            text = "-${EconomyCalculator.formatNumber(finalDmg)}",
                                            isCriticalOrRocket = isBig
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        return ProjectileResolutionResult(
            survivingProjectiles = surviving,
            updatedBaseHp = currentBaseHp,
            updatedShieldHp = currentShield
        )
    }

    fun updateEnemiesAndCombat(
        enemies: List<EnemyUnit>,
        projectiles: MutableList<Projectile>,
        updatedFloatingTexts: MutableList<FloatingDamageText>,
        clampedDt: Float,
        waveNumber: Int,
        save: PlayerSaveEntity,
        nowWallTimeMs: Long,
        nextIdProvider: () -> Long
    ): EnemyResolutionResult {
        val survivingEnemies = mutableListOf<EnemyUnit>()
        var defeatedCountDelta = 0
        var goldDelta = 0.0
        var infKills = 0
        var tankKills = 0
        var airKills = 0
        var bossKills = 0

        for (enemy in enemies) {
            if (enemy.currentHp <= 0.0) {
                defeatedCountDelta += 1
                val killGold = CombatSimulationHelper.calculateEnemyKillGold(enemy.type, waveNumber, save, nowWallTimeMs)
                goldDelta += killGold
                when (enemy.type) {
                    EnemyType.INFANTRY -> infKills += 1
                    EnemyType.ARMORED_TANK -> tankKills += 1
                    EnemyType.STEALTH_JET -> airKills += 1
                    EnemyType.MEGA_BOSS_MECH -> bossKills += 1
                }
                if (updatedFloatingTexts.size < 20) {
                    updatedFloatingTexts.add(
                        FloatingDamageText(
                            id = nextIdProvider(),
                            x = enemy.x,
                            y = enemy.laneY - 0.08f,
                            text = "+${EconomyCalculator.formatNumber(killGold)} AU",
                            isCriticalOrRocket = enemy.type.isBoss,
                            isGoldGain = true
                        )
                    )
                }
                continue
            }

            val stopX = enemy.type.attackRangeX
            val nextX = if (enemy.x > stopX) {
                max(stopX, enemy.x - enemy.speed * clampedDt)
            } else {
                enemy.x
            }
            var atkCd = max(0f, enemy.attackCooldownSec - clampedDt)
            val hitFlash = max(0f, enemy.hitFlashTimer - clampedDt)

            if (nextX <= stopX + 0.01f && atkCd <= 0f) {
                atkCd = when (enemy.type) {
                    EnemyType.INFANTRY -> 1.15f
                    EnemyType.ARMORED_TANK -> 2.10f
                    EnemyType.STEALTH_JET -> 1.45f
                    EnemyType.MEGA_BOSS_MECH -> 1.65f
                }
                projectiles.add(
                    Projectile(
                        id = nextIdProvider(),
                        kind = if (enemy.type.isBoss) ProjectileKind.BOSS_PLASMA else ProjectileKind.ENEMY_SHELL,
                        startX = nextX - 0.02f,
                        startY = if (enemy.type.isFlying) 0.28f else enemy.laneY,
                        currentX = nextX - 0.02f,
                        currentY = if (enemy.type.isFlying) 0.28f else enemy.laneY,
                        targetX = 0.26f,
                        targetY = enemy.laneY.coerceIn(0.38f, 0.72f),
                        progress = 0f,
                        speed = 1.9f,
                        damage = enemy.attackDamage,
                        blastRadius = 0.05f,
                        arcHeight = if (enemy.type == EnemyType.ARMORED_TANK) 0.06f else 0.01f
                    )
                )
            }

            survivingEnemies.add(
                enemy.copy(
                    x = nextX,
                    attackCooldownSec = atkCd,
                    walkCyclePhase = (enemy.walkCyclePhase + clampedDt * 5.5f) % 6.28f,
                    hitFlashTimer = hitFlash
                )
            )
        }

        return EnemyResolutionResult(
            survivingEnemies = survivingEnemies,
            defeatedCountDelta = defeatedCountDelta,
            goldDelta = goldDelta,
            infKills = infKills,
            tankKills = tankKills,
            airKills = airKills,
            bossKills = bossKills
        )
    }
}
