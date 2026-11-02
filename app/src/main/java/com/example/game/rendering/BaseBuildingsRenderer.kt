package com.example.game.rendering

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EconomyGold
import kotlin.math.cos
import kotlin.math.sin

/**
 * Helper to draw an isometric 2.5D building block with top roof, left sunlit wall, and right shaded wall.
 */
fun DrawScope.drawIsoBuildingBlock(
    cx: Float,
    cy: Float,
    width: Float,
    depth: Float,
    height: Float,
    roofColor: Color,
    leftWallColor: Color,
    rightWallColor: Color
) {
    val halfW = width / 2f
    val halfD = depth / 2f

    // Top diamond vertices (shifted up by height)
    val topN = Offset(cx, cy - height - halfD)
    val topW = Offset(cx - halfW, cy - height)
    val topS = Offset(cx, cy - height + halfD)
    val topE = Offset(cx + halfW, cy - height)

    // Bottom wall vertices
    val botW = Offset(cx - halfW, cy)
    val botS = Offset(cx, cy + halfD)
    val botE = Offset(cx + halfW, cy)

    // Left Wall
    val leftPath = Path().apply {
        moveTo(topW.x, topW.y)
        lineTo(topS.x, topS.y)
        lineTo(botS.x, botS.y)
        lineTo(botW.x, botW.y)
        close()
    }
    drawPath(leftPath, leftWallColor)

    // Right Wall
    val rightPath = Path().apply {
        moveTo(topS.x, topS.y)
        lineTo(topE.x, topE.y)
        lineTo(botE.x, botE.y)
        lineTo(botS.x, botS.y)
        close()
    }
    drawPath(rightPath, rightWallColor)

    // Roof Diamond
    val roofPath = Path().apply {
        moveTo(topN.x, topN.y)
        lineTo(topW.x, topW.y)
        lineTo(topS.x, topS.y)
        lineTo(topE.x, topE.y)
        close()
    }
    drawPath(roofPath, roofColor)
}

fun DrawScope.drawRadarTowerIso(
    cx: Float,
    cy: Float,
    scale: Float,
    radarAngleDeg: Float,
    accentColor: Color
) {
    drawIsoBuildingBlock(
        cx = cx,
        cy = cy,
        width = 72f * scale,
        depth = 42f * scale,
        height = 46f * scale,
        roofColor = Color(0xFF475569),
        leftWallColor = Color(0xFF334155),
        rightWallColor = Color(0xFF1E293B)
    )
    // Rotating radar dish
    val dishCenter = Offset(cx, cy - 64f * scale)
    val rad = Math.toRadians(radarAngleDeg.toDouble())
    val dishEnd = Offset(
        dishCenter.x + (cos(rad) * 24f * scale).toFloat(),
        dishCenter.y + (sin(rad) * 12f * scale).toFloat()
    )
    drawCircle(
        color = accentColor.copy(alpha = 0.35f),
        radius = 22f * scale,
        center = dishCenter,
        style = Stroke(width = 2f)
    )
    drawLine(
        color = accentColor,
        start = dishCenter,
        end = dishEnd,
        strokeWidth = 4f * scale,
        cap = StrokeCap.Round
    )
}

