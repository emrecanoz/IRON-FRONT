package com.example.ui.headquarters

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PlayerSaveEntity
import com.example.game.economy.EconomyCalculator
import com.example.game.progression.BaseSkinOption
import com.example.game.progression.MissionItem
import com.example.game.progression.PrestigeUpgradeId
import com.example.game.progression.ProgressionCatalog
import com.example.ui.theme.DiamondBlue
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.PrestigePurple
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HeadquartersScreen(
    save: PlayerSaveEntity,
    onPerformPrestige: () -> Unit,
    onUpgradePermanentSkill: (PrestigeUpgradeId) -> Unit,
    onClaimMission: (MissionItem) -> Unit,
    onSelectOrBuySkin: (BaseSkinOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val rank = ProgressionCatalog.getPlayerRank(save)
    val missions = ProgressionCatalog.buildMissions(save)
    val potentialPrestigePoints = EconomyCalculator.calculatePrestigePointsReward(save.currentWave)
    val ownedSkins = save.ownedSkinsCsv.split(",").map { it.trim() }.toSet()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("headquarters_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Commander Profile & Combat Telemetry Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, TacticalGreen.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TacticalGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = TacticalGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KARARGÂH KOMUTA MERKEZİ",
                                style = MaterialTheme.typography.labelLarge,
                                color = TacticalGreen
                            )
                            Text(
                                text = "Rütbe: ${rank.titleTr} (${rank.badgeCode})",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "En Yüksek Dalga: ${save.highestWaveReached} • Terfi Sayısı: ${save.prestigeCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBadgeCard(
                            label = "İmha Piyade",
                            value = "${save.totalInfantryKilled}",
                            color = TacticalGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatBadgeCard(
                            label = "İmha Tank",
                            value = "${save.totalTanksKilled}",
                            color = EconomyGold,
                            modifier = Modifier.weight(1f)
                        )
                        StatBadgeCard(
                            label = "İmha Uçak",
                            value = "${save.totalAircraftKilled}",
                            color = ShieldCyan,
                            modifier = Modifier.weight(1f)
                        )
                        StatBadgeCard(
                            label = "Boss Mech",
                            value = "${save.totalBossesKilled}",
                            color = PrestigePurple,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. General Promotion (Prestige) System Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, PrestigePurple)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = PrestigePurple
                            )
                            Column {
                                Text(
                                    text = "GENERAL TERFİSİ (PRESTİJ)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Mevcut Terfi Puanı: ${save.promotionPoints} TP",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PrestigePurple
                                )
                            }
                        }

                        Button(
                            onClick = onPerformPrestige,
                            enabled = potentialPrestigePoints > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrestigePurple,
                                disabledContainerColor = SlateCardBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("prestige_button")
                        ) {
                            Text(
                                text = if (potentialPrestigePoints > 0) {
                                    "TERFİ ET (+$potentialPrestigePoints TP)"
                                } else {
                                    "Dalga 10 Gerekli"
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (potentialPrestigePoints > 0) Color.White else TextSecondary
                            )
                        }
                    }

                    Text(
                        text = "10. Dalgadan itibaren isteğe bağlı terfi yapabilirsiniz. Terfi işlemi dalga ve tesis seviyelerini başlangıca döndürürken kalıcı Terfi Puanı (TP) kazandırır. Daha yüksek dalgalar daha fazla TP verir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    // Permanent Upgrades List
                    PrestigeUpgradeId.entries.forEach { permId ->
                        val lv = ProgressionCatalog.getPrestigeUpgradeLevel(save, permId)
                        val cost = ProgressionCatalog.getPrestigeUpgradeCost(save, permId)
                        val canBuy = save.promotionPoints >= cost

                        Surface(
                            color = SlateCardBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (canBuy) PrestigePurple else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpgradePermanentSkill(permId) }
                                .testTag("perm_upgrade_${permId.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = permId.titleTr,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Sv.$lv",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrestigePurple
                                        )
                                    }
                                    Text(
                                        text = permId.descriptionTr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = permId.bonusPerLevelTr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TacticalGreen
                                    )
                                }

                                Surface(
                                    color = if (canBuy) PrestigePurple else SlateSurface,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$cost TP",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (canBuy) Color.White else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Special Base Skins (Özel Üs Görünümleri — Preserved across Prestige)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DiamondBlue.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = DiamondBlue)
                        Text(
                            text = "ÖZEL ÜS GÖRÜNÜMLERİ",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    ProgressionCatalog.baseSkins.forEach { skin ->
                        val isOwned = skin.id in ownedSkins
                        val isSelected = save.selectedBaseSkinId == skin.id

                        Surface(
                            color = SlateCardBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) skin.accentColor else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectOrBuySkin(skin) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(skin.accentColor)
                                    )
                                    Column {
                                        Text(
                                            text = skin.nameTr,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = skin.descriptionTr,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Surface(
                                    color = when {
                                        isSelected -> TacticalGreen.copy(alpha = 0.2f)
                                        isOwned -> SlateSurface
                                        else -> DiamondBlue.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when {
                                            isSelected -> "AKTİF"
                                            isOwned -> "SEÇ"
                                            else -> "${skin.diamondPrice} ELMAS"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = when {
                                            isSelected -> TacticalGreen
                                            isOwned -> TextPrimary
                                            else -> DiamondBlue
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Fixed Missions & Achievements (Sabit Görevler ve Başarımlar)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = EconomyGold)
                Text(
                    text = "SABİT GÖREVLER VE BAŞARIMLAR",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        }

        items(missions, key = { it.id }) { mission ->
            MissionItemCard(
                mission = mission,
                onClaimMission = onClaimMission
            )
        }
    }
}

@Composable
private fun StatBadgeCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SlateCardBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}
