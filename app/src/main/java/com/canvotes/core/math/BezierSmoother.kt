package com.canvotes.core.math

import androidx.compose.ui.graphics.Path
import com.canvotes.core.model.StrokePoint
import kotlin.math.hypot

object BezierSmoother {

    /**
     * Converts a series of sampled stroke points into a smooth path.
     * Preserves sharp directional apexes (like letters 'M', 'N', 'Z') via corner detection,
     * while rendering organic quadratic Bezier curves for flowing handwriting.
     */
    fun createSmoothPath(points: List<StrokePoint>): Path {
        val path = Path()
        if (points.isEmpty()) return path

        if (points.size == 1) {
            val p = points.first()
            path.moveTo(p.x, p.y)
            path.lineTo(p.x + 0.1f, p.y + 0.1f)
            return path
        }

        path.moveTo(points[0].x, points[0].y)

        if (points.size == 2) {
            path.lineTo(points[1].x, points[1].y)
            return path
        }

        var prevMidX = (points[0].x + points[1].x) / 2f
        var prevMidY = (points[0].y + points[1].y) / 2f
        path.lineTo(prevMidX, prevMidY)

        for (i in 1 until points.size - 1) {
            val pPrev = points[i - 1]
            val pCurr = points[i]
            val pNext = points[i + 1]

            // Check if pCurr is a sharp corner
            val v1x = pCurr.x - pPrev.x
            val v1y = pCurr.y - pPrev.y
            val v2x = pNext.x - pCurr.x
            val v2y = pNext.y - pCurr.y

            val len1 = hypot(v1x, v1y)
            val len2 = hypot(v2x, v2y)

            val isSharpCorner = if (len1 > 2f && len2 > 2f) {
                val cosTheta = (v1x * v2x + v1y * v2y) / (len1 * len2)
                cosTheta < 0.35f // Angle > ~70 degrees
            } else {
                false
            }

            if (isSharpCorner) {
                // Draw sharp line to the corner apex to keep handwritten letters crisp
                path.lineTo(pCurr.x, pCurr.y)
                prevMidX = (pCurr.x + pNext.x) / 2f
                prevMidY = (pCurr.y + pNext.y) / 2f
                path.lineTo(prevMidX, prevMidY)
            } else {
                val nextMidX = (pCurr.x + pNext.x) / 2f
                val nextMidY = (pCurr.y + pNext.y) / 2f
                path.quadraticTo(pCurr.x, pCurr.y, nextMidX, nextMidY)
                prevMidX = nextMidX
                prevMidY = nextMidY
            }
        }

        val last = points.last()
        path.lineTo(last.x, last.y)
        return path
    }

    /**
     * Subsamples or filters noisy jitter if consecutive points are excessively close,
     * while strictly protecting sharp corners and endpoints.
     */
    fun filterJitter(points: List<StrokePoint>, minDistance: Float = 1.2f): List<StrokePoint> {
        if (points.size <= 2) return points
        val result = mutableListOf(points.first())
        val minDistSq = minDistance * minDistance

        for (i in 1 until points.size - 1) {
            val prev = result.last()
            val curr = points[i]
            val next = points[i + 1]

            val distSq = (curr.x - prev.x) * (curr.x - prev.x) + (curr.y - prev.y) * (curr.y - prev.y)

            // Detect if curr is an apex corner
            val v1x = curr.x - prev.x
            val v1y = curr.y - prev.y
            val v2x = next.x - curr.x
            val v2y = next.y - curr.y
            val len1 = hypot(v1x, v1y)
            val len2 = hypot(v2x, v2y)
            val isSharp = if (len1 > 1f && len2 > 1f) {
                ((v1x * v2x + v1y * v2y) / (len1 * len2)) < 0.35f
            } else false

            if (distSq >= minDistSq || isSharp) {
                result.add(curr)
            }
        }
        result.add(points.last())
        return result
    }
}
