package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.GameDatabase
import com.example.data.local.GameRepository
import com.example.ui.*
import com.example.ui.factories.FactoriesScreen
import com.example.ui.frontline.FrontlineScreen
import com.example.ui.headquarters.HeadquartersScreen
import com.example.ui.monetization.OfflineEarningsDialog
import com.example.ui.monetization.RewardedAdSimulationDialog
import com.example.ui.monetization.StoreAndMonetizationDialog
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                IronFrontApp()
            }
        }
    }
}

@Composable
fun IronFrontApp() {
    val context = LocalContext.current
    val repository = remember(context) {
        val db = GameDatabase.getInstance(context)
        GameRepository(db.gameDao())
    }
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Lifecycle observer to persist state & timestamp whenever app goes to background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgroundedOrPaused()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // BackHandler on secondary screens to return to Frontline
    BackHandler(enabled = uiState.currentTab != MainNavTab.FRONTLINE) {
        viewModel.selectTab(MainNavTab.FRONTLINE)
    }

    // Auto-dismiss status toast after 2.6s
    LaunchedEffect(uiState.statusToastMessage) {
        if (uiState.statusToastMessage != null) {
            delay(2600L)
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = SlateBackground,
        bottomBar = {
            NavigationBar(
                containerColor = SlateSurface,
                contentColor = TextPrimary
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == MainNavTab.FRONTLINE,
                    onClick = { viewModel.selectTab(MainNavTab.FRONTLINE) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == MainNavTab.FRONTLINE) {
                                Icons.Filled.Public
                            } else {
                                Icons.Outlined.Public
                            },
                            contentDescription = "Cephe"
                        )
                    },
                    label = { Text("Cephe") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TacticalGreen,
                        selectedTextColor = TacticalGreen,
                        indicatorColor = TacticalGreen.copy(alpha = 0.18f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_frontline")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == MainNavTab.FACTORIES,
                    onClick = { viewModel.selectTab(MainNavTab.FACTORIES) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == MainNavTab.FACTORIES) {
                                Icons.Filled.Factory
                            } else {
                                Icons.Outlined.Factory
                            },
                            contentDescription = "Fabrikalar"
                        )
                    },
                    label = { Text("Fabrikalar") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EconomyGold,
                        selectedTextColor = EconomyGold,
                        indicatorColor = EconomyGold.copy(alpha = 0.18f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_factories")
                )

                NavigationBarItem(
                    selected = uiState.currentTab == MainNavTab.HEADQUARTERS,
                    onClick = { viewModel.selectTab(MainNavTab.HEADQUARTERS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == MainNavTab.HEADQUARTERS) {
                                Icons.Filled.MilitaryTech
                            } else {
                                Icons.Outlined.MilitaryTech
                            },
                            contentDescription = "Karargâh"
                        )
                    },
                    label = { Text("Karargâh") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TacticalGreen,
                        selectedTextColor = TacticalGreen,
                        indicatorColor = TacticalGreen.copy(alpha = 0.18f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_headquarters")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                MainNavTab.FRONTLINE -> {
                    FrontlineScreen(
                        save = uiState.save,
                        combat = uiState.combat,
                        passiveGoldPerSec = uiState.passiveGoldPerSec,
                        isPaused = uiState.isPaused,
                        gameSpeedMultiplier = uiState.gameSpeedMultiplier,
                        onBattlefieldTap = viewModel::onBattlefieldTapped,
                        onSkillClick = viewModel::onTacticalSkillButtonClicked,
                        onToggleSpeed = viewModel::toggleGameSpeed,
                        onTogglePause = viewModel::togglePause,
                        onQuickUpgradeFacility = viewModel::upgradeFacilityMain,
                        onSelectWaveReward = viewModel::selectWaveReward,
                        onOpenStore = viewModel::openStoreDialog
                    )
                }

                MainNavTab.FACTORIES -> {
                    FactoriesScreen(
                        save = uiState.save,
                        passiveGoldPerSec = uiState.passiveGoldPerSec,
                        onUpgradeFacilityMain = viewModel::upgradeFacilityMain,
                        onUpgradeSubAttribute = viewModel::upgradeSubAttribute,
                        onToggleAutoUpgrade = viewModel::toggleAutoUpgrade,
                        onTriggerRewardedBoost = viewModel::startRewardedAdFlow
                    )
                }

                MainNavTab.HEADQUARTERS -> {
                    HeadquartersScreen(
                        save = uiState.save,
                        onPerformPrestige = viewModel::performPrestigePromotion,
                        onUpgradePermanentSkill = viewModel::upgradePermanentPrestigeSkill,
                        onClaimMission = viewModel::claimMissionReward,
                        onSelectOrBuySkin = viewModel::selectOrPurchaseBaseSkin
                    )
                }
            }

            // Floating Status Toast Banner
            AnimatedVisibility(
                visible = uiState.statusToastMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 88.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    color = SlateBackground.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TacticalGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.statusToastMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // 1. Offline Earnings Modal (Max 3 Hours, Normal vs 2x Ad)
            val offlineReport = uiState.offlineReport
            if (offlineReport != null && uiState.activeAdSimulation == null) {
                OfflineEarningsDialog(
                    report = offlineReport,
                    adFreeOwned = uiState.save.adFreePackPurchased,
                    onClaimNormal = viewModel::claimOfflineEarningsNormal,
                    onClaimDoubleAd = viewModel::claimOfflineEarningsDoubleWithAd
                )
            }

            // 2. Store & Monetization Dialog
            if (uiState.showStoreDialog) {
                StoreAndMonetizationDialog(
                    save = uiState.save,
                    products = uiState.billingProducts,
                    onTriggerRewardedAd = viewModel::startRewardedAdFlow,
                    onPurchaseProduct = viewModel::purchaseStoreProduct,
                    onRestorePurchases = viewModel::restorePurchases,
                    onDismiss = viewModel::closeStoreDialog
                )
            }

            // 3. Rewarded Ad Verification Simulation Dialog
            val activeAd = uiState.activeAdSimulation
            if (activeAd != null) {
                RewardedAdSimulationDialog(
                    simulation = activeAd,
                    onConfirmReward = viewModel::confirmCompletedAdReward,
                    onCancelAd = viewModel::cancelAdFlow
                )
            }
        }
    }
}
