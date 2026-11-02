package com.example.game.rendering

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.game.combat.AirSortiePlane
import com.example.game.combat.AlliedSoldier
import com.example.game.combat.EnemyUnit
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EconomyGold
import com.example.ui.theme.TacticalGreen
import kotlin.math.sin

fun DrawScope.drawAlliedSoldier(
    ally: AlliedSoldier,
    w: Float,
    h: Float,
    accentColor: Color
) {
    val cx = ally.x * w
    val cy = ally.laneY * h
    val legSwing = sin(ally.walkPhase) * 4f

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.35f),
        topLeft = Offset(cx - 8f, cy + 6f),
        size = Size(16f, 6f)
    )
    // Legs
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(cx - 2f, cy),
        end = Offset(cx - 2f + legSwing, cy + 8f),
        strokeWidth = 3f
    )
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(cx + 2f, cy),
        end = Offset(cx + 2f - legSwing, cy + 8f),
        strokeWidth = 3f
    )
    // Armored torso
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(cx - 6f, cy - 10f),
        size = Size(12f, 11f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Helmet
    drawCircle(
        color = Color(0xFF065F46),
        radius = 5.5f,
        center = Offset(cx, cy - 14f)
    )
    // Rifle & muzzle flash
    drawLine(
        color = Color(0xFFCBD5E1),
        start = Offset(cx + 2f, cy - 5f),
        end = Offset(cx + 14f, cy - 5f),
        strokeWidth = 2.5f
    )
    if (ally.muzzleFlashTimer > 0f) {
        drawCircle(
            color = EconomyGold,
            radius = 5f,
            center = Offset(cx + 16f, cy - 5f)
        )
    }
}

fun DrawScope.drawEnemyInfantry(enemy: EnemyUnit, w: Float, h: Float) {
    val cx = enemy.x * w
    val cy = enemy.laneY * h
    val legSwing = sin(enemy.walkCyclePhase) * 4.5f
    val bodyColor = if (enemy.hitFlashTimer > 0f) Color.White else DangerRed

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.35f),
        topLeft = Offset(cx - 8f, cy + 6f),
        size = Size(16f, 6f)
    )
    // Legs
    drawLine(
        color = Color(0xFF450A0A),
        start = Offset(cx - 2f, cy),
        end = Offset(cx - 2f + legSwing, cy + 9f),
        strokeWidth = 3f
    )
    drawLine(
        color = Color(0xFF450A0A),
        start = Offset(cx + 2f, cy),
        end = Offset(cx + 2f - legSwing, cy + 9f),
        strokeWidth = 3f
    )
    // Body
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(cx - 6f, cy - 11f),
        size = Size(12f, 12f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Helmet
    drawCircle(
        color = Color(0xFF7F1D1D),
        radius = 5.5f,
        center = Offset(cx, cy - 15f)
    )
    // Rifle pointing left
    drawLine(
        color = Color(0xFF94A3B8),
        start = Offset(cx - 2f, cy - 5f),
        end = Offset(cx - 14f, cy - 5f),
        strokeWidth = 2.5f
    )
    drawEnemyHealthBar(cx, cy - 25f, 24f, enemy.currentHp / enemy.maxHp)
}

