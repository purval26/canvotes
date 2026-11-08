package com.canvotes.feature.canvas.engine

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke as ComposeStroke
import com.canvotes.core.math.BezierSmoother
import com.canvotes.core.model.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object StrokeRenderer {

    fun drawStroke(
        drawScope: DrawScope,
        stroke: Stroke,
        viewport: ViewportState,
        isDarkTheme: Boolean
    ) {
        if (stroke.points.isEmpty()) return

        // Map world points to screen coordinates
        val screenPoints = stroke.points.map { pt ->
            val sx = (pt.x + viewport.panX) * viewport.zoom
            val sy = (pt.y + viewport.panY) * viewport.zoom
            StrokePoint(sx, sy, pt.pressure, pt.timestamp)
        }

        val baseColor = parseColor(stroke.colorHex, isDarkTheme)
        val strokeWidth = (stroke.strokeWidth * viewport.zoom).coerceAtLeast(1.0f)

        when (stroke.tool) {
            StrokeTool.PEN -> {
                val path = BezierSmoother.createSmoothPath(screenPoints)
                drawScope.drawPath(
                    path = path,
                    color = baseColor.copy(alpha = stroke.opacity),
                    style = ComposeStroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            StrokeTool.PENCIL -> {
                val path = BezierSmoother.createSmoothPath(screenPoints)
                drawScope.drawPath(
                    path = path,
                    color = baseColor.copy(alpha = (stroke.opacity * 0.75f).coerceIn(0.1f, 1f)),
                    style = ComposeStroke(
                        width = (strokeWidth * 0.85f).coerceAtLeast(1.0f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            StrokeTool.HIGHLIGHTER -> {
                val path = BezierSmoother.createSmoothPath(screenPoints)
                val highlighterColor = if (isDarkTheme) Color(0xFFE4E4E7) else Color(0xFF27272A)
                drawScope.drawPath(
                    path = path,
                    color = highlighterColor.copy(alpha = 0.32f),
                    style = ComposeStroke(
                        width = strokeWidth * 3.5f,
                        cap = StrokeCap.Square,
                        join = StrokeJoin.Bevel
                    )
                )
            }
            StrokeTool.ERASER -> {
                // Handled via stroke collision / removal
            }
        }
    }

    fun drawShape(
        drawScope: DrawScope,
        shape: ShapeItem,
        viewport: ViewportState,
        isDarkTheme: Boolean
    ) {
        val sX = (shape.startX + viewport.panX) * viewport.zoom
        val sY = (shape.startY + viewport.panY) * viewport.zoom
        val eX = (shape.endX + viewport.panX) * viewport.zoom
        val eY = (shape.endY + viewport.panY) * viewport.zoom

        val left = minOf(sX, eX)
        val top = minOf(sY, eY)
        val right = maxOf(sX, eX)
        val bottom = maxOf(sY, eY)
        val width = right - left
        val height = bottom - top

        val strokeColor = parseColor(shape.strokeColorHex, isDarkTheme)
        val strokeWidth = (shape.strokeWidth * viewport.zoom).coerceAtLeast(1.0f)
        val strokeStyle = ComposeStroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (shape.type) {
            ShapeType.RECTANGLE -> {
                if (shape.isFilled && shape.fillColorHex != null) {
                    val fillColor = parseColor(shape.fillColorHex, isDarkTheme)
                    drawScope.drawRect(fillColor, topLeft = Offset(left, top), size = Size(width, height), style = Fill)
                }
                drawScope.drawRect(strokeColor, topLeft = Offset(left, top), size = Size(width, height), style = strokeStyle)
            }
            ShapeType.ROUNDED_RECTANGLE -> {
                val cornerRadius = CornerRadius(16f * viewport.zoom, 16f * viewport.zoom)
                if (shape.isFilled && shape.fillColorHex != null) {
                    val fillColor = parseColor(shape.fillColorHex, isDarkTheme)
                    drawScope.drawRoundRect(fillColor, topLeft = Offset(left, top), size = Size(width, height), cornerRadius = cornerRadius, style = Fill)
                }
                drawScope.drawRoundRect(strokeColor, topLeft = Offset(left, top), size = Size(width, height), cornerRadius = cornerRadius, style = strokeStyle)
            }
            ShapeType.CIRCLE, ShapeType.ELLIPSE -> {
                if (shape.isFilled && shape.fillColorHex != null) {
                    val fillColor = parseColor(shape.fillColorHex, isDarkTheme)
                    drawScope.drawOval(fillColor, topLeft = Offset(left, top), size = Size(width, height), style = Fill)
                }
                drawScope.drawOval(strokeColor, topLeft = Offset(left, top), size = Size(width, height), style = strokeStyle)
            }
            ShapeType.LINE -> {
                drawScope.drawLine(
                    color = strokeColor,
                    start = Offset(sX, sY),
                    end = Offset(eX, eY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
            ShapeType.ARROW -> {
                // Draw shaft
                drawScope.drawLine(
                    color = strokeColor,
                    start = Offset(sX, sY),
                    end = Offset(eX, eY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                // Draw arrowhead
                val angle = atan2(eY - sY, eX - sX)
                val arrowHeadLength = (20f * viewport.zoom).coerceIn(12f, 40f)
                val arrowAngle = Math.PI / 6.0

                val x1 = eX - arrowHeadLength * cos(angle - arrowAngle).toFloat()
                val y1 = eY - arrowHeadLength * sin(angle - arrowAngle).toFloat()
                val x2 = eX - arrowHeadLength * cos(angle + arrowAngle).toFloat()
                val y2 = eY - arrowHeadLength * sin(angle + arrowAngle).toFloat()

                val arrowPath = Path().apply {
                    moveTo(eX, eY)
                    lineTo(x1, y1)
                    lineTo(x2, y2)
                    close()
                }
                drawScope.drawPath(path = arrowPath, color = strokeColor, style = Fill)
            }
        }
    }

    private fun parseColor(hex: String, isDarkTheme: Boolean): Color {
        return try {
            // Adaptive monochrome: Pure black text becomes pure white in dark mode
            if (hex.equals("#000000", ignoreCase = true) || hex.equals("#000", ignoreCase = true)) {
                if (isDarkTheme) Color.White else Color.Black
            } else if (hex.equals("#FFFFFF", ignoreCase = true) || hex.equals("#FFF", ignoreCase = true)) {
                if (isDarkTheme) Color.Black else Color.White
            } else {
                Color(android.graphics.Color.parseColor(hex))
            }
        } catch (e: Exception) {
            if (isDarkTheme) Color.White else Color.Black
        }
    }
}
