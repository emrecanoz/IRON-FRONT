package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.GameRepository
import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.CombatSnapshot
import com.example.game.combat.TacticalSkillType
import com.example.game.combat.WavePhase
import com.example.game.combat.WaveRewardOption
import com.example.game.combat.WaveRewardType
import com.example.game.economy.EconomyCalculator
import com.example.game.economy.OfflineEarningsReport
import com.example.game.engine.GameEngine
import com.example.monetization.BillingCatalog
import com.example.monetization.BillingProductConfig
import com.example.monetization.RewardedAdPlacement
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

enum class MainNavTab {
    FRONTLINE,
    FACTORIES,
    HEADQUARTERS
}

data class ActiveAdSimulation(
    val placement: RewardedAdPlacement,
    val remainingSeconds: Int = 3,
    val isCompleted: Boolean = false
)

data class GameUiState(
    val isLoaded: Boolean = false,
    val save: PlayerSaveEntity = PlayerSaveEntity(),
    val combat: CombatSnapshot = CombatSnapshot(),
    val currentTab: MainNavTab = MainNavTab.FRONTLINE,
    val isPaused: Boolean = false,
    val gameSpeedMultiplier: Int = 1,
    val passiveGoldPerSec: Double = 12.0,
    val offlineReport: OfflineEarningsReport? = null,
    val showStoreDialog: Boolean = false,
    val activeAdSimulation: ActiveAdSimulation? = null,
    val billingProducts: List<BillingProductConfig> = BillingCatalog.queryAvailableProducts(),
    val statusToastMessage: String? = null
)

