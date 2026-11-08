package com.canvotes.core.math

import com.canvotes.core.model.Stroke
import com.canvotes.core.model.StrokePoint
import com.canvotes.core.model.ViewportState
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

data class RectF(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun contains(x: Float, y: Float): Boolean {
        return x in left..right && y in top..bottom
    }

    fun intersects(other: RectF): Boolean {
        return left < other.right && right > other.left &&
                top < other.bottom && bottom > other.top
    }
}

object CoordinateTransform {

    fun screenToWorld(screenX: Float, screenY: Float, viewport: ViewportState): Pair<Float, Float> {
        val wx = (screenX / viewport.zoom) - viewport.panX
        val wy = (screenY / viewport.zoom) - viewport.panY
        return Pair(wx, wy)
    }

    fun worldToScreen(worldX: Float, worldY: Float, viewport: ViewportState): Pair<Float, Float> {
        val sx = (worldX + viewport.panX) * viewport.zoom
        val sy = (worldY + viewport.panY) * viewport.zoom
        return Pair(sx, sy)
    }

    fun computeStrokeBounds(stroke: Stroke): RectF? {
        if (stroke.points.isEmpty()) return null
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE

        for (pt in stroke.points) {
            if (pt.x < minX) minX = pt.x
            if (pt.x > maxX) maxX = pt.x
            if (pt.y < minY) minY = pt.y
            if (pt.y > maxY) maxY = pt.y
        }

        val padding = stroke.strokeWidth / 2f
        return RectF(minX - padding, minY - padding, maxX + padding, maxY + padding)
    }

    fun distancePointToSegment(
        px: Float, py: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val lengthSq = dx * dx + dy * dy
        if (lengthSq == 0f) return hypot(px - x1, py - y1)

        val t = max(0f, min(1f, ((px - x1) * dx + (py - y1) * dy) / lengthSq))
        val projX = x1 + t * dx
        val projY = y1 + t * dy
        return hypot(px - projX, py - projY)
    }

    fun isStrokeIntersectingPoint(stroke: Stroke, worldX: Float, worldY: Float, tolerance: Float): Boolean {
        val bounds = computeStrokeBounds(stroke) ?: return false
        val paddedBounds = RectF(
            bounds.left - tolerance,
            bounds.top - tolerance,
            bounds.right + tolerance,
            bounds.bottom + tolerance
        )
        if (!paddedBounds.contains(worldX, worldY)) return false

        val points = stroke.points
        if (points.size == 1) {
            val dist = hypot(points[0].x - worldX, points[0].y - worldY)
            return dist <= (tolerance + stroke.strokeWidth / 2f)
        }

        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val dist = distancePointToSegment(worldX, worldY, p1.x, p1.y, p2.x, p2.y)
            if (dist <= (tolerance + stroke.strokeWidth / 2f)) {
                return true
            }
        }
        return false
    }

    /**
     * Ray-casting algorithm to test whether a world point is inside a closed polygon (e.g. Lasso selection).
     */
    fun isPointInPolygon(px: Float, py: Float, polygon: List<StrokePoint>): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val xi = polygon[i].x
            val yi = polygon[i].y
            val xj = polygon[j].x
            val yj = polygon[j].y

            val intersect = ((yi > py) != (yj > py)) &&
                    (px < (xj - xi) * (py - yi) / (yj - yi) + xi)
            if (intersect) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
}
