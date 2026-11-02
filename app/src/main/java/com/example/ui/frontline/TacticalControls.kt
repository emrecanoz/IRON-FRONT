package com.example.ui.frontline

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.CombatSnapshot
import com.example.game.combat.TacticalSkillType
import com.example.game.economy.EconomyCalculator
import com.example.game.progression.FacilityType
import com.example.game.progression.ProgressionCatalog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TacticalControlBar(
    save: PlayerSaveEntity,
    combat: CombatSnapshot,
    isPaused: Boolean,
    gameSpeedMultiplier: Int,
    onSkillClick: (TacticalSkillType) -> Unit,
    onToggleSpeed: () -> Unit,
    onTogglePause: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Hava Bombardımanı (Airstrike)
        val isAirstrikeSelected = combat.activeTargetingSkill == TacticalSkillType.AIRSTRIKE
        val airReady = combat.airstrikeCharges > 0 || combat.airstrikeCooldownRemaining <= 0f
        TacticalSkillButton(
            title = "Hava Desteği",
            badge = if (combat.airstrikeCharges > 0) "${combat.airstrikeCharges}x Hazır" else "${combat.airstrikeCooldownRemaining.toInt()}sn",
            icon = Icons.Default.FlightTakeoff,
            accentColor = if (isAirstrikeSelected) DangerRed else EconomyGold,
            isEnabled = airReady,
            isSelected = isAirstrikeSelected,
            testTag = "skill_airstrike_button",
            onClick = { onSkillClick(TacticalSkillType.AIRSTRIKE) },
            modifier = Modifier.weight(1.25f)
        )

        // 2. Topçu Ateşi (Unlocks Wave 4)
        val artUnlocked = save.highestWaveReached >= 4 || save.currentWave >= 4
        val isArtSelected = combat.activeTargetingSkill == TacticalSkillType.ARTILLERY_BARRAGE
        TacticalSkillButton(
            title = "Topçu Atışı",
            badge = if (!artUnlocked) "Dalga 4" else if (combat.artilleryCooldownRemaining <= 0f) "Hazır" else "${combat.artilleryCooldownRemaining.toInt()}sn",
            icon = Icons.Default.GpsFixed,
            accentColor = TacticalGreen,
            isEnabled = artUnlocked && combat.artilleryCooldownRemaining <= 0f,
            isSelected = isArtSelected,
            testTag = "skill_artillery_button",
            onClick = { onSkillClick(TacticalSkillType.ARTILLERY_BARRAGE) },
            modifier = Modifier.weight(1.1f)
        )

        // 3. Enerji Kalkanı Yükle (Unlocks Wave 7)
        val shieldUnlocked = save.highestWaveReached >= 7 || save.currentWave >= 7
        TacticalSkillButton(
            title = "Kalkan %100",
            badge = if (!shieldUnlocked) "Dalga 7" else if (combat.shieldBoostCooldownRemaining <= 0f) "Hazır" else "${combat.shieldBoostCooldownRemaining.toInt()}sn",
            icon = Icons.Default.Shield,
            accentColor = ShieldCyan,
            isEnabled = shieldUnlocked && combat.shieldBoostCooldownRemaining <= 0f,
            isSelected = false,
            testTag = "skill_shield_button",
            onClick = { onSkillClick(TacticalSkillType.SHIELD_OVERCHARGE) },
            modifier = Modifier.weight(1.1f)
        )

        // 4. Speed x1 / x2 Button
        Surface(
            color = if (gameSpeedMultiplier == 2) EconomyGold.copy(alpha = 0.22f) else SlateSurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (gameSpeedMultiplier == 2) EconomyGold else Color(0xFF475569)),
            modifier = Modifier
                .height(48.dp)
                .clickable(onClick = onToggleSpeed)
                .testTag("toggle_speed_button")
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "x$gameSpeedMultiplier",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (gameSpeedMultiplier == 2) EconomyGold else TextPrimary
                )
            }
        }

        // 5. Pause / Resume Button
        Surface(
            color = if (isPaused) DangerRed.copy(alpha = 0.25f) else SlateSurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (isPaused) DangerRed else Color(0xFF475569)),
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onTogglePause)
                .testTag("toggle_pause_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Devam Et" else "Duraklat",
                    tint = if (isPaused) DangerRed else TextPrimary
                )
            }
        }
    }
}

@Composable
fun TacticalSkillButton(
    title: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    isEnabled: Boolean,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = when {
            isSelected -> accentColor.copy(alpha = 0.28f)
            isEnabled -> SlateSurface
            else -> SlateCardBg.copy(alpha = 0.7f)
        },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isEnabled || isSelected) accentColor else Color(0xFF334155)
        ),
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isEnabled) accentColor else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) TextPrimary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEnabled) accentColor else TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun QuickFacilityUpgradeStrip(
    save: PlayerSaveEntity,
    onUpgradeFacility: (FacilityType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FacilityType.entries.forEach { facility ->
            val level = ProgressionCatalog.getFacilityLevel(save, facility)
            val cost = EconomyCalculator.calculateUpgradeCost(facility.baseCost, level)
            val canAfford = save.gold >= cost
            val shortName = when (facility) {
                FacilityType.BARRACKS -> "Kışla"
                FacilityType.ROCKET_SILO -> "Roket"
                FacilityType.AIR_FLEET -> "Uçak"
                FacilityType.BORDER_WALL -> "Duvar"
            }

            Surface(
                color = if (canAfford) SlateSurface else SlateCardBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (canAfford) facility.accentColor else Color(0xFF334155)
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onUpgradeFacility(facility) }
                    .testTag("quick_upgrade_${facility.name.lowercase()}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = shortName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Sv.$level",
                            style = MaterialTheme.typography.labelSmall,
                            color = facility.accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${EconomyCalculator.formatNumber(cost)} AU",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canAfford) EconomyGold else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
