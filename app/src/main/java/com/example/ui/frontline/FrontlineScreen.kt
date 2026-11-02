package com.example.ui.frontline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.CombatSnapshot
import com.example.game.combat.TacticalSkillType
import com.example.game.combat.WavePhase
import com.example.game.combat.WaveRewardOption
import com.example.game.progression.FacilityType
import com.example.game.rendering.IsometricBattlefieldCanvas
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextSecondary

@Composable
fun FrontlineScreen(
    save: PlayerSaveEntity,
    combat: CombatSnapshot,
    passiveGoldPerSec: Double,
    isPaused: Boolean,
    gameSpeedMultiplier: Int,
    onBattlefieldTap: (Float, Float) -> Unit,
    onSkillClick: (TacticalSkillType) -> Unit,
    onToggleSpeed: () -> Unit,
    onTogglePause: () -> Unit,
    onQuickUpgradeFacility: (FacilityType) -> Unit,
    onSelectWaveReward: (WaveRewardOption) -> Unit,
    onOpenStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Real-time 2.5D Isometric Military Base & Combat Canvas
        IsometricBattlefieldCanvas(
            snapshot = combat,
            save = save,
            onBattlefieldTap = onBattlefieldTap,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Tactical HUD Bar (Overlaying top cleanly)
        TopTacticalHudBar(
            save = save,
            combat = combat,
            passiveGoldPerSec = passiveGoldPerSec,
            onOpenStore = onOpenStore,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )

        // 3. Bottom Tactical Controls & Quick Facility Upgrade Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SlateBackground.copy(alpha = 0.88f),
                            SlateBackground.copy(alpha = 0.96f)
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tactical Support Skills + Speed + Pause Row
            TacticalControlBar(
                save = save,
                combat = combat,
                isPaused = isPaused,
                gameSpeedMultiplier = gameSpeedMultiplier,
                onSkillClick = onSkillClick,
                onToggleSpeed = onToggleSpeed,
                onTogglePause = onTogglePause
            )

            // Quick 4-Facility Upgrade Strip
            QuickFacilityUpgradeStrip(
                save = save,
                onUpgradeFacility = onQuickUpgradeFacility
            )
        }

        // 4. Paused Overlay Banner
        AnimatedVisibility(
            visible = isPaused && combat.wavePhase != WavePhase.REWARD_SELECTION,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = SlateBackground.copy(alpha = 0.92f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, EconomyGold),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OPERASYON DURAKLATILDI",
                        style = MaterialTheme.typography.headlineMedium,
                        color = EconomyGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Savaş alanı simülasyonu korundu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onTogglePause,
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalGreen),
                        modifier = Modifier.testTag("resume_game_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVAŞA DEVAM ET", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Wave Completion 3-Reward Tactical Selection Modal
        AnimatedVisibility(
            visible = combat.wavePhase == WavePhase.REWARD_SELECTION && combat.rewardOptions.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            WaveRewardSelectionOverlay(
                waveNumber = combat.waveNumber,
                options = combat.rewardOptions,
                onSelectOption = onSelectWaveReward
            )
        }
    }
}
