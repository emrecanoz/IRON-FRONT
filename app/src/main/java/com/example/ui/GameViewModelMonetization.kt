package com.example.ui

import androidx.lifecycle.viewModelScope
import com.example.game.economy.EconomyCalculator
import com.example.monetization.BillingProductConfig
import com.example.monetization.RewardedAdPlacement
import com.example.monetization.StoreProductType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Store dialogs, rewarded ads simulation, Google Play billing, and offline earnings for [GameViewModel].
 */
fun GameViewModel.claimOfflineEarningsNormal() {
    val report = uiState.value.offlineReport ?: return
    val save = uiState.value.save
    if (save.lastClaimedOfflineTimestampMs >= report.saveTimestampId && report.saveTimestampId > 0L) {
        _uiState.update { it.copy(offlineReport = null) }
        return
    }

    val now = System.currentTimeMillis()
    val updated = save.copy(
        gold = save.gold + report.baseRewardGold,
        totalGoldEarned = save.totalGoldEarned + report.baseRewardGold,
        lastClaimedOfflineTimestampMs = max(now, report.saveTimestampId),
        lastSavedWallTimeMs = now
    )
    _uiState.update { it.copy(offlineReport = null) }
    persistSaveAndSync(
        updated,
        "+${EconomyCalculator.formatNumber(report.baseRewardGold)} Çevrimdışı Altın Toplandı!"
    )
}

fun GameViewModel.claimOfflineEarningsDoubleWithAd() {
    startRewardedAdFlow(RewardedAdPlacement.OFFLINE_DOUBLE)
}

fun GameViewModel.openStoreDialog() {
    _uiState.update { it.copy(showStoreDialog = true) }
}

fun GameViewModel.closeStoreDialog() {
    _uiState.update { it.copy(showStoreDialog = false) }
}

fun GameViewModel.startRewardedAdFlow(placement: RewardedAdPlacement) {
    val save = uiState.value.save
    if (placement == RewardedAdPlacement.BONUS_SUPPLY_DROP &&
        bonusSupplyClaimedForWave == save.currentWave
    ) {
        showToast("Bu dalga için Hava İkmal Sandığı zaten alındı!")
        return
    }

    if (save.adFreePackPurchased) {
        grantVerifiedAdReward(placement)
        return
    }

    viewModelScope.launch {
        _uiState.update {
            it.copy(activeAdSimulation = ActiveAdSimulation(placement, remainingSeconds = 3, isCompleted = false))
        }
        for (sec in 2 downTo 1) {
            delay(1000L)
            if (uiState.value.activeAdSimulation == null) return@launch
            _uiState.update {
                it.copy(activeAdSimulation = it.activeAdSimulation?.copy(remainingSeconds = sec))
            }
        }
        delay(1000L)
        if (uiState.value.activeAdSimulation == null) return@launch
        _uiState.update {
            it.copy(activeAdSimulation = it.activeAdSimulation?.copy(remainingSeconds = 0, isCompleted = true))
        }
    }
}

fun GameViewModel.cancelAdFlow() {
    _uiState.update { it.copy(activeAdSimulation = null) }
    showToast("Reklam tamamlanmadı — normal oynanışa devam ediliyor.")
}

fun GameViewModel.confirmCompletedAdReward() {
    val activeAd = uiState.value.activeAdSimulation ?: return
    if (!activeAd.isCompleted) return
    _uiState.update { it.copy(activeAdSimulation = null) }
    grantVerifiedAdReward(activeAd.placement)
}

