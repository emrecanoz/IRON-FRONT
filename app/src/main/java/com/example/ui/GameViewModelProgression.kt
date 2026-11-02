package com.example.ui

import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.WavePhase
import com.example.game.economy.EconomyCalculator
import com.example.game.progression.BaseSkinOption
import com.example.game.progression.FacilityType
import com.example.game.progression.MissionItem
import com.example.game.progression.PrestigeUpgradeId
import com.example.game.progression.ProgressionCatalog
import com.example.game.progression.SubUpgradeId

/**
 * Progression, Facility Upgrades, Prestige promotions, and Missions for [GameViewModel].
 */
fun GameViewModel.upgradeFacilityMain(type: FacilityType) {
    val save = uiState.value.save
    val currentLv = ProgressionCatalog.getFacilityLevel(save, type)
    val cost = EconomyCalculator.calculateUpgradeCost(type.baseCost, currentLv)
    if (save.gold < cost) {
        showToast("Yetersiz Altın! Gereken: ${EconomyCalculator.formatNumber(cost)}")
        return
    }

    val updated = when (type) {
        FacilityType.BARRACKS -> save.copy(gold = save.gold - cost, barracksLevel = currentLv + 1)
        FacilityType.ROCKET_SILO -> save.copy(gold = save.gold - cost, rocketSiloLevel = currentLv + 1)
        FacilityType.AIR_FLEET -> save.copy(gold = save.gold - cost, airFleetLevel = currentLv + 1)
        FacilityType.BORDER_WALL -> save.copy(gold = save.gold - cost, borderWallLevel = currentLv + 1)
    }
    engine.syncStatsFromSave(updated)
    persistSaveAndSync(updated)
}

fun GameViewModel.upgradeSubAttribute(subId: SubUpgradeId) {
    val save = uiState.value.save
    val currentLv = ProgressionCatalog.getSubUpgradeLevel(save, subId)
    val cost = EconomyCalculator.calculateUpgradeCost(subId.baseCost, currentLv)
    if (save.gold < cost) {
        showToast("Yetersiz Altın! Gereken: ${EconomyCalculator.formatNumber(cost)}")
        return
    }

    val updated = when (subId) {
        SubUpgradeId.BARRACKS_SPAWN_RATE -> save.copy(gold = save.gold - cost, barracksSpawnRateLv = currentLv + 1)
        SubUpgradeId.BARRACKS_DAMAGE -> save.copy(gold = save.gold - cost, barracksDamageLv = currentLv + 1)
        SubUpgradeId.BARRACKS_HEALTH -> save.copy(gold = save.gold - cost, barracksHealthLv = currentLv + 1)
        SubUpgradeId.BARRACKS_MAX_UNITS -> save.copy(gold = save.gold - cost, barracksMaxUnitsLv = currentLv + 1)
        SubUpgradeId.ROCKET_DAMAGE -> save.copy(gold = save.gold - cost, rocketDamageLv = currentLv + 1)
        SubUpgradeId.ROCKET_FIRE_RATE -> save.copy(gold = save.gold - cost, rocketFireRateLv = currentLv + 1)
        SubUpgradeId.ROCKET_BLAST_RADIUS -> save.copy(gold = save.gold - cost, rocketBlastRadiusLv = currentLv + 1)
        SubUpgradeId.ROCKET_MULTI_TARGET -> save.copy(gold = save.gold - cost, rocketMultiTargetLv = currentLv + 1)
        SubUpgradeId.AIR_DAMAGE -> save.copy(gold = save.gold - cost, airDamageLv = currentLv + 1)
        SubUpgradeId.AIR_FREQUENCY -> save.copy(gold = save.gold - cost, airFrequencyLv = currentLv + 1)
        SubUpgradeId.AIR_TARGET_COUNT -> save.copy(gold = save.gold - cost, airTargetCountLv = currentLv + 1)
        SubUpgradeId.AIR_BOMB_AREA -> save.copy(gold = save.gold - cost, airBombAreaLv = currentLv + 1)
        SubUpgradeId.WALL_MAX_HP -> save.copy(gold = save.gold - cost, wallMaxHpLv = currentLv + 1)
        SubUpgradeId.WALL_ARMOR -> save.copy(gold = save.gold - cost, wallArmorLv = currentLv + 1)
        SubUpgradeId.WALL_DAMAGE_REDUCTION -> save.copy(gold = save.gold - cost, wallDamageReductionLv = currentLv + 1)
        SubUpgradeId.WALL_SHIELD_CAPACITY -> save.copy(gold = save.gold - cost, wallShieldCapacityLv = currentLv + 1)
    }
    engine.syncStatsFromSave(updated)
    persistSaveAndSync(updated)
}

fun GameViewModel.performCheapestFacilityUpgradeIfPossible(save: PlayerSaveEntity): PlayerSaveEntity {
    val candidates = FacilityType.entries.map { type ->
        val lv = ProgressionCatalog.getFacilityLevel(save, type)
        val cost = EconomyCalculator.calculateUpgradeCost(type.baseCost, lv)
        Triple(type, lv, cost)
    }.sortedBy { it.third }

    val cheapest = candidates.firstOrNull() ?: return save
    if (save.gold < cheapest.third) return save

    val updated = when (cheapest.first) {
        FacilityType.BARRACKS -> save.copy(gold = save.gold - cheapest.third, barracksLevel = cheapest.second + 1)
        FacilityType.ROCKET_SILO -> save.copy(gold = save.gold - cheapest.third, rocketSiloLevel = cheapest.second + 1)
        FacilityType.AIR_FLEET -> save.copy(gold = save.gold - cheapest.third, airFleetLevel = cheapest.second + 1)
        FacilityType.BORDER_WALL -> save.copy(gold = save.gold - cheapest.third, borderWallLevel = cheapest.second + 1)
    }
    engine.syncStatsFromSave(updated)
    return updated
}

