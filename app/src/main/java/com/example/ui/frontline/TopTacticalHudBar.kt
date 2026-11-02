package com.example.ui.frontline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.CombatSnapshot
import com.example.game.combat.WavePhase
import com.example.game.economy.EconomyCalculator
import com.example.game.progression.ProgressionCatalog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DiamondBlue
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.max

@Composable
fun TopTacticalHudBar(
    save: PlayerSaveEntity,
    combat: CombatSnapshot,
    passiveGoldPerSec: Double,
    onOpenStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rank = ProgressionCatalog.getPlayerRank(save)
    val hpRatio = (combat.baseCurrentHp / max(1.0, combat.baseMaxHp)).toFloat().coerceIn(0f, 1f)
    val shieldRatio = (combat.shieldCurrentHp / max(1.0, combat.shieldMaxHp)).toFloat().coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Rank, Gold (+/s), Diamonds, Wave & Store Button
        Surface(
            color = SlateBackground.copy(alpha = 0.86f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Rank Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TacticalGreen.copy(alpha = 0.2f))
                            .border(1.dp, TacticalGreen, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank.badgeCode,
                            style = MaterialTheme.typography.labelLarge,
                            color = TacticalGreen
                        )
                    }
                    Column {
                        Text(
                            text = rank.titleTr.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "DALGA ${combat.waveNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.testTag("hud_wave_text")
                        )
                    }
                }

                // Gold & Passive Rate
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${EconomyCalculator.formatNumber(save.gold)} AU",
                            style = MaterialTheme.typography.labelLarge,
                            color = EconomyGold,
                            modifier = Modifier.testTag("hud_gold_text")
                        )
                    }
                    Text(
                        text = "+${EconomyCalculator.formatNumber(passiveGoldPerSec)}/sn",
                        style = MaterialTheme.typography.labelSmall,
                        color = TacticalGreen
                    )
                }

                // Diamonds & Store Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = SlateSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, DiamondBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable(onClick = onOpenStore)
                            .testTag("open_store_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Mağaza",
                                tint = DiamondBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${save.diamonds}",
                                style = MaterialTheme.typography.labelLarge,
                                color = DiamondBlue
                            )
                        }
                    }
                }
            }
        }

        // Row 2: Base HP, Energy Shield & Wave Countdown / Enemy Progress
        Surface(
            color = SlateBackground.copy(alpha = 0.82f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Base Health Bar
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ÜS ZIRHI",
                                style = MaterialTheme.typography.labelSmall,
                                color = TacticalGreen
                            )
                            Text(
                                text = "${EconomyCalculator.formatNumber(combat.baseCurrentHp)} / ${EconomyCalculator.formatNumber(combat.baseMaxHp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { hpRatio },
                            color = if (hpRatio > 0.35f) TacticalGreen else DangerRed,
                            trackColor = SlateSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Energy Shield Bar
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ENERJİ KALKANI",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShieldCyan
                            )
                            Text(
                                text = "${EconomyCalculator.formatNumber(combat.shieldCurrentHp)} / ${EconomyCalculator.formatNumber(combat.shieldMaxHp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShieldCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { shieldRatio },
                            color = ShieldCyan,
                            trackColor = SlateSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }

                // Status / Countdown Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = when (combat.wavePhase) {
                        WavePhase.PREPARING -> String.format(
                            Locale.US,
                            "HAZIRLIK SÜRESİ: %.1f sn",
                            combat.prepareCountdownSec
                        )
                        WavePhase.FIGHTING -> "AKTİF ÇATIŞMA — DÜŞMAN: ${combat.enemiesDefeatedInWave}/${combat.totalWaveEnemies}"
                        WavePhase.REWARD_SELECTION -> "DALGA TAMAMLANDI — ÖDÜL SEÇİN"
                        WavePhase.COMPLETED -> "SONRAKİ DALGAYA GEÇİLİYOR"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (combat.wavePhase == WavePhase.PREPARING) EconomyGold else TextSecondary
                    )

                    if (combat.activeTargetingSkill != null) {
                        Text(
                            text = "HEDEF SEÇİN: SAVAŞ ALANINA DOKUNUN",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed
                        )
                    }
                }
            }
        }
    }
}
