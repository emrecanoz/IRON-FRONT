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
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.game.economy.EconomyCalculator
import com.example.game.economy.OfflineEarningsReport
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OfflineEarningsDialog(
    report: OfflineEarningsReport,
    adFreeOwned: Boolean,
    onClaimNormal: () -> Unit,
    onClaimDoubleAd: () -> Unit
) {
    Dialog(
        onDismissRequest = onClaimNormal,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, EconomyGold),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offline_earnings_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = EconomyGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, EconomyGold)
                ) {
                    Text(
                        text = "ÇEVRİMDIŞI LOJİSTİK RAPORU",
                        style = MaterialTheme.typography.labelLarge,
                        color = EconomyGold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "KOMUTAN DÖNDÜ!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )

                Text(
                    text = "Üsten uzak kaldığınız süre boyunca fabrikalarınız pasif altın üretimine devam etti (Maksimum 3 saat):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Surface(
                    color = SlateCardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hesaplanan Süre:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                text = EconomyCalculator.formatDuration(report.cappedSeconds),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Üretim Hızı:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                text = "${EconomyCalculator.formatNumber(report.passiveRatePerSec)} AU/sn",
                                style = MaterialTheme.typography.labelLarge,
                                color = TacticalGreen
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Standart Ödül:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                text = "+${EconomyCalculator.formatNumber(report.baseRewardGold)} AU",
                                style = MaterialTheme.typography.titleMedium,
                                color = EconomyGold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("2x Bonuslu Ödül:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                text = "+${EconomyCalculator.formatNumber(report.doubleAdRewardGold)} AU",
                                style = MaterialTheme.typography.titleMedium,
                                color = TacticalGreen
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClaimNormal,
                        border = BorderStroke(1.dp, EconomyGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("claim_offline_normal_button")
                    ) {
                        Text(
                            text = "Normal Topla",
                            color = EconomyGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onClaimDoubleAd,
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .testTag("claim_offline_double_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (adFreeOwned) "VIP 2x Al" else "Reklam İzle — 2x Al",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
