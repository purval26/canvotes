package com.canvotes.feature.canvas.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.canvotes.core.model.ViewportState
import com.canvotes.design.theme.DotColorDark
import com.canvotes.design.theme.DotColorLight
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

object PolkaDotGridRenderer {

    // Base spacing between dots in world space (density independent)
    const val BASE_DOT_SPACING = 36f
    const val BASE_DOT_RADIUS = 1.4f

    fun drawGrid(
        drawScope: DrawScope,
        viewport: ViewportState,
        isDarkTheme: Boolean
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        val baseColor = if (isDarkTheme) DotColorDark else DotColorLight

        // Visible world bounds
        val minWorldX = -viewport.panX
        val maxWorldX = (width / viewport.zoom) - viewport.panX
        val minWorldY = -viewport.panY
        val maxWorldY = (height / viewport.zoom) - viewport.panY

        // Subsampling when zoomed out to prevent dot overcrowding and maintain 60+ FPS
        val stepMultiplier = when {
            viewport.zoom < 0.25f -> 4
            viewport.zoom < 0.5f -> 2
            else -> 1
        }
        val effectiveSpacing = BASE_DOT_SPACING * stepMultiplier

        val startCol = floor(minWorldX / effectiveSpacing).toInt()
        val endCol = ceil(maxWorldX / effectiveSpacing).toInt()
        val startRow = floor(minWorldY / effectiveSpacing).toInt()
        val endRow = ceil(maxWorldY / effectiveSpacing).toInt()

        // Scaled dot radius with gentle square root scaling to keep dots crisp across zoom levels
        val dotRadius = (BASE_DOT_RADIUS * sqrt(viewport.zoom.coerceIn(0.4f, 2.5f))).coerceIn(1.0f, 3.5f)

        // Alpha fade when zoomed very far out
        val alpha = when {
            viewport.zoom < 0.15f -> (viewport.zoom / 0.15f).coerceIn(0.1f, 1f)
            else -> 1.0f
        }
        val dotColor = baseColor.copy(alpha = baseColor.alpha * alpha)

        for (col in startCol..endCol) {
            val worldX = col * effectiveSpacing
            val screenX = (worldX + viewport.panX) * viewport.zoom

            for (row in startRow..endRow) {
                val worldY = row * effectiveSpacing
                val screenY = (worldY + viewport.panY) * viewport.zoom

                drawScope.drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(screenX, screenY)
                )
            }
        }
    }
}