fun DrawScope.drawRocketSiloIso(
    cx: Float,
    cy: Float,
    scale: Float,
    level: Int,
    launchFlash: Boolean,
    accentColor: Color
) {
    val tier = if (level >= 10) 3 else if (level >= 5) 2 else 1
    val w = (85f + tier * 14f) * scale
    val d = (52f + tier * 8f) * scale
    val h = (28f + tier * 10f) * scale

    drawIsoBuildingBlock(
        cx = cx,
        cy = cy,
        width = w,
        depth = d,
        height = h,
        roofColor = if (launchFlash) EconomyGold else Color(0xFF64748B),
        leftWallColor = Color(0xFF475569),
        rightWallColor = Color(0xFF334155)
    )
    // Silo Blast Hatch(es)
    val hatchCount = tier
    for (i in 0 until hatchCount) {
        val offsetX = (i - (hatchCount - 1) / 2f) * 22f * scale
        drawOval(
            color = Color(0xFF0F172A),
            topLeft = Offset(cx + offsetX - 10f * scale, cy - h - 8f * scale),
            size = Size(20f * scale, 12f * scale)
        )
        // Missile warhead tip peeking out
        drawCircle(
            color = if (launchFlash) EconomyGold else DangerRed,
            radius = 5f * scale,
            center = Offset(cx + offsetX, cy - h - 4f * scale)
        )
    }
    if (tier >= 2) {
        drawLine(
            color = accentColor,
            start = Offset(cx - w * 0.3f, cy - h),
            end = Offset(cx + w * 0.3f, cy - h),
            strokeWidth = 3f * scale
        )
    }
}

fun DrawScope.drawCommandHqIso(
    cx: Float,
    cy: Float,
    scale: Float,
    wallColor: Color,
    accentColor: Color
) {
    // Base tier
    drawIsoBuildingBlock(
        cx = cx,
        cy = cy,
        width = 115f * scale,
        depth = 68f * scale,
        height = 48f * scale,
        roofColor = wallColor.copy(alpha = 0.95f),
        leftWallColor = wallColor,
        rightWallColor = Color(0xFF1E293B)
    )
    // Upper command tower
    drawIsoBuildingBlock(
        cx = cx,
        cy = cy - 44f * scale,
        width = 74f * scale,
        depth = 44f * scale,
        height = 38f * scale,
        roofColor = accentColor.copy(alpha = 0.85f),
        leftWallColor = Color(0xFF334155),
        rightWallColor = Color(0xFF0F172A)
    )
}

fun DrawScope.drawBarracksIso(
    cx: Float,
    cy: Float,
    scale: Float,
    level: Int,
    accentColor: Color
) {
    val tier = if (level >= 10) 3 else if (level >= 5) 2 else 1
    // Main barracks building
    drawIsoBuildingBlock(
        cx = cx,
        cy = cy,
        width = (92f + tier * 12f) * scale,
        depth = (54f + tier * 6f) * scale,
        height = (32f + tier * 7f) * scale,
        roofColor = Color(0xFF15803D),
        leftWallColor = Color(0xFF475569),
        rightWallColor = Color(0xFF334155)
    )
    // Level 5+ adds side training tent / annex
    if (tier >= 2) {
        drawIsoBuildingBlock(
            cx = cx + 38f * scale,
            cy = cy + 16f * scale,
            width = 52f * scale,
            depth = 32f * scale,
            height = 24f * scale,
            roofColor = accentColor,
            leftWallColor = Color(0xFF334155),
            rightWallColor = Color(0xFF1E293B)
        )
    }
    // Level 10+ adds command communication spire
    if (tier >= 3) {
        drawIsoBuildingBlock(
            cx = cx - 22f * scale,
            cy = cy - 34f * scale,
            width = 40f * scale,
            depth = 26f * scale,
            height = 28f * scale,
            roofColor = EconomyGold,
            leftWallColor = Color(0xFF475569),
            rightWallColor = Color(0xFF1E293B)
        )
    }
}

