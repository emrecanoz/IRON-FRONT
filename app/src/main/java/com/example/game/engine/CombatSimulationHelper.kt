package com.example.game.engine

import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.AlliedSoldier
import com.example.game.combat.EnemyType
import com.example.game.combat.EnemyUnit
import com.example.game.combat.WaveRewardOption
import com.example.game.combat.WaveRewardType
import com.example.game.economy.EconomyCalculator
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Pure calculation and spawn generation algorithms for the combat simulation.
 * Keeps [GameEngine] focused on the simulation state machine and loop.
 */
object CombatSimulationHelper {

    fun buildWaveSpawnQueue(wave: Int): List<EnemyType> {
        val list = mutableListOf<EnemyType>()
        val infantryCount = 5 + (wave * 2).coerceAtMost(24)
        val tankCount = if (wave >= 2) 1 + (wave / 2).coerceAtMost(10) else 0
        val jetCount = if (wave >= 3) 1 + ((wave - 1) / 2).coerceAtMost(8) else 0
        val hasBoss = (wave % 5 == 0)

        for (i in 0 until infantryCount) {
            list.add(EnemyType.INFANTRY)
            if (i % 2 == 1 && list.count { it == EnemyType.ARMORED_TANK } < tankCount) {
                list.add(EnemyType.ARMORED_TANK)
            }
            if (i % 3 == 2 && list.count { it == EnemyType.STEALTH_JET } < jetCount) {
                list.add(EnemyType.STEALTH_JET)
            }
        }
        while (list.count { it == EnemyType.ARMORED_TANK } < tankCount) {
            list.add(EnemyType.ARMORED_TANK)
        }
        while (list.count { it == EnemyType.STEALTH_JET } < jetCount) {
            list.add(EnemyType.STEALTH_JET)
        }
        if (hasBoss) {
            list.add(EnemyType.MEGA_BOSS_MECH)
        }
        return list
    }

    fun createEnemyUnit(
        id: Long,
        type: EnemyType,
        wave: Int,
        random: Random
    ): EnemyUnit {
        val scale = 1.18.pow((wave - 1).toDouble())
        val dmgScale = 1.13.pow((wave - 1).toDouble())
        val laneY = if (type.isFlying) {
            0.22f + random.nextFloat() * 0.12f
        } else if (type.isBoss) {
            0.56f
        } else {
            0.40f + random.nextFloat() * 0.38f
        }
        val hp = type.baseHp * scale
        return EnemyUnit(
            id = id,
            type = type,
            x = 0.98f + random.nextFloat() * 0.05f,
            laneY = laneY,
            maxHp = hp,
            currentHp = hp,
            speed = type.baseSpeed * (0.94f + random.nextFloat() * 0.12f),
            attackDamage = type.baseDamage * dmgScale
        )
    }

    fun buildInitialAlliedSquad(
        save: PlayerSaveEntity,
        nextIdProvider: () -> Long
    ): List<AlliedSoldier> {
        val count = min(6, 2 + (save.barracksMaxUnitsLv / 2))
        return (0 until count).map { idx ->
            createAlliedSoldier(save, idx, nextIdProvider())
        }
    }

    fun createAlliedSoldier(
        save: PlayerSaveEntity,
        index: Int,
        id: Long
    ): AlliedSoldier {
        val maxHp = (90.0 + save.barracksLevel * 25.0 + save.barracksHealthLv * 20.0)
        val dmg = (14.0 + save.barracksLevel * 5.5 + save.barracksDamageLv * 4.5)
        val laneY = (0.42f + (index % 4) * 0.09f).coerceIn(0.38f, 0.76f)
        return AlliedSoldier(
            id = id,
            x = 0.24f + (index % 3) * 0.025f,
            laneY = laneY,
            maxHp = maxHp,
            currentHp = maxHp,
            damage = dmg
        )
    }

    fun calculateMaxBaseHp(save: PlayerSaveEntity): Double {
        val base = 900.0 + (save.borderWallLevel * 220.0) + (save.wallMaxHpLv * 160.0)
        val permMult = 1.0 + (save.permBaseHealthLv * 0.30)
        return base * permMult
    }

    fun calculateMaxShieldHp(save: PlayerSaveEntity): Double {
        val base = 300.0 + (save.borderWallLevel * 95.0) + (save.wallShieldCapacityLv * 110.0)
        val cycleMult = 1.0 + (save.cycleShieldBonusPct / 100.0)
        val permMult = 1.0 + (save.permBaseHealthLv * 0.30)
        return base * cycleMult * permMult
    }

    fun calculateGlobalDamageMultiplier(
        save: PlayerSaveEntity,
        nowWallTimeMs: Long = System.currentTimeMillis()
    ): Double {
        val cycleFirepower = 1.0 + (save.cycleFirepowerBonusPct / 100.0)
        val permDamage = 1.0 + (save.permDamageLv * 0.25)
        val adDamageBoost = if (save.damageBoostUntilMs > nowWallTimeMs) 1.50 else 1.0
        return cycleFirepower * permDamage * adDamageBoost
    }

    fun calculateEnemyKillGold(
        type: EnemyType,
        wave: Int,
        save: PlayerSaveEntity,
        nowWallTimeMs: Long
    ): Double {
        val baseKill = 14.0 * 1.14.pow((wave - 1).toDouble()) * type.goldMultiplier
        val permGoldMult = 1.0 + (save.permGoldProductionLv * 0.25)
        val adMult = if (save.productionBoostUntilMs > nowWallTimeMs) 2.0 else 1.0
        return baseKill * permGoldMult * adMult
    }

    fun generateWaveRewardOptions(wave: Int, save: PlayerSaveEntity): List<WaveRewardOption> {
        val passivePerSec = EconomyCalculator.calculateTotalPassiveGoldPerSec(save)
        val instantVaultGold = max(180.0 * 1.18.pow(wave.toDouble()), passivePerSec * 32.0)

        return listOf(
            WaveRewardOption(
                type = WaveRewardType.FIREPOWER,
                titleTr = "Ateş Gücü Doktrini",
                descriptionTr = "Mevcut terfi döngüsü boyunca tüm askeri tesis ve taretlerin hasarını artırır.",
                valueSummaryTr = "+%15 Tüm Birim Hasarı",
                bonusPctOrGold = 15.0
            ),
            WaveRewardOption(
                type = WaveRewardType.ENERGY_SHIELD,
                titleTr = "Altıgen Kalkan Yüklemesi",
                descriptionTr = "Maksimum enerji kalkanı kapasitesini artırır ve üs canını onarır.",
                valueSummaryTr = "+%20 Kalkan Kapasitesi & Onarım",
                bonusPctOrGold = 20.0
            ),
            WaveRewardOption(
                type = WaveRewardType.TREASURE_VAULT,
                titleTr = "Stratejik Hazine Kasası",
                descriptionTr = "Tesis geliştirmeleri için anında yüksek miktarda altın ikmali sağlar.",
                valueSummaryTr = "+${EconomyCalculator.formatNumber(instantVaultGold)} Altın",
                bonusPctOrGold = instantVaultGold
            )
        )
    }
}