fun GameViewModel.performPrestigePromotion() {
    val save = uiState.value.save
    val earnedPoints = EconomyCalculator.calculatePrestigePointsReward(save.currentWave)
    if (earnedPoints <= 0) {
        showToast("General Terfisi için en az 10. Dalga'ya ulaşmalısınız!")
        return
    }

    val prestigedSave = save.copy(
        gold = 250.0 + (save.permGoldProductionLv * 150.0),
        currentWave = 1,
        prestigeCount = save.prestigeCount + 1,
        promotionPoints = save.promotionPoints + earnedPoints,
        barracksLevel = 1,
        barracksSpawnRateLv = 1,
        barracksDamageLv = 1,
        barracksHealthLv = 1,
        barracksMaxUnitsLv = 1,
        rocketSiloLevel = 1,
        rocketDamageLv = 1,
        rocketFireRateLv = 1,
        rocketBlastRadiusLv = 1,
        rocketMultiTargetLv = 1,
        airFleetLevel = 1,
        airDamageLv = 1,
        airFrequencyLv = 1,
        airTargetCountLv = 1,
        airBombAreaLv = 1,
        borderWallLevel = 1,
        wallMaxHpLv = 1,
        wallArmorLv = 1,
        wallDamageReductionLv = 1,
        wallShieldCapacityLv = 1,
        cycleFirepowerBonusPct = 0,
        cycleShieldBonusPct = 0
    )

    engine.initializeForWave(prestigedSave, WavePhase.PREPARING)
    persistSaveAndSync(
        prestigedSave,
        "GENERAL TERFİSİ TAMAMLANDI! +$earnedPoints Terfi Puanı Kazanıldı!"
    )
}

fun GameViewModel.upgradePermanentPrestigeSkill(id: PrestigeUpgradeId) {
    val save = uiState.value.save
    val cost = ProgressionCatalog.getPrestigeUpgradeCost(save, id)
    if (save.promotionPoints < cost) {
        showToast("Yetersiz Terfi Puanı! Gereken: $cost TP")
        return
    }

    val updated = when (id) {
        PrestigeUpgradeId.PERM_DAMAGE -> save.copy(
            promotionPoints = save.promotionPoints - cost,
            permDamageLv = save.permDamageLv + 1
        )
        PrestigeUpgradeId.PERM_GOLD -> save.copy(
            promotionPoints = save.promotionPoints - cost,
            permGoldProductionLv = save.permGoldProductionLv + 1
        )
        PrestigeUpgradeId.PERM_BASE_HP -> save.copy(
            promotionPoints = save.promotionPoints - cost,
            permBaseHealthLv = save.permBaseHealthLv + 1
        )
        PrestigeUpgradeId.PERM_SUPPORT_SKILL -> save.copy(
            promotionPoints = save.promotionPoints - cost,
            permSupportSkillLv = save.permSupportSkillLv + 1
        )
        PrestigeUpgradeId.PERM_AUTOMATION -> save.copy(
            promotionPoints = save.promotionPoints - cost,
            permAutomationLv = save.permAutomationLv + 1
        )
    }
    engine.syncStatsFromSave(updated)
    persistSaveAndSync(updated, "${id.titleTr} Seviye Yükseltildi!")
}

fun GameViewModel.claimMissionReward(mission: MissionItem) {
    val save = uiState.value.save
    if (!mission.isCompleted || mission.isClaimed) return

    val existingIds = save.claimedMissionIdsCsv
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toMutableSet()
    existingIds.add(mission.id)

    val updated = save.copy(
        gold = save.gold + mission.rewardGold,
        totalGoldEarned = save.totalGoldEarned + mission.rewardGold,
        diamonds = save.diamonds + mission.rewardDiamonds,
        claimedMissionIdsCsv = existingIds.joinToString(",")
    )
    persistSaveAndSync(
        updated,
        "Görev Ödülü: +${EconomyCalculator.formatNumber(mission.rewardGold)} Altın & +${mission.rewardDiamonds} Elmas!"
    )
}

fun GameViewModel.selectOrPurchaseBaseSkin(skin: BaseSkinOption) {
    val save = uiState.value.save
    val ownedSet = save.ownedSkinsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()

    if (skin.id in ownedSet) {
        val updated = save.copy(selectedBaseSkinId = skin.id)
        persistSaveAndSync(updated, "${skin.nameTr} Görünümü Etkinleştirildi!")
        return
    }

    if (save.diamonds < skin.diamondPrice) {
        showToast("Yetersiz Elmas! Gereken: ${skin.diamondPrice} Elmas")
        return
    }

    ownedSet.add(skin.id)
    val updated = save.copy(
        diamonds = save.diamonds - skin.diamondPrice,
        ownedSkinsCsv = ownedSet.joinToString(","),
        selectedBaseSkinId = skin.id
    )
    persistSaveAndSync(updated, "${skin.nameTr} Satın Alındı ve Etkinleştirildi!")
}
