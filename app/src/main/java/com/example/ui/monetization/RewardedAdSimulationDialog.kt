package com.example.ui.monetization

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.ActiveAdSimulation
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RewardedAdSimulationDialog(
    simulation: ActiveAdSimulation,
    onConfirmReward: () -> Unit,
    onCancelAd: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancelAd,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateBackground),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.5.dp, TacticalGreen),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rewarded_ad_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ADMOB TEST REKLAM SİMÜLASYONU",
                    style = MaterialTheme.typography.labelLarge,
                    color = EconomyGold
                )
                Text(
                    text = simulation.placement.titleTr,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = if (simulation.isCompleted) {
                        "Reklam gösterimi başarıyla doğrulandı! Ödülünüzü alabilirsiniz."
                    } else {
                        "Ödül doğrulaması için kalan süre: ${simulation.remainingSeconds} saniye..."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelAd,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İptal Et", color = TextSecondary)
                    }

                    Button(
                        onClick = onConfirmReward,
                        enabled = simulation.isCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalGreen),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_ad_reward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (simulation.isCompleted) "ÖDÜLÜ AL" else "${simulation.remainingSeconds} sn",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
