package com.example.game.rendering

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.PlayerSaveEntity
import com.example.game.combat.CombatSnapshot
import com.example.game.combat.EnemyType
import com.example.game.progression.ProgressionCatalog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.max

@Composable
fun IsometricBattlefieldCanvas(
    snapshot: CombatSnapshot,
    save: PlayerSaveEntity,
    onBattlefieldTap: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val skin = ProgressionCatalog.baseSkins.find { it.id == save.selectedBaseSkinId }
        ?: ProgressionCatalog.baseSkins.first()

    val enemyPlaneBitmap = ImageBitmap.imageResource(R.drawable.plane_enemy)
    val friendlyPlaneBitmap = ImageBitmap.imageResource(R.drawable.plane_friendly)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("battlefield_canvas")
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (size.width > 0 && size.height > 0) {
                        val normX = (offset.x / size.width).coerceIn(0f, 1f)
                        val normY = (offset.y / size.height).coerceIn(0f, 1f)
                        onBattlefieldTap(normX, normY)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // 1. Tactical Sky & Horizon Atmosphere
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF09101E),
                    Color(0xFF111E34),
                    Color(0xFF1A2E26),
                    Color(0xFF1E3224)
                ),
                startY = 0f,
                endY = h
            )
        )

        // 2. Isometric Terrain Ground, Base Concrete Pad, Roads & Perimeter Fences
        drawIsometricTerrainAndRoads(w, h, skin.accentColor)

        // 3. Left Military Base Structures (2.5D Low-Poly Isometric Buildings)
        drawRadarTowerIso(
            cx = w * 0.09f,
            cy = h * 0.30f,
            scale = w / 1080f,
            radarAngleDeg = snapshot.radarAngleDeg,
            accentColor = skin.accentColor
        )

        drawRocketSiloIso(
            cx = w * 0.13f,
            cy = h * 0.41f,
            scale = w / 1080f,
            level = save.rocketSiloLevel,
            launchFlash = snapshot.siloLaunchFlashTimer > 0f,
            accentColor = skin.accentColor
        )

        drawCommandHqIso(
            cx = w * 0.08f,
            cy = h * 0.54f,
            scale = w / 1080f,
            wallColor = skin.primaryWallColor,
            accentColor = skin.accentColor
        )

        drawBarracksIso(
            cx = w * 0.14f,
            cy = h * 0.67f,
            scale = w / 1080f,
            level = save.barracksLevel,
            accentColor = skin.accentColor
        )

        drawAirHangarIso(
            cx = w * 0.10f,
            cy = h * 0.81f,
            scale = w / 1080f,
            level = save.airFleetLevel,
            accentColor = skin.accentColor,
            planeBitmap = friendlyPlaneBitmap
        )

        // 4. Fortified Border Wall & Automated Turrets (x = 0.25w)
        drawBorderWallAndTurrets(
            wallX = w * 0.255f,
            h = h,
            scale = w / 1080f,
            level = save.borderWallLevel,
            turretFiring = snapshot.turretMuzzleFlashTimer > 0f,
            wallColor = skin.primaryWallColor,
            accentColor = skin.accentColor
        )

        // 5. Hexagonal Energy Shield Barrier
        if (snapshot.shieldCurrentHp > 0.0) {
            val shieldRatio = (snapshot.shieldCurrentHp / max(1.0, snapshot.shieldMaxHp)).toFloat().coerceIn(0.1f, 1f)
            drawHexEnergyShield(
                shieldX = w * 0.278f,
                h = h,
                shieldRatio = shieldRatio,
                shieldColor = skin.shieldColor
            )
        }

        // 6. Allied Soldiers on the Defense Line
        for (ally in snapshot.alliedSoldiers) {
            drawAlliedSoldier(
                ally = ally,
                w = w,
                h = h,
                accentColor = skin.accentColor
            )
        }

        // 7. Ground Enemies sorted by depth (laneY) for clean 2.5D painter's algorithm
        val groundEnemies = snapshot.enemies.filter { !it.type.isFlying }.sortedBy { it.laneY }
        for (enemy in groundEnemies) {
            when (enemy.type) {
                EnemyType.INFANTRY -> drawEnemyInfantry(enemy, w, h)
                EnemyType.ARMORED_TANK -> drawIsometricTank(enemy, w, h)
                EnemyType.MEGA_BOSS_MECH -> drawMegaBossMech(enemy, w, h)
                else -> {}
            }
        }

        // 8. Flying Enemy Stealth Jets (high altitude + ground shadow)
        val flyingEnemies = snapshot.enemies.filter { it.type.isFlying }
        for (jet in flyingEnemies) {
            drawStealthJet(jet, w, h, enemyPlaneBitmap)
        }

        // 9. Smoke Trails, Projectiles (Bullets, Homing Rockets, Bombs, Shells)
        for (smoke in snapshot.smokeTrail) {
            drawCircle(
                color = Color(0xFFCBD5E1).copy(alpha = smoke.alpha * 0.55f),
                radius = smoke.size,
                center = Offset(smoke.x * w, smoke.y * h)
            )
        }

        for (proj in snapshot.projectiles) {
            drawProjectile(proj, w, h, skin.shieldColor)
        }

        // 10. Friendly Air Sortie Planes sweeping across battlefield
        for (plane in snapshot.airSorties) {
            drawFriendlySortieJet(plane, w, h, skin.accentColor, friendlyPlaneBitmap)
        }

        // 11. Explosions (Light flash, shockwave ring, debris particles)
        for (exp in snapshot.explosions) {
            val cx = exp.x * w
            val cy = exp.y * h
            val maxR = exp.radius * w
            val curR = maxR * (0.25f + 0.75f * exp.progress)
            val alpha = (1f - exp.progress).coerceIn(0f, 1f)

            if (exp.isShieldHit) {
                drawCircle(
                    color = skin.shieldColor.copy(alpha = alpha * 0.55f),
                    radius = curR,
                    center = Offset(cx, cy),
                    style = Stroke(width = 4f)
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFBEB).copy(alpha = alpha),
                            Color(0xFFF59E0B).copy(alpha = alpha * 0.85f),
                            Color(0xFFEF4444).copy(alpha = alpha * 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = max(4f, curR)
                    ),
                    radius = max(4f, curR),
                    center = Offset(cx, cy)
                )
                if (exp.isBigRocketOrBomb) {
                    drawCircle(
                        color = EconomyGold.copy(alpha = 0.7f),
                        radius = curR * 1.15f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3f)
                    )
                }
            }
        }

        // 12. Floating Damage & Gold Numbers
        for (ft in snapshot.floatingTexts) {
            val alpha = (1f - ft.progress).coerceIn(0f, 1f)
            val color = when {
                ft.isGoldGain -> EconomyGold.copy(alpha = alpha)
                ft.isCriticalOrRocket -> Color(0xFFFFB703).copy(alpha = alpha)
                else -> Color.White.copy(alpha = alpha)
            }
            val measured = textMeasurer.measure(
                text = ft.text,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (ft.isCriticalOrRocket) 12.sp else 10.sp,
                    color = color
                )
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    (ft.x * w - measured.size.width / 2f).coerceIn(4f, w - measured.size.width - 4f),
                    (ft.y * h).coerceIn(10f, h - 30f)
                )
            )
        }

        // 13. Tactical Targeting Overlay Reticle when player selects Airstrike/Artillery
        if (snapshot.activeTargetingSkill != null) {
            val pulseX = w * 0.62f
            val pulseY = h * 0.56f
            drawCircle(
                color = DangerRed.copy(alpha = 0.25f),
                radius = w * 0.14f,
                center = Offset(pulseX, pulseY)
            )
            drawCircle(
                color = DangerRed,
                radius = w * 0.14f,
                center = Offset(pulseX, pulseY),
                style = Stroke(width = 3f)
            )
            drawLine(
                color = DangerRed,
                start = Offset(pulseX - w * 0.17f, pulseY),
                end = Offset(pulseX + w * 0.17f, pulseY),
                strokeWidth = 2f
            )
            drawLine(
                color = DangerRed,
                start = Offset(pulseX, pulseY - w * 0.17f),
                end = Offset(pulseX, pulseY + w * 0.17f),
                strokeWidth = 2f
            )
        }
    }
}