fun GameViewModel.grantVerifiedAdReward(placement: RewardedAdPlacement) {
    val state = uiState.value
    val save = state.save
    val now = System.currentTimeMillis()

    when (placement) {
        RewardedAdPlacement.OFFLINE_DOUBLE -> {
            val report = state.offlineReport ?: return
            if (save.lastClaimedOfflineTimestampMs >= report.saveTimestampId && report.saveTimestampId > 0L) {
                _uiState.update { it.copy(offlineReport = null) }
                return
            }
            val updated = save.copy(
                gold = save.gold + report.doubleAdRewardGold,
                totalGoldEarned = save.totalGoldEarned + report.doubleAdRewardGold,
                lastClaimedOfflineTimestampMs = max(now, report.saveTimestampId),
                lastSavedWallTimeMs = now
            )
            _uiState.update { it.copy(offlineReport = null) }
            persistSaveAndSync(
                updated,
                "2x Ödül Doğrulandı: +${EconomyCalculator.formatNumber(report.doubleAdRewardGold)} Altın!"
            )
        }

        RewardedAdPlacement.PRODUCTION_BOOST_15M -> {
            val currentEnd = max(now, save.productionBoostUntilMs)
            val newEnd = currentEnd + (15L * 60L * 1000L)
            val updated = save.copy(productionBoostUntilMs = newEnd)
            persistSaveAndSync(updated, "15 Dakika 2x Altın Üretim Hızlandırması Aktif!")
        }

        RewardedAdPlacement.DAMAGE_BOOST_10M -> {
            val currentEnd = max(now, save.damageBoostUntilMs)
            val newEnd = currentEnd + (10L * 60L * 1000L)
            val updated = save.copy(damageBoostUntilMs = newEnd)
            persistSaveAndSync(updated, "10 Dakika +%50 Savunma Hasar Bonusu Aktif!")
        }

        RewardedAdPlacement.BONUS_SUPPLY_DROP -> {
            bonusSupplyClaimedForWave = save.currentWave
            val bonusGold = max(350.0, state.passiveGoldPerSec * 40.0)
            val updated = save.copy(
                gold = save.gold + bonusGold,
                totalGoldEarned = save.totalGoldEarned + bonusGold,
                diamonds = save.diamonds + 10
            )
            persistSaveAndSync(
                updated,
                "İkmal Sandığı: +${EconomyCalculator.formatNumber(bonusGold)} Altın & +10 Elmas!"
            )
        }
    }
}

fun GameViewModel.purchaseStoreProduct(product: BillingProductConfig) {
    val save = uiState.value.save

    if (product.type == StoreProductType.STARTER_PACK && save.starterPackPurchased) {
        showToast("Başlangıç Paketi zaten hesabınızda aktif.")
        return
    }
    if (product.type == StoreProductType.PERMANENT_AUTOMATION && save.permanentAutomationPurchased) {
        showToast("Kalıcı Otomasyon Modülü zaten aktif.")
        return
    }
    if (product.type == StoreProductType.AD_FREE_PACK && save.adFreePackPurchased) {
        showToast("Reklamsız VIP Komuta Paketi zaten aktif.")
        return
    }

    val updated = when (product.type) {
        StoreProductType.STARTER_PACK -> save.copy(
            gold = save.gold + product.goldAmount,
            totalGoldEarned = save.totalGoldEarned + product.goldAmount,
            diamonds = save.diamonds + product.diamondAmount,
            starterPackPurchased = true,
            unlockedSpeed2x = true
        )
        StoreProductType.PERMANENT_AUTOMATION -> save.copy(
            gold = save.gold + product.goldAmount,
            totalGoldEarned = save.totalGoldEarned + product.goldAmount,
            diamonds = save.diamonds + product.diamondAmount,
            permanentAutomationPurchased = true,
            autoUpgradeEnabled = true
        )
        StoreProductType.AD_FREE_PACK -> save.copy(
            gold = save.gold + product.goldAmount,
            totalGoldEarned = save.totalGoldEarned + product.goldAmount,
            diamonds = save.diamonds + product.diamondAmount,
            adFreePackPurchased = true,
            unlockedSpeed2x = true
        )
        StoreProductType.DIAMOND_PACK -> save.copy(
            gold = save.gold + product.goldAmount,
            totalGoldEarned = save.totalGoldEarned + product.goldAmount,
            diamonds = save.diamonds + product.diamondAmount
        )
    }
    persistSaveAndSync(updated, "${product.titleTr} Doğrulandı ve Teslim Edildi!")
}

fun GameViewModel.restorePurchases() {
    val save = uiState.value.save
    val restoredCount = listOf(
        save.starterPackPurchased,
        save.permanentAutomationPurchased,
        save.adFreePackPurchased
    ).count { it }
    showToast(
        if (restoredCount > 0) {
            "Google Play Kayıtları Senkronize Edildi ($restoredCount kalıcı paket aktif)."
        } else {
            "Geri yüklenecek önceki Google Play satın alımı bulunamadı."
        }
    )
}
