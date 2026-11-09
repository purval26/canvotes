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
     * Checks if a stroke intersects with an eraser path segment (p1 -> p2).
     * Prevents fast eraser swipes from skipping strokes between touch sample intervals.
     */
    fun isStrokeIntersectingSegment(
        stroke: Stroke,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        tolerance: Float
    ): Boolean {
        if (x1 == x2 && y1 == y2) {
            return isStrokeIntersectingPoint(stroke, x1, y1, tolerance)
        }

        val bounds = computeStrokeBounds(stroke) ?: return false
        val segMinX = min(x1, x2) - tolerance
        val segMaxX = max(x1, x2) + tolerance
        val segMinY = min(y1, y2) - tolerance
        val segMaxY = max(y1, y2) + tolerance

        val segBounds = RectF(segMinX, segMinY, segMaxX, segMaxY)
        if (!bounds.intersects(segBounds)) return false

        // Check if endpoints hit the stroke
        if (isStrokeIntersectingPoint(stroke, x1, y1, tolerance)) return true
        if (isStrokeIntersectingPoint(stroke, x2, y2, tolerance)) return true

        // Check midpoint
        val midX = (x1 + x2) / 2f
        val midY = (y1 + y2) / 2f
        if (isStrokeIntersectingPoint(stroke, midX, midY, tolerance)) return true

        // Check line-line intersection with each stroke segment
        val points = stroke.points
        for (i in 0 until points.size - 1) {
            val sp1 = points[i]
            val sp2 = points[i + 1]
            if (doSegmentsIntersect(x1, y1, x2, y2, sp1.x, sp1.y, sp2.x, sp2.y)) {
                return true
            }
        }
        return false
    }

    private fun doSegmentsIntersect(
        p1x: Float, p1y: Float, p2x: Float, p2y: Float,
        p3x: Float, p3y: Float, p4x: Float, p4y: Float
    ): Boolean {
        fun ccw(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Boolean {
            return (cy - ay) * (bx - ax) > (by - ay) * (cx - ax)
        }
        return (ccw(p1x, p1y, p3x, p3y, p4x, p4y) != ccw(p2x, p2y, p3x, p3y, p4x, p4y)) &&
                (ccw(p1x, p1y, p2x, p2y, p3x, p3y) != ccw(p1x, p1y, p2x, p2y, p4x, p4y))
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