fun DrawScope.drawIsometricTank(enemy: EnemyUnit, w: Float, h: Float) {
    val cx = enemy.x * w
    val cy = enemy.laneY * h
    val hullColor = if (enemy.hitFlashTimer > 0f) Color.White else Color(0xFF991B1B)
    val turretColor = if (enemy.hitFlashTimer > 0f) Color.White else Color(0xFFDC2626)

    // Ground shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(cx - 28f, cy + 4f),
        size = Size(56f, 16f)
    )
    // Animated tank caterpillar treads
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(cx - 24f, cy + 2f),
        size = Size(48f, 11f),
        cornerRadius = CornerRadius(5f, 5f)
    )
    val treadOffset = ((enemy.walkCyclePhase * 3f).toInt() % 6).toFloat()
    for (wheel in -2..2) {
        drawCircle(
            color = Color(0xFF64748B),
            radius = 3.2f,
            center = Offset(cx + wheel * 8f - treadOffset * 0.4f, cy + 7.5f)
        )
    }

    // Armored Low-Poly Hull
    drawRoundRect(
        color = hullColor,
        topLeft = Offset(cx - 26f, cy - 9f),
        size = Size(52f, 14f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Tank Turret
    drawRoundRect(
        color = turretColor,
        topLeft = Offset(cx - 14f, cy - 18f),
        size = Size(28f, 11f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Heavy Cannon Barrel pointing left toward player's base
    drawLine(
        color = Color(0xFFE2E8F0),
        start = Offset(cx - 14f, cy - 13f),
        end = Offset(cx - 38f, cy - 13f),
        strokeWidth = 4.5f,
        cap = StrokeCap.Round
    )
    drawEnemyHealthBar(cx, cy - 28f, 44f, enemy.currentHp / enemy.maxHp)
}

fun DrawScope.drawStealthJet(
    enemy: EnemyUnit,
    w: Float,
    h: Float,
    planeBitmap: ImageBitmap
) {
    val cx = enemy.x * w
    val cy = enemy.laneY * h
    val scale = w / 1080f
    val planeSize = 96f * scale

    // Ground shadow far below
    drawOval(
        color = Color.Black.copy(alpha = 0.28f),
        topLeft = Offset(cx - planeSize * 0.42f, cy + h * 0.16f),
        size = Size(planeSize * 0.85f, planeSize * 0.26f)
    )

    // Red engine afterburner glow behind the jet
    drawCircle(
        color = EconomyGold,
        radius = 4f * scale,
        center = Offset(cx + planeSize * 0.28f, cy - planeSize * 0.12f)
    )
    drawCircle(
        color = DangerRed,
        radius = 6.5f * scale,
        center = Offset(cx + planeSize * 0.33f, cy - planeSize * 0.14f)
    )

    // Draw high-fidelity enemy stealth jet sprite
    drawImage(
        image = planeBitmap,
        dstOffset = IntOffset((cx - planeSize / 2f).toInt(), (cy - planeSize / 2f).toInt()),
        dstSize = IntSize(planeSize.toInt(), planeSize.toInt()),
        colorFilter = if (enemy.hitFlashTimer > 0f) ColorFilter.tint(Color.White) else null
    )

    drawEnemyHealthBar(cx, cy - planeSize * 0.44f, 44f * scale, enemy.currentHp / enemy.maxHp)
}

fun DrawScope.drawMegaBossMech(enemy: EnemyUnit, w: Float, h: Float) {
    val cx = enemy.x * w
    val cy = enemy.laneY * h
    val legOffset = sin(enemy.walkCyclePhase) * 6f
    val armorColor = if (enemy.hitFlashTimer > 0f) Color.White else Color(0xFF7F1D1D)

    // Massive shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.55f),
        topLeft = Offset(cx - 38f, cy + 10f),
        size = Size(76f, 22f)
    )
    // Mech Hydraulic Legs
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(cx - 18f + legOffset, cy - 8f),
        size = Size(12f, 24f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(cx + 6f - legOffset, cy - 8f),
        size = Size(12f, 24f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Mech Armored Torso
    drawRoundRect(
        color = armorColor,
        topLeft = Offset(cx - 28f, cy - 44f),
        size = Size(56f, 38f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Glowing Plasma Reactor Core
    drawCircle(
        color = DangerRed,
        radius = 10f,
        center = Offset(cx, cy - 25f)
    )
    drawCircle(
        color = EconomyGold,
        radius = 5f,
        center = Offset(cx, cy - 25f)
    )
    // Twin Shoulder Rocket Pods & Heavy Plasma Cannon
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(cx - 42f, cy - 38f),
        size = Size(24f, 10f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawEnemyHealthBar(cx, cy - 58f, 68f, enemy.currentHp / enemy.maxHp, isBoss = true)
}

fun DrawScope.drawFriendlySortieJet(
    plane: AirSortiePlane,
    w: Float,
    h: Float,
    accentColor: Color,
    planeBitmap: ImageBitmap
) {
    val cx = plane.x * w
    val cy = plane.y * h
    val scale = w / 1080f
    val planeSize = if (plane.isTacticalStrike) 115f * scale else 98f * scale

    // Exhaust position on friendly jet (facing right, engine at back-left)
    val exhaustX = cx - planeSize * 0.28f
    val exhaustY = cy - planeSize * 0.12f

    // Twin afterburner contrails
    drawLine(
        color = Color.White.copy(alpha = 0.55f),
        start = Offset(exhaustX, exhaustY - 3f * scale),
        end = Offset(exhaustX - 55f * scale, exhaustY - 8f * scale),
        strokeWidth = 3f * scale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White.copy(alpha = 0.55f),
        start = Offset(exhaustX, exhaustY + 3f * scale),
        end = Offset(exhaustX - 55f * scale, exhaustY - 2f * scale),
        strokeWidth = 3f * scale,
        cap = StrokeCap.Round
    )

    drawCircle(
        color = if (plane.isTacticalStrike) EconomyGold else accentColor,
        radius = 6f * scale,
        center = Offset(exhaustX, exhaustY)
    )

    // Draw high-fidelity friendly aircraft sprite
    drawImage(
        image = planeBitmap,
        dstOffset = IntOffset((cx - planeSize / 2f).toInt(), (cy - planeSize / 2f).toInt()),
        dstSize = IntSize(planeSize.toInt(), planeSize.toInt())
    )
}

fun DrawScope.drawEnemyHealthBar(
    cx: Float,
    topY: Float,
    barWidth: Float,
    ratio: Double,
    isBoss: Boolean = false
) {
    val clamped = ratio.toFloat().coerceIn(0f, 1f)
    val barHeight = if (isBoss) 7f else 4.5f
    drawRoundRect(
        color = Color(0xFF0F172A).copy(alpha = 0.85f),
        topLeft = Offset(cx - barWidth / 2f, topY),
        size = Size(barWidth, barHeight),
        cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = if (isBoss) EconomyGold else DangerRed,
        topLeft = Offset(cx - barWidth / 2f, topY),
        size = Size(barWidth * clamped, barHeight),
        cornerRadius = CornerRadius(2f, 2f)
    )
}
