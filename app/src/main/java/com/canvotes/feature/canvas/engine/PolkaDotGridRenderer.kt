package com.canvotes.feature.canvas.engine

import android.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.canvotes.core.model.ViewportState
import com.canvotes.design.theme.DotColorDark
import com.canvotes.design.theme.DotColorLight
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

object PolkaDotGridRenderer {

    const val BASE_DOT_SPACING = 36f
    const val BASE_DOT_RADIUS = 1.4f

    private val dotPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private var reusablePointsBuffer = FloatArray(4096)

    fun drawGrid(
        drawScope: DrawScope,
        viewport: ViewportState,
        isDarkTheme: Boolean
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height
        if (width <= 0f || height <= 0f) return

        val baseColor = if (isDarkTheme) DotColorDark else DotColorLight

        // Visible world bounds
        val minWorldX = -viewport.panX
        val maxWorldX = (width / viewport.zoom) - viewport.panX
        val minWorldY = -viewport.panY
        val maxWorldY = (height / viewport.zoom) - viewport.panY

        // Subsampling when zoomed out to prevent dot overcrowding and maintain 120 FPS
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

        val numCols = endCol - startCol + 1
        val numRows = endRow - startRow + 1
        val totalDots = numCols * numRows
        if (totalDots <= 0) return

        val requiredFloatCount = totalDots * 2
        if (reusablePointsBuffer.size < requiredFloatCount) {
            reusablePointsBuffer = FloatArray(requiredFloatCount + 512)
        }

        // Scaled dot radius with gentle square root scaling to keep dots crisp across zoom levels
        val dotRadius = (BASE_DOT_RADIUS * sqrt(viewport.zoom.coerceIn(0.4f, 2.5f))).coerceIn(1.0f, 3.5f)

        // Alpha fade when zoomed very far out
        val alpha = when {
            viewport.zoom < 0.15f -> (viewport.zoom / 0.15f).coerceIn(0.1f, 1f)
            else -> 1.0f
        }
        val dotColor = baseColor.copy(alpha = baseColor.alpha * alpha)
        dotPaint.color = dotColor.toArgb()
        dotPaint.strokeWidth = dotRadius * 2f

        var idx = 0
        val pts = reusablePointsBuffer

        for (col in startCol..endCol) {
            val worldX = col * effectiveSpacing
            val screenX = (worldX + viewport.panX) * viewport.zoom

            for (row in startRow..endRow) {
                val worldY = row * effectiveSpacing
                val screenY = (worldY + viewport.panY) * viewport.zoom

                pts[idx++] = screenX
                pts[idx++] = screenY
            }
        }

        // Single vectorized hardware GPU draw call
        drawScope.drawContext.canvas.nativeCanvas.drawPoints(pts, 0, idx, dotPaint)
    }
}
