package com.example.game.engine

import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.AirSortiePlane
import com.example.game.combat.EnemyType
import com.example.game.combat.EnemyUnit
import com.example.game.combat.Projectile
import com.example.game.combat.ProjectileKind
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class AirSortiesStepResult(
    val updatedTimer: Float,
    val activeSorties: List<AirSortiePlane>
)

/**
 * Handles automated defense systems: Wall Turrets, Rocket Silo missiles, and Air Fleet sorties.
 */
object CombatDefensesSimulation {

    fun updateTurrets(
        currentTimer: Float,
        clampedDt: Float,
        enemies: List<EnemyUnit>,
        save: PlayerSaveEntity,
        globalDmgMult: Double,
        projectiles: MutableList<Projectile>,
        nextIdProvider: () -> Long
    ): Pair<Float, Float> {
        var turretTimer = currentTimer + clampedDt
        var turretFlash = 0f
        val turretInterval = max(0.28f, 0.72f - save.borderWallLevel * 0.03f)

        if (turretTimer >= turretInterval && enemies.isNotEmpty()) {
            turretTimer = 0f
            val priorityTarget = enemies
                .filter { it.currentHp > 0.0 }
                .minByOrNull { if (it.type.isFlying) it.x - 0.15f else it.x }
            if (priorityTarget != null) {
                turretFlash = 0.15f
                val turretDamage = (18.0 + save.borderWallLevel * 7.5 + save.wallArmorLv * 4.0) * globalDmgMult
                projectiles.add(
                    Projectile(
                        id = nextIdProvider(),
                        kind = ProjectileKind.TURRET_TRACER,
                        startX = 0.25f,
                        startY = 0.48f,
                        currentX = 0.25f,
                        currentY = 0.48f,
                        targetX = priorityTarget.x,
                        targetY = priorityTarget.laneY,
                        progress = 0f,
                        speed = 3.1f,
                        damage = turretDamage,
                        blastRadius = 0.03f,
                        arcHeight = 0.0f
                    )
                )
            }
        }
        return Pair(turretTimer, turretFlash)
    }

    fun updateRocketSilo(
        currentTimer: Float,
        clampedDt: Float,
        enemies: List<EnemyUnit>,
        save: PlayerSaveEntity,
        globalDmgMult: Double,
        projectiles: MutableList<Projectile>,
        nextIdProvider: () -> Long
    ): Pair<Float, Float> {
        var rocketTimer = currentTimer + clampedDt
        var siloFlash = 0f
        val rocketInterval = max(1.1f, 3.2f - save.rocketFireRateLv * 0.16f)

        if (rocketTimer >= rocketInterval && enemies.isNotEmpty()) {
            rocketTimer = 0f
            val missileCount = min(4, 1 + (save.rocketMultiTargetLv / 2))
            val sortedTargets = enemies
                .filter { it.currentHp > 0.0 }
                .sortedByDescending {
                    when (it.type) {
                        EnemyType.MEGA_BOSS_MECH -> 1000.0
                        EnemyType.ARMORED_TANK -> 500.0
                        EnemyType.STEALTH_JET -> 250.0
                        EnemyType.INFANTRY -> 100.0
                    } - it.x * 50.0
                }
            if (sortedTargets.isNotEmpty()) {
                siloFlash = 0.28f
                val rocketDmg = (55.0 + save.rocketSiloLevel * 24.0 + save.rocketDamageLv * 18.0) * globalDmgMult
                val blastRad = (0.09f + save.rocketBlastRadiusLv * 0.012f).coerceAtMost(0.22f)
                for (i in 0 until missileCount) {
                    val tgt = sortedTargets[i % sortedTargets.size]
                    projectiles.add(
                        Projectile(
                            id = nextIdProvider(),
                            kind = ProjectileKind.HOMING_ROCKET,
                            startX = 0.12f,
                            startY = 0.34f,
                            currentX = 0.12f,
                            currentY = 0.34f,
                            targetX = tgt.x,
                            targetY = tgt.laneY,
                            progress = 0f,
                            speed = 1.15f + (i * 0.08f),
                            damage = rocketDmg,
                            blastRadius = blastRad,
                            arcHeight = 0.18f + (i * 0.03f)
                        )
                    )
                }
            }
        }
        return Pair(rocketTimer, siloFlash)
    }

    fun updateAirSortiesAndBombs(
        currentTimer: Float,
        sorties: List<AirSortiePlane>,
        enemies: List<EnemyUnit>,
        clampedDt: Float,
        save: PlayerSaveEntity,
        globalDmgMult: Double,
        random: Random,
        projectiles: MutableList<Projectile>,
        nextIdProvider: () -> Long
    ): AirSortiesStepResult {
        var airTimer = currentTimer + clampedDt
        val sortieInterval = max(2.8f, 6.8f - save.airFrequencyLv * 0.30f)
        val workingSorties = sorties.toMutableList()

        if (airTimer >= sortieInterval && enemies.isNotEmpty()) {
            airTimer = 0f
            val clusterX = enemies.map { it.x }.average().toFloat().coerceIn(0.38f, 0.88f)
            workingSorties.add(
                AirSortiePlane(
                    id = nextIdProvider(),
                    x = -0.10f,
                    y = 0.22f + random.nextFloat() * 0.10f,
                    speed = 0.55f,
                    isTacticalStrike = false,
                    targetDropX = clusterX
                )
            )
        }

        val activeSorties = mutableListOf<AirSortiePlane>()
        for (plane in workingSorties) {
            val nextX = plane.x + plane.speed * clampedDt
            var dropped = plane.hasDroppedPayload
            if (!dropped && nextX >= plane.targetDropX - 0.08f) {
                dropped = true
                val bombCount = if (plane.isTacticalStrike) {
                    4 + (save.permSupportSkillLv / 2)
                } else {
                    min(5, 1 + (save.airTargetCountLv / 2))
                }
                val bombDmg = if (plane.isTacticalStrike) {
                    (165.0 + save.airFleetLevel * 45.0 + save.permSupportSkillLv * 65.0) * globalDmgMult
                } else {
                    (48.0 + save.airFleetLevel * 20.0 + save.airDamageLv * 15.0) * globalDmgMult
                }
                val bombRadius = (0.11f + save.airBombAreaLv * 0.012f).coerceAtMost(0.24f)
                for (b in 0 until bombCount) {
                    val dropX = (plane.targetDropX + (b - bombCount / 2) * 0.055f).coerceIn(0.32f, 0.94f)
                    val dropY = (0.48f + ((b % 3) - 1) * 0.11f).coerceIn(0.30f, 0.82f)
                    projectiles.add(
                        Projectile(
                            id = nextIdProvider(),
                            kind = ProjectileKind.AIR_BOMB,
                            startX = nextX,
                            startY = plane.y,
                            currentX = nextX,
                            currentY = plane.y,
                            targetX = dropX,
                            targetY = dropY,
                            progress = 0f,
                            speed = 1.65f,
                            damage = bombDmg,
                            blastRadius = bombRadius,
                            arcHeight = 0.02f
                        )
                    )
                }
            }
            if (nextX <= 1.18f) {
                activeSorties.add(plane.copy(x = nextX, hasDroppedPayload = dropped))
            }
        }

        return AirSortiesStepResult(
            updatedTimer = airTimer,
            activeSorties = activeSorties
        )
    }
}
