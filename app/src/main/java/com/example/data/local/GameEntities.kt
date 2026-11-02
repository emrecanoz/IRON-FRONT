package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_save")
data class PlayerSaveEntity(
    @PrimaryKey val id: Int = 1,
    val gold: Double = 250.0,
    val diamonds: Int = 45,
    val currentWave: Int = 1,
    val highestWaveReached: Int = 1,
    val prestigeCount: Int = 0,
    val promotionPoints: Int = 0,
    val totalGoldEarned: Double = 250.0,

    // Facility main levels & 4 sub-upgrades each
    // 4.1 Askeri Kışla (Barracks)
    val barracksLevel: Int = 1,
    val barracksSpawnRateLv: Int = 1,
    val barracksDamageLv: Int = 1,
    val barracksHealthLv: Int = 1,
    val barracksMaxUnitsLv: Int = 1,

    // 4.2 Roket Silosu (Rocket Silo)
    val rocketSiloLevel: Int = 1,
    val rocketDamageLv: Int = 1,
    val rocketFireRateLv: Int = 1,
    val rocketBlastRadiusLv: Int = 1,
    val rocketMultiTargetLv: Int = 1,

    // 4.3 Uçak Filosu (Air Fleet)
    val airFleetLevel: Int = 1,
    val airDamageLv: Int = 1,
    val airFrequencyLv: Int = 1,
    val airTargetCountLv: Int = 1,
    val airBombAreaLv: Int = 1,

    // 4.4 Sınır Duvarı (Border Wall)
    val borderWallLevel: Int = 1,
    val wallMaxHpLv: Int = 1,
    val wallArmorLv: Int = 1,
    val wallDamageReductionLv: Int = 1,
    val wallShieldCapacityLv: Int = 1,

    // Temporary Prestige-cycle Wave Rewards (Section 6)
    val cycleFirepowerBonusPct: Int = 0,
    val cycleShieldBonusPct: Int = 0,

    // Permanent Prestige Upgrades (Section 8)
    val permDamageLv: Int = 0,
    val permGoldProductionLv: Int = 0,
    val permBaseHealthLv: Int = 0,
    val permSupportSkillLv: Int = 0,
    val permAutomationLv: Int = 0,

    // Statistics & Achievements (Section 12)
    val totalInfantryKilled: Int = 0,
    val totalTanksKilled: Int = 0,
    val totalAircraftKilled: Int = 0,
    val totalBossesKilled: Int = 0,
    val claimedMissionIdsCsv: String = "",

    // Monetization & Skins (Sections 10 & 11)
    val unlockedSpeed2x: Boolean = false,
    val starterPackPurchased: Boolean = false,
    val permanentAutomationPurchased: Boolean = false,
    val adFreePackPurchased: Boolean = false,
    val autoUpgradeEnabled: Boolean = false,
    val selectedBaseSkinId: String = "STANDARD",
    val ownedSkinsCsv: String = "STANDARD",

    // Active temporary Ad Boosts (timestamps in wall-clock ms)
    val productionBoostUntilMs: Long = 0L,
    val damageBoostUntilMs: Long = 0L,

    // Offline Earnings & Anti-Cheat Timestamps (Section 9)
    val lastSavedWallTimeMs: Long = System.currentTimeMillis(),
    val lastSavedPassiveRatePerSec: Double = 12.0,
    val lastClaimedOfflineTimestampMs: Long = 0L
)
