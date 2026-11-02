package com.example.ui.frontline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.game.combat.WaveRewardOption
import com.example.game.combat.WaveRewardType
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WaveRewardSelectionOverlay(
    waveNumber: Int,
    options: List<WaveRewardOption>,
    onSelectOption: (WaveRewardOption) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9090D16))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, TacticalGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = TacticalGreen.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, TacticalGreen)
                ) {
                    Text(
                        text = "DALGA $waveNumber ZAFERİ",
                        style = MaterialTheme.typography.labelLarge,
                        color = TacticalGreen,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "TAKTİKSEL İKMAL SEÇİMİ",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Bir sonraki dalga başlamadan önce komuta üssünüz için 1 taktiksel destek kartı seçin:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                options.forEach { option ->
                    val cardAccent = when (option.type) {
                        WaveRewardType.FIREPOWER -> DangerRed
                        WaveRewardType.ENERGY_SHIELD -> ShieldCyan
                        WaveRewardType.TREASURE_VAULT -> EconomyGold
                    }
                    val cardIcon = when (option.type) {
                        WaveRewardType.FIREPOWER -> Icons.Default.Whatshot
                        WaveRewardType.ENERGY_SHIELD -> Icons.Default.Security
                        WaveRewardType.TREASURE_VAULT -> Icons.Default.Bolt
                    }

                    Surface(
                        color = SlateCardBg,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, cardAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectOption(option) }
                            .testTag("reward_card_${option.type.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(cardAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cardIcon,
                                    contentDescription = option.titleTr,
                                    tint = cardAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.type.badgeTr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cardAccent
                                )
                                Text(
                                    text = option.titleTr,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = option.descriptionTr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = option.valueSummaryTr,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = cardAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
