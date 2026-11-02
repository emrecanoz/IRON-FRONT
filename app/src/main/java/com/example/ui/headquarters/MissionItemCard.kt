package com.example.ui.headquarters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.game.economy.EconomyCalculator
import com.example.game.progression.MissionItem
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MissionItemCard(
    mission: MissionItem,
    onClaimMission: (MissionItem) -> Unit
) {
    val progressRatio = (mission.currentProgress.toFloat() / mission.targetProgress.toFloat()).coerceIn(0f, 1f)
    Surface(
        color = SlateSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            when {
                mission.isClaimed -> Color(0xFF334155)
                mission.isCompleted -> EconomyGold
                else -> Color(0xFF1E293B)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mission.titleTr,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = mission.descriptionTr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressRatio },
                    color = if (mission.isCompleted) TacticalGreen else EconomyGold,
                    trackColor = SlateCardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "İlerleme: ${mission.currentProgress}/${mission.targetProgress} • Ödül: +${EconomyCalculator.formatNumber(mission.rewardGold)} AU & +${mission.rewardDiamonds} Elmas",
                    style = MaterialTheme.typography.labelSmall,
                    color = EconomyGold
                )
            }

            if (mission.isClaimed) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Tamamlandı",
                    tint = TacticalGreen
                )
            } else {
                Button(
                    onClick = { onClaimMission(mission) },
                    enabled = mission.isCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EconomyGold,
                        disabledContainerColor = SlateCardBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("claim_mission_${mission.id.lowercase()}")
                ) {
                    Text(
                        text = "ÖDÜL AL",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (mission.isCompleted) Color.Black else TextSecondary
                    )
                }
            }
        }
    }
}
