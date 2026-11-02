package com.example.ui.factories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PlayerSaveEntity
import com.example.game.economy.EconomyCalculator
import com.example.game.progression.FacilityType
import com.example.game.progression.ProgressionCatalog
import com.example.game.progression.SubUpgradeId
import com.example.monetization.RewardedAdPlacement
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FactoriesScreen(
    save: PlayerSaveEntity,
    passiveGoldPerSec: Double,
    onUpgradeFacilityMain: (FacilityType) -> Unit,
    onUpgradeSubAttribute: (SubUpgradeId) -> Unit,
    onToggleAutoUpgrade: () -> Unit,
    onTriggerRewardedBoost: (RewardedAdPlacement) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val prodBoostRemainingSec = ((save.productionBoostUntilMs - now) / 1000L).coerceAtLeast(0L)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("factories_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Economy & Automation Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, EconomyGold.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ASKERİ ENDÜSTRİ VE TESİSLER",
                                style = MaterialTheme.typography.labelLarge,
                                color = EconomyGold
                            )
                            Text(
                                text = "Mevcut Hazine: ${EconomyCalculator.formatNumber(save.gold)} AU",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Pasif Altın Üretimi: +${EconomyCalculator.formatNumber(passiveGoldPerSec)} / saniye",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TacticalGreen
                            )
                        }
                    }

                    // 15-Min 2x Production Boost Ad Button + Automation Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onTriggerRewardedBoost(RewardedAdPlacement.PRODUCTION_BOOST_15M) },
                            colors = ButtonDefaults.buttonColors(containerColor = EconomyGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("boost_production_15m_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (prodBoostRemainingSec > 0) {
                                    "2x Aktif (${EconomyCalculator.formatDuration(prodBoostRemainingSec)})"
                                } else {
                                    "15 Dk 2x Üretim"
                                },
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = SlateCardBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, TacticalGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onToggleAutoUpgrade() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoMode,
                                    contentDescription = "Otomasyon",
                                    tint = TacticalGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Oto-Geliştir",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Switch(
                                    checked = save.autoUpgradeEnabled,
                                    onCheckedChange = { onToggleAutoUpgrade() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TacticalGreen)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. All 4 Military Facilities & their 4 Sub-Upgrades
        items(FacilityType.entries) { facility ->
            FacilityUpgradeCard(
                facility = facility,
                save = save,
                onUpgradeMain = { onUpgradeFacilityMain(facility) },
                onUpgradeSub = onUpgradeSubAttribute
            )
        }
    }
}

@Composable
private fun FacilityUpgradeCard(
    facility: FacilityType,
    save: PlayerSaveEntity,
    onUpgradeMain: () -> Unit,
    onUpgradeSub: (SubUpgradeId) -> Unit
) {
    val mainLevel = ProgressionCatalog.getFacilityLevel(save, facility)
    val mainCost = EconomyCalculator.calculateUpgradeCost(facility.baseCost, mainLevel)
    val mainProduction = EconomyCalculator.calculateFacilityProduction(facility.baseProduction, mainLevel)
    val canAffordMain = save.gold >= mainCost
    val visualTierText = ProgressionCatalog.getVisualTierText(mainLevel)

    val icon = when (facility) {
        FacilityType.BARRACKS -> Icons.Default.Groups
        FacilityType.ROCKET_SILO -> Icons.Default.RocketLaunch
        FacilityType.AIR_FLEET -> Icons.Default.Flight
        FacilityType.BORDER_WALL -> Icons.Default.Shield
    }

    val subUpgrades = SubUpgradeId.entries.filter { it.facility == facility }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, facility.accentColor.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(facility.accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = facility.titleTr,
                            tint = facility.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = facility.titleTr,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Surface(
                                color = facility.accentColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "SV. $mainLevel",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = facility.accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = facility.subtitleTr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "$visualTierText • Katkı: +${EconomyCalculator.formatNumber(mainProduction)} AU/sn",
                            style = MaterialTheme.typography.labelSmall,
                            color = facility.accentColor
                        )
                    }
                }

                Button(
                    onClick = onUpgradeMain,
                    enabled = canAffordMain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = facility.accentColor,
                        disabledContainerColor = SlateCardBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("upgrade_main_${facility.name.lowercase()}")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "YÜKSELT",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (canAffordMain) Color.Black else TextSecondary
                        )
                        Text(
                            text = "${EconomyCalculator.formatNumber(mainCost)} AU",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (canAffordMain) Color.Black else TextSecondary
                        )
                    }
                }
            }

            // 4 Sub-Upgrades Grid (2 rows of 2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                subUpgrades.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { sub ->
                            val subLv = ProgressionCatalog.getSubUpgradeLevel(save, sub)
                            val subCost = EconomyCalculator.calculateUpgradeCost(sub.baseCost, subLv)
                            val canAffordSub = save.gold >= subCost

                            Surface(
                                color = SlateCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (canAffordSub) facility.accentColor.copy(alpha = 0.55f) else Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpgradeSub(sub) }
                                    .testTag("upgrade_sub_${sub.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = sub.titleTr,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Sv.$subLv",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = facility.accentColor
                                        )
                                    }
                                    Text(
                                        text = sub.descriptionTr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "${EconomyCalculator.formatNumber(subCost)} AU",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (canAffordSub) EconomyGold else TextSecondary
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
