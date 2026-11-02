package com.example.game.engine

import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.AirSortiePlane
import com.example.game.combat.CombatSnapshot
import com.example.game.combat.ExplosionEffect
import com.example.game.combat.Projectile
import com.example.game.combat.ProjectileKind
import com.example.game.combat.TacticalSkillType
import kotlin.math.max
import kotlin.math.min

/**
 * Handles active tactical abilities like Airstrikes, Artillery Barrages, and Energy Shield Overcharges.
 */
object CombatTacticalSkillsHandler {

    fun triggerShieldOvercharge(
        snapshot: CombatSnapshot,
        permSkillLv: Int,
        nextIdProvider: () -> Long
    ): CombatSnapshot {
        if (snapshot.shieldBoostCooldownRemaining > 0f) return snapshot
        val cooldown = max(10f, TacticalSkillType.SHIELD_OVERCHARGE.baseCooldownSec - permSkillLv * 1.2f)
        val restoredShield = snapshot.shieldMaxHp
        val healBase = min(snapshot.baseMaxHp, snapshot.baseCurrentHp + snapshot.baseMaxHp * 0.20)
        val newExplosion = ExplosionEffect(
            id = nextIdProvider(),
            x = 0.28f,
            y = 0.56f,
            radius = 0.22f,
            progress = 0f,
            isBigRocketOrBomb = false,
            isShieldHit = true
        )
        return snapshot.copy(
            shieldCurrentHp = restoredShield,
            baseCurrentHp = healBase,
            shieldBoostCooldownRemaining = cooldown,
            activeTargetingSkill = null,
            explosions = (snapshot.explosions + newExplosion).takeLast(24),
            lastBannerMessage = "ENERJİ KALKANI %100 YÜKLENDİ!"
        )
    }

    fun executeBattlefieldTap(
        snapshot: CombatSnapshot,
        normX: Float,
        normY: Float,
        save: PlayerSaveEntity,
        nextIdProvider: () -> Long
    ): Pair<CombatSnapshot, Boolean> {
        val targetX = normX.coerceIn(0.32f, 0.94f)
        val targetY = normY.coerceIn(0.28f, 0.84f)
        val activeSkill = snapshot.activeTargetingSkill ?: TacticalSkillType.AIRSTRIKE

        return when (activeSkill) {
            TacticalSkillType.AIRSTRIKE -> {
                if (snapshot.airstrikeCharges <= 0 && snapshot.airstrikeCooldownRemaining > 0f) {
                    Pair(snapshot, false)
                } else {
                    val cooldownBase = max(6f, TacticalSkillType.AIRSTRIKE.baseCooldownSec - save.permSupportSkillLv * 1.1f)
                    val newCharges = max(0, snapshot.airstrikeCharges - 1)
                    val newPlane = AirSortiePlane(
                        id = nextIdProvider(),
                        x = -0.12f,
                        y = targetY,
                        speed = 0.65f,
                        isTacticalStrike = true,
                        targetDropX = targetX
                    )
                    val updated = snapshot.copy(
                        airSorties = snapshot.airSorties + newPlane,
                        airstrikeCharges = newCharges,
                        airstrikeCooldownRemaining = if (snapshot.airstrikeCooldownRemaining <= 0f) cooldownBase else snapshot.airstrikeCooldownRemaining,
                        activeTargetingSkill = null,
                        lastBannerMessage = "TAKTİKSEL HAVA DESTEĞİ YOLDA!"
                    )
                    Pair(updated, true)
                }
            }

            TacticalSkillType.ARTILLERY_BARRAGE -> {
                if (snapshot.artilleryCooldownRemaining > 0f) {
                    Pair(snapshot, false)
                } else {
                    val cooldown = max(8f, TacticalSkillType.ARTILLERY_BARRAGE.baseCooldownSec - save.permSupportSkillLv * 1.3f)
                    val baseDmg = CombatSimulationHelper.calculateGlobalDamageMultiplier(save) * (140.0 + save.rocketSiloLevel * 45.0)
                    val shells = (0 until 5).map { idx ->
                        val offsetX = (idx - 2) * 0.045f
                        val offsetY = ((idx % 2) - 0.5f) * 0.06f
                        Projectile(
                            id = nextIdProvider(),
                            kind = ProjectileKind.ARTILLERY_SHELL,
                            startX = 0.14f,
                            startY = 0.45f,
                            currentX = 0.14f,
                            currentY = 0.45f,
                            targetX = (targetX + offsetX).coerceIn(0.32f, 0.95f),
                            targetY = (targetY + offsetY).coerceIn(0.28f, 0.85f),
                            progress = 0f,
                            speed = 0.95f + idx * 0.08f,
                            damage = baseDmg,
                            blastRadius = 0.14f,
                            arcHeight = 0.22f
                        )
                    }
                    val updated = snapshot.copy(
                        projectiles = snapshot.projectiles + shells,
                        artilleryCooldownRemaining = cooldown,
                        activeTargetingSkill = null,
                        lastBannerMessage = "TOPÇU BATARYASI ATEŞLENDİ!"
                    )
                    Pair(updated, true)
                }
            }

            TacticalSkillType.SHIELD_OVERCHARGE -> {
                val updated = triggerShieldOvercharge(snapshot, save.permSupportSkillLv, nextIdProvider)
                Pair(updated, true)
            }
        }
    }
}