fun DrawScope.drawAirHangarIso(
    cx: Float,
    cy: Float,
    scale: Float,
    level: Int,
    accentColor: Color,
    planeBitmap: ImageBitmap
) {
    val tier = if (level >= 10) 3 else if (level >= 5) 2 else 1
    // Hangar structure
    drawIsoBuildingBlock(
        cx = cx,
        cy = cy,
        width = (102f + tier * 10f) * scale,
        depth = 58f * scale,
        height = (30f + tier * 6f) * scale,
        roofColor = Color(0xFF38BDF8).copy(alpha = 0.75f),
        leftWallColor = Color(0xFF334155),
        rightWallColor = Color(0xFF1E293B)
    )
    // Helipad / Runway H marking on roof
    val roofY = cy - (30f + tier * 6f) * scale
    drawCircle(
        color = EconomyGold,
        radius = 16f * scale,
        center = Offset(cx, roofY),
        style = Stroke(width = 2.5f * scale)
    )
    // Parked stealth combat jet on hangar runway
    val parkedSize = 46f * scale
    drawImage(
        image = planeBitmap,
        dstOffset = IntOffset((cx - parkedSize * 0.45f).toInt(), (roofY - parkedSize * 0.50f).toInt()),
        dstSize = IntSize(parkedSize.toInt(), parkedSize.toInt())
    )

    if (tier >= 2) {
        // Control tower next to hangar
        drawIsoBuildingBlock(
            cx = cx - 36f * scale,
            cy = cy - 18f * scale,
            width = 34f * scale,
            depth = 24f * scale,
            height = 46f * scale,
            roofColor = accentColor,
            leftWallColor = Color(0xFF475569),
            rightWallColor = Color(0xFF1E293B)
        )
    }
}

fun DrawScope.drawBorderWallAndTurrets(
    wallX: Float,
    h: Float,
    scale: Float,
    level: Int,
    turretFiring: Boolean,
    wallColor: Color,
    accentColor: Color
) {
    val tier = if (level >= 10) 3 else if (level >= 5) 2 else 1
    val wallThickness = (18f + tier * 5f) * scale

    // Continuous fortified perimeter wall segment
    drawRoundRect(
        color = wallColor,
        topLeft = Offset(wallX - wallThickness / 2f, h * 0.25f),
        size = Size(wallThickness, h * 0.66f),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    // Bastion pillars & automated defense turrets along the wall
    val turretPositionsY = if (tier >= 3) {
        listOf(h * 0.30f, h * 0.44f, h * 0.58f, h * 0.72f, h * 0.85f)
    } else if (tier == 2) {
        listOf(h * 0.34f, h * 0.50f, h * 0.66f, h * 0.82f)
    } else {
        listOf(h * 0.38f, h * 0.58f, h * 0.78f)
    }

    for (ty in turretPositionsY) {
        // Bastion block
        drawRoundRect(
            color = Color(0xFF475569),
            topLeft = Offset(wallX - 16f * scale, ty - 16f * scale),
            size = Size(32f * scale, 32f * scale),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )
        // Turret head
        drawCircle(
            color = accentColor,
            radius = 10f * scale,
            center = Offset(wallX, ty)
        )
        // Twin gun barrels pointing right
        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(wallX, ty - 3f * scale),
            end = Offset(wallX + 20f * scale, ty - 3f * scale),
            strokeWidth = 3f * scale
        )
        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(wallX, ty + 3f * scale),
            end = Offset(wallX + 20f * scale, ty + 3f * scale),
            strokeWidth = 3f * scale
        )
        if (turretFiring) {
            drawCircle(
                color = EconomyGold,
                radius = 7f * scale,
                center = Offset(wallX + 23f * scale, ty)
            )
        }
    }
}

fun DrawScope.drawHexEnergyShield(
    shieldX: Float,
    h: Float,
    shieldRatio: Float,
    shieldColor: Color
) {
    val alpha = (0.18f + 0.35f * shieldRatio).coerceIn(0.12f, 0.55f)
    // Glowing vertical plasma barrier
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                shieldColor.copy(alpha = alpha * 0.8f),
                shieldColor.copy(alpha = alpha * 0.2f)
            ),
            startX = shieldX - 18f,
            endX = shieldX + 14f
        ),
        topLeft = Offset(shieldX - 18f, h * 0.25f),
        size = Size(32f, h * 0.66f)
    )

    // Hexagonal lattice nodes along the shield
    val stepY = h * 0.045f
    var curY = h * 0.27f
    while (curY < h * 0.89f) {
        drawCircle(
            color = shieldColor.copy(alpha = alpha + 0.15f),
            radius = 9f,
            center = Offset(shieldX, curY),
            style = Stroke(width = 1.6f)
        )
        curY += stepY
    }
}