class GameViewModel(
    val repository: GameRepository
) : ViewModel() {

    internal val engine = GameEngine()
    internal val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var periodicSaveAccumulatorSec = 0f
    private var autoUpgradeAccumulatorSec = 0f
    internal var bonusSupplyClaimedForWave = -1

    init {
        initializeGame()
    }

    private fun initializeGame() {
        viewModelScope.launch {
            val loadedSave = repository.getOrCreateSave()
            val now = System.currentTimeMillis()
            val offlineReport = EconomyCalculator.evaluateOfflineEarnings(loadedSave, now)
            val passiveRate = EconomyCalculator.calculateTotalPassiveGoldPerSec(loadedSave, now)

            engine.initializeForWave(loadedSave, WavePhase.PREPARING)

            val updatedSave = loadedSave.copy(
                lastSavedWallTimeMs = now,
                lastSavedPassiveRatePerSec = passiveRate
            )
            repository.saveState(updatedSave)

            _uiState.update {
                it.copy(
                    isLoaded = true,
                    save = updatedSave,
                    combat = engine.snapshot,
                    passiveGoldPerSec = passiveRate,
                    offlineReport = offlineReport
                )
            }

            startSimulationLoop()
        }
    }

    private fun startSimulationLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            val tickMs = 33L // ~30 simulation steps per second
            val baseDtSec = tickMs / 1000f
            while (isActive) {
                delay(tickMs)
                val state = _uiState.value
                if (!state.isLoaded || state.isPaused) continue

                val speedMult = state.gameSpeedMultiplier.toFloat()
                val effectiveDt = baseDtSec * speedMult
                val now = System.currentTimeMillis()

                val events = engine.step(effectiveDt, state.save, now)
                var nextSave = state.save

                if (events.goldEarnedDelta > 0.0 ||
                    events.infantryKilledDelta > 0 ||
                    events.tanksKilledDelta > 0 ||
                    events.aircraftKilledDelta > 0 ||
                    events.bossesKilledDelta > 0
                ) {
                    nextSave = nextSave.copy(
                        gold = nextSave.gold + events.goldEarnedDelta,
                        totalGoldEarned = nextSave.totalGoldEarned + events.goldEarnedDelta,
                        totalInfantryKilled = nextSave.totalInfantryKilled + events.infantryKilledDelta,
                        totalTanksKilled = nextSave.totalTanksKilled + events.tanksKilledDelta,
                        totalAircraftKilled = nextSave.totalAircraftKilled + events.aircraftKilledDelta,
                        totalBossesKilled = nextSave.totalBossesKilled + events.bossesKilledDelta
                    )
                }

                // Automation module auto-upgrades cheapest facility
                if (nextSave.autoUpgradeEnabled &&
                    (nextSave.permanentAutomationPurchased || nextSave.permAutomationLv > 0)
                ) {
                    autoUpgradeAccumulatorSec += baseDtSec
                    if (autoUpgradeAccumulatorSec >= 1.5f) {
                        autoUpgradeAccumulatorSec = 0f
                        nextSave = performCheapestFacilityUpgradeIfPossible(nextSave)
                    }
                }

                val passiveRate = EconomyCalculator.calculateTotalPassiveGoldPerSec(nextSave, now)

                periodicSaveAccumulatorSec += baseDtSec
                if (events.shouldPersistSave || periodicSaveAccumulatorSec >= 5.0f) {
                    periodicSaveAccumulatorSec = 0f
                    nextSave = nextSave.copy(
                        lastSavedWallTimeMs = now,
                        lastSavedPassiveRatePerSec = passiveRate
                    )
                    repository.saveState(nextSave)
                }

                _uiState.update {
                    it.copy(
                        save = nextSave,
                        combat = engine.snapshot,
                        passiveGoldPerSec = passiveRate
                    )
                }
            }
        }
    }

    fun onAppBackgroundedOrPaused() {
        viewModelScope.launch {
            val current = _uiState.value
            if (!current.isLoaded) return@launch
            val now = System.currentTimeMillis()
            val passive = EconomyCalculator.calculateTotalPassiveGoldPerSec(current.save, now)
            val updated = current.save.copy(
                lastSavedWallTimeMs = now,
                lastSavedPassiveRatePerSec = passive
            )
            repository.saveState(updated)
            _uiState.update { it.copy(save = updated) }
        }
    }

    fun selectTab(tab: MainNavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
        onAppBackgroundedOrPaused()
    }

    fun toggleGameSpeed() {
        val save = _uiState.value.save
        val canUse2x = save.unlockedSpeed2x ||
            save.starterPackPurchased ||
            save.adFreePackPurchased ||
            save.highestWaveReached >= 3

        if (!canUse2x) {
            if (save.diamonds >= 15) {
                val updated = save.copy(
                    diamonds = save.diamonds - 15,
                    unlockedSpeed2x = true
                )
                persistSaveAndSync(updated, "2x Oyun Hızı Kilidi Açıldı!")
                _uiState.update { it.copy(gameSpeedMultiplier = 2) }
            } else {
                showToast("2x Hız 3. Dalgada veya 15 Elmas ile açılır!")
            }
            return
        }

        val nextSpeed = if (_uiState.value.gameSpeedMultiplier == 1) 2 else 1
        _uiState.update { it.copy(gameSpeedMultiplier = nextSpeed) }
    }

    fun toggleAutoUpgrade() {
        val save = _uiState.value.save
        if (!save.permanentAutomationPurchased && save.permAutomationLv == 0) {
            showToast("Otomasyon için Terfi Ağı veya Mağaza'dan Otomasyon Modülü açın.")
            return
        }
        val updated = save.copy(autoUpgradeEnabled = !save.autoUpgradeEnabled)
        persistSaveAndSync(
            updated,
            if (updated.autoUpgradeEnabled) "Otonom Tesis Geliştirme: AKTİF" else "Otonom Tesis Geliştirme: KAPALI"
        )
    }

    fun onTacticalSkillButtonClicked(skill: TacticalSkillType) {
        val save = _uiState.value.save
        if (save.currentWave < skill.unlockWave && save.highestWaveReached < skill.unlockWave) {
            showToast("${skill.titleTr} Dalga ${skill.unlockWave}'de açılır!")
            return
        }
        if (!skill.requiresTargetTap) {
            engine.triggerShieldOvercharge(save.permSupportSkillLv)
            _uiState.update { it.copy(combat = engine.snapshot) }
        } else {
            engine.selectTargetingSkill(skill)
            _uiState.update { it.copy(combat = engine.snapshot) }
        }
    }

    fun onBattlefieldTapped(normX: Float, normY: Float) {
        val save = _uiState.value.save
        val executed = engine.executeBattlefieldTap(normX, normY, save)
        if (executed) {
            _uiState.update { it.copy(combat = engine.snapshot) }
        }
    }

    fun selectWaveReward(option: WaveRewardOption) {
        val state = _uiState.value
        if (state.combat.wavePhase != WavePhase.REWARD_SELECTION) return

        val nextWave = state.save.currentWave + 1
        val highestWave = max(state.save.highestWaveReached, nextWave)

        val updatedSave = when (option.type) {
            WaveRewardType.FIREPOWER -> state.save.copy(
                currentWave = nextWave,
                highestWaveReached = highestWave,
                cycleFirepowerBonusPct = state.save.cycleFirepowerBonusPct + option.bonusPctOrGold.toInt()
            )
            WaveRewardType.ENERGY_SHIELD -> state.save.copy(
                currentWave = nextWave,
                highestWaveReached = highestWave,
                cycleShieldBonusPct = state.save.cycleShieldBonusPct + option.bonusPctOrGold.toInt()
            )
            WaveRewardType.TREASURE_VAULT -> state.save.copy(
                currentWave = nextWave,
                highestWaveReached = highestWave,
                gold = state.save.gold + option.bonusPctOrGold,
                totalGoldEarned = state.save.totalGoldEarned + option.bonusPctOrGold
            )
        }

        engine.initializeForWave(updatedSave, WavePhase.PREPARING)
        persistSaveAndSync(updatedSave, "${option.titleTr} Seçildi — Dalga $nextWave Başlıyor!")
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(statusToastMessage = null) }
    }

    internal fun showToast(msg: String) {
        _uiState.update { it.copy(statusToastMessage = msg) }
    }

    internal fun persistSaveAndSync(updatedSave: PlayerSaveEntity, toastMsg: String? = null) {
        val now = System.currentTimeMillis()
        val passiveRate = EconomyCalculator.calculateTotalPassiveGoldPerSec(updatedSave, now)
        val stampedSave = updatedSave.copy(
            lastSavedWallTimeMs = now,
            lastSavedPassiveRatePerSec = passiveRate
        )
        _uiState.update {
            it.copy(
                save = stampedSave,
                combat = engine.snapshot,
                passiveGoldPerSec = passiveRate,
                statusToastMessage = toastMsg ?: it.statusToastMessage
            )
        }
        viewModelScope.launch {
            repository.saveState(stampedSave)
        }
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(repository) as T
        }
    }
}
