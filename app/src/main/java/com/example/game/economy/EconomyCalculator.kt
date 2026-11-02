package com.example.game.economy

import com.example.data.local.PlayerSaveEntity
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class OfflineEarningsReport(
    val elapsedSeconds: Long,
    val cappedSeconds: Long,
    val passiveRatePerSec: Double,
    val baseRewardGold: Double,
    val doubleAdRewardGold: Double,
    val saveTimestampId: Long
)

object EconomyCalculator {
    const val MAX_OFFLINE_SECONDS = 3L * 3600L // 3 hours max (10,800s)
    const val MIN_OFFLINE_SECONDS_TO_TRIGGER = 15L // Show popup if away >= 15s

    /**
     * Formats large idle numbers with standard K, M, B, T, Qa suffixes.
     */
    fun formatNumber(value: Double): String {
        if (value < 0) return "-" + formatNumber(-value)
        if (value < 1000.0) {
            return if (value >= 100.0) {
                String.format(Locale.US, "%.0f", value)
            } else if (value >= 10.0) {
                String.format(Locale.US, "%.1f", value).removeSuffix(".0")
            } else {
                String.format(Locale.US, "%.0f", value)
            }
        }
        val suffixes = arrayOf("", "K", "M", "B", "T", "Qa", "Qi")
        var temp = value
        var idx = 0
        while (temp >= 1000.0 && idx < suffixes.size - 1) {
            temp /= 1000.0
            idx++
        }
        return if (temp >= 100.0) {
            String.format(Locale.US, "%.1f%s", temp, suffixes[idx])
        } else {
            String.format(Locale.US, "%.2f%s", temp, suffixes[idx])
        }
    }

    fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            hrs > 0 -> "${hrs}sa ${mins}dk ${secs}sn"
            mins > 0 -> "${mins}dk ${secs}sn"
            else -> "${secs}sn"
        }
    }

    /**
     * UpgradeCost(level) = BaseCost * 1.15^(level - 1)
     */
    fun calculateUpgradeCost(baseCost: Double, currentLevel: Int): Double {
        val exponent = max(0, currentLevel - 1)
        return baseCost * 1.15.pow(exponent.toDouble())
    }

    /**
     * Production(level) = BaseProduction * 1.12^(level - 1)
     */
    fun calculateFacilityProduction(baseProduction: Double, level: Int): Double {
        val exponent = max(0, level - 1)
        return baseProduction * 1.12.pow(exponent.toDouble())
    }

    /**
     * Calculates total passive gold per second from HQ + 4 military facilities + permanent bonuses + boosts.
     */
    fun calculateTotalPassiveGoldPerSec(
        save: PlayerSaveEntity,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Double {
        val hqBase = 6.0
        val barracksProd = calculateFacilityProduction(8.0, save.barracksLevel) *
            (1.0 + (save.barracksSpawnRateLv - 1) * 0.04)
        val rocketProd = calculateFacilityProduction(14.0, save.rocketSiloLevel) *
            (1.0 + (save.rocketDamageLv - 1) * 0.04)
        val airProd = calculateFacilityProduction(22.0, save.airFleetLevel) *
            (1.0 + (save.airFrequencyLv - 1) * 0.04)
        val wallProd = calculateFacilityProduction(10.0, save.borderWallLevel) *
            (1.0 + (save.wallShieldCapacityLv - 1) * 0.04)

        val rawTotal = hqBase + barracksProd + rocketProd + airProd + wallProd

        // Permanent Prestige Gold Bonus (+25% per level)
        val prestigeGoldMult = 1.0 + (save.permGoldProductionLv * 0.25)

        // Permanent Automation Bonus (+15% per level + 25% if IAP purchased)
        val automationMult = 1.0 + (save.permAutomationLv * 0.15) +
            if (save.permanentAutomationPurchased) 0.25 else 0.0

        // Starter Pack Bonus (+20% passive production)
        val starterMult = if (save.starterPackPurchased) 1.20 else 1.0

        // Cosmetic Base Skin Bonus (+10% for premium skins)
        val skinMult = if (save.selectedBaseSkinId != "STANDARD") 1.10 else 1.0

        // Temporary 15-min Ad Boost (2x)
        val adBoostMult = if (save.productionBoostUntilMs > currentTimeMs) 2.0 else 1.0

        return rawTotal * prestigeGoldMult * automationMult * starterMult * skinMult * adBoostMult
    }

    /**
     * Evaluates offline earnings safely with anti-clock-tampering and duplicate-claim guard.
     */
    fun evaluateOfflineEarnings(
        save: PlayerSaveEntity,
        nowWallTimeMs: Long = System.currentTimeMillis()
    ): OfflineEarningsReport? {
        val lastSaved = save.lastSavedWallTimeMs
        // Anti-clock rollback check: if user rolled clock backwards, ignore offline reward
        if (nowWallTimeMs <= lastSaved) return null

        // Duplicate claim guard: cannot claim twice for the same saved timestamp
        if (save.lastClaimedOfflineTimestampMs >= lastSaved && lastSaved > 0L) return null

        val elapsedSec = (nowWallTimeMs - lastSaved) / 1000L
        if (elapsedSec < MIN_OFFLINE_SECONDS_TO_TRIGGER) return null

        val cappedSec = min(elapsedSec, MAX_OFFLINE_SECONDS)
        val rate = max(save.lastSavedPassiveRatePerSec, calculateTotalPassiveGoldPerSec(save, nowWallTimeMs))
        val baseGold = rate * cappedSec.toDouble()

        return OfflineEarningsReport(
            elapsedSeconds = elapsedSec,
            cappedSeconds = cappedSec,
            passiveRatePerSec = rate,
            baseRewardGold = baseGold,
            doubleAdRewardGold = baseGold * 2.0,
            saveTimestampId = lastSaved
        )
    }

    /**
     * Calculates promotion points earned if player prestiges at [currentWave].
     * Unlocked after completing Wave 10 (i.e. when currentWave >= 11 or wave 10 completed).
     */
    fun calculatePrestigePointsReward(currentWave: Int): Int {
        if (currentWave < 10) return 0
        val waveFactor = (currentWave - 9).toDouble()
        return max(1, (waveFactor.pow(1.35) * 2.5).toInt())
    }
}
