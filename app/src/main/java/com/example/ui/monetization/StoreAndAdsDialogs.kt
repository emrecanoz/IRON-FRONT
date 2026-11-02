package com.example.ui.monetization

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.PlayerSaveEntity
import com.example.game.economy.EconomyCalculator
import com.example.monetization.BillingProductConfig
import com.example.monetization.RewardedAdPlacement
import com.example.monetization.StoreProductType
import com.example.ui.theme.DiamondBlue
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StoreAndMonetizationDialog(
    save: PlayerSaveEntity,
    products: List<BillingProductConfig>,
    onTriggerRewardedAd: (RewardedAdPlacement) -> Unit,
    onPurchaseProduct: (BillingProductConfig) -> Unit,
    onRestorePurchases: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD9090D16))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, DiamondBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = DiamondBlue)
                            Column {
                                Text(
                                    text = "İKMAL MERKEZİ & MAĞAZA",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Hazine: ${EconomyCalculator.formatNumber(save.gold)} AU • ${save.diamonds} Elmas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EconomyGold
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_store_button")) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextPrimary)
                        }
                    }

                    // Section A: Optional Rewarded Ads (AdMob Test Placements)
                    Text(
                        text = "ÜCRETSİZ TAKTİKSEL DESTEK (ÖDÜLLÜ REKLAM)",
                        style = MaterialTheme.typography.labelLarge,
                        color = TacticalGreen
                    )

                    listOf(
                        RewardedAdPlacement.PRODUCTION_BOOST_15M,
                        RewardedAdPlacement.DAMAGE_BOOST_10M,
                        RewardedAdPlacement.BONUS_SUPPLY_DROP
                    ).forEach { placement ->
                        Surface(
                            color = SlateCardBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, TacticalGreen.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTriggerRewardedAd(placement) }
                                .testTag("ad_placement_${placement.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = placement.titleTr,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = placement.descriptionTr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onTriggerRewardedAd(placement) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TacticalGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (save.adFreePackPurchased) "VIP AL" else "İZLE",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Section B: Google Play Billing Products
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KOMUTAN PAKETLERİ (GOOGLE PLAY BILLING)",
                            style = MaterialTheme.typography.labelLarge,
                            color = DiamondBlue
                        )
                        Surface(
                            color = SlateCardBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF475569)),
                            modifier = Modifier
                                .clickable(onClick = onRestorePurchases)
                                .testTag("restore_purchases_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Geri Yükle",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    products.forEach { product ->
                        val alreadyOwned = (product.type == StoreProductType.STARTER_PACK && save.starterPackPurchased) ||
                            (product.type == StoreProductType.PERMANENT_AUTOMATION && save.permanentAutomationPurchased) ||
                            (product.type == StoreProductType.AD_FREE_PACK && save.adFreePackPurchased)

                        Surface(
                            color = SlateCardBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (alreadyOwned) TacticalGreen else DiamondBlue.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !alreadyOwned) { onPurchaseProduct(product) }
                                .testTag("buy_product_${product.productId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.badgeTr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EconomyGold
                                    )
                                    Text(
                                        text = product.titleTr,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = product.subtitleTr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onPurchaseProduct(product) },
                                    enabled = !alreadyOwned,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DiamondBlue,
                                        disabledContainerColor = SlateSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (alreadyOwned) "AKTİF" else product.formattedPrice,
                                        color = if (alreadyOwned) TacticalGreen else Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
