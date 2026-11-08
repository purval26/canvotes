package com.canvotes.core.math

import androidx.compose.ui.graphics.Path
import com.canvotes.core.model.StrokePoint

object BezierSmoother {

    /**
     * Converts a series of sampled stroke points into a smooth quadratic Bezier Path.
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

        for (i in 1 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            // Midpoint serves as the endpoint of the quadratic curve segment
            val midX = (p0.x + p1.x) / 2f
            val midY = (p0.y + p1.y) / 2f
            path.quadraticTo(p0.x, p0.y, midX, midY)
        }

        val last = points.last()
        path.lineTo(last.x, last.y)
        return path
    }

    /**
     * Subsamples or filters noisy jitter if consecutive points are excessively close.
     */
    fun filterJitter(points: List<StrokePoint>, minDistance: Float = 1.5f): List<StrokePoint> {
        if (points.size <= 2) return points
        val result = mutableListOf(points.first())
        for (i in 1 until points.size) {
            val prev = result.last()
            val curr = points[i]
            val distSq = (curr.x - prev.x) * (curr.x - prev.x) + (curr.y - prev.y) * (curr.y - prev.y)
            if (distSq >= minDistance * minDistance || i == points.size - 1) {
                result.add(curr)
            }
        }
        return result
    }
}
