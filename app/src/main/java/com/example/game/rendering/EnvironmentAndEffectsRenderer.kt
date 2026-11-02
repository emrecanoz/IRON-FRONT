package com.example.game.rendering

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.game.combat.Projectile
import com.example.game.combat.ProjectileKind
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EconomyGold

fun DrawScope.drawIsometricTerrainAndRoads(w: Float, h: Float, accentColor: Color) {
    // Left base tarmac & sandy compound floor
    val basePadPath = Path().apply {
        moveTo(0f, h * 0.22f)
        lineTo(w * 0.265f, h * 0.26f)
        lineTo(w * 0.265f, h * 0.90f)
        lineTo(0f, h * 0.94f)
        close()
    }
    drawPath(
        path = basePadPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF28352B), Color(0xFF344234)),
            startX = 0f,
            endX = w * 0.265f
        )
    )

    // Subtle isometric grid lines across battlefield
    val gridColor = Color(0xFF94A3B8).copy(alpha = 0.07f)
    for (i in 1..12) {
        val x = w * (i / 12f)
        drawLine(
            color = gridColor,
            start = Offset(x, h * 0.22f),
            end = Offset(x - w * 0.08f, h * 0.95f),
            strokeWidth = 1f
        )
    }
    for (j in 3..9) {
        val y = h * (j / 10f)
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
    }

    // Two Military Supply & Combat Asphalt Lanes leading from right to base gate
    val roadLanes = listOf(h * 0.46f, h * 0.66f)
    for (laneY in roadLanes) {
        drawRect(
            color = Color(0xFF1E293B).copy(alpha = 0.78f),
            topLeft = Offset(w * 0.25f, laneY - h * 0.042f),
            size = Size(w * 0.75f, h * 0.084f)
        )
        // Dashed yellow road markings
        var dashX = w * 0.28f
        while (dashX < w * 0.98f) {
            drawLine(
                color = EconomyGold.copy(alpha = 0.45f),
                start = Offset(dashX, laneY),
                end = Offset(dashX + w * 0.03f, laneY),
                strokeWidth = 2.5f
            )
            dashX += w * 0.065f
        }
    }

    // Low-poly Pine Trees along top & bottom edges
    val treePositions = listOf(
        Offset(w * 0.05f, h * 0.23f),
        Offset(w * 0.16f, h * 0.24f),
        Offset(w * 0.35f, h * 0.25f),
        Offset(w * 0.58f, h * 0.25f),
        Offset(w * 0.82f, h * 0.24f),
        Offset(w * 0.44f, h * 0.87f),
        Offset(w * 0.72f, h * 0.88f)
    )
    for (pos in treePositions) {
        drawLowPolyPineTree(pos.x, pos.y, w * 0.024f, accentColor)
    }
}

fun DrawScope.drawLowPolyPineTree(cx: Float, cy: Float, radius: Float, accentColor: Color) {
    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.3f),
        topLeft = Offset(cx - radius * 0.8f, cy - radius * 0.2f),
        size = Size(radius * 1.6f, radius * 0.6f)
    )
    // Trunk
    drawRect(
        color = Color(0xFF5C4033),
        topLeft = Offset(cx - radius * 0.18f, cy - radius * 0.6f),
        size = Size(radius * 0.36f, radius * 0.6f)
    )
    // Layered triangular foliage
    for (layer in 0..2) {
        val topY = cy - radius * (2.2f + layer * 0.65f)
        val botY = cy - radius * (0.5f + layer * 0.55f)
        val halfW = radius * (1.05f - layer * 0.22f)
        val foliage = Path().apply {
            moveTo(cx, topY)
            lineTo(cx - halfW, botY)
            lineTo(cx + halfW, botY)
            close()
        }
        drawPath(
            path = foliage,
            color = if (layer == 2) accentColor.copy(alpha = 0.75f) else Color(0xFF15803D)
        )
    }
}

fun DrawScope.drawProjectile(
    proj: Projectile,
    w: Float,
    h: Float,
    shieldColor: Color
) {
    val cx = proj.currentX * w
    val cy = proj.currentY * h
    when (proj.kind) {
        ProjectileKind.ALLY_BULLET -> {
            drawLine(
                color = EconomyGold,
                start = Offset(cx - 6f, cy),
                end = Offset(cx + 6f, cy),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }
        ProjectileKind.TURRET_TRACER -> {
            drawLine(
                color = shieldColor,
                start = Offset(cx - 9f, cy),
                end = Offset(cx + 9f, cy),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )
        }
        ProjectileKind.HOMING_ROCKET, ProjectileKind.ARTILLERY_SHELL -> {
            // Rocket body + flame plume
            drawCircle(
                color = EconomyGold,
                radius = 6.5f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = DangerRed,
                radius = 4f,
                center = Offset(cx - 5f, cy + 2f)
            )
        }
        ProjectileKind.AIR_BOMB -> {
            drawOval(
                color = EconomyGold,
                topLeft = Offset(cx - 5f, cy - 7f),
                size = Size(10f, 14f)
            )
        }
        ProjectileKind.ENEMY_SHELL -> {
            drawCircle(
                color = DangerRed,
                radius = 4.5f,
                center = Offset(cx, cy)
            )
        }
        ProjectileKind.BOSS_PLASMA -> {
            drawCircle(
                color = Color(0xFFF43F5E),
                radius = 8.5f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(cx, cy)
            )
        }
    }
}
