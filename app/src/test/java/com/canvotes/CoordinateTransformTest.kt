package com.canvotes

import com.canvotes.core.math.CoordinateTransform
import com.canvotes.core.model.Stroke
import com.canvotes.core.model.StrokePoint
import com.canvotes.core.model.ViewportState
import org.junit.Assert.*
import org.junit.Test

class CoordinateTransformTest {

    @Test
    fun testScreenToWorldTransformation() {
        val viewport = ViewportState(panX = 100f, panY = 50f, zoom = 2.0f)
        val screenX = 400f
        val screenY = 300f

        val (worldX, worldY) = CoordinateTransform.screenToWorld(screenX, screenY, viewport)

        // screenX / zoom - panX = 400 / 2 - 100 = 100
        // screenY / zoom - panY = 300 / 2 - 50 = 100
        assertEquals(100f, worldX, 0.001f)
        assertEquals(100f, worldY, 0.001f)
    }

    @Test
    fun testWorldToScreenTransformation() {
        val viewport = ViewportState(panX = 100f, panY = 50f, zoom = 2.0f)
        val worldX = 100f
        val worldY = 100f

        val (screenX, screenY) = CoordinateTransform.worldToScreen(worldX, worldY, viewport)

        // (worldX + panX) * zoom = (100 + 100) * 2 = 400
        // (worldY + panY) * zoom = (100 + 50) * 2 = 300
        assertEquals(400f, screenX, 0.001f)
        assertEquals(300f, screenY, 0.001f)
    }

    @Test
    fun testStrokeBoundsComputation() {
        val stroke = Stroke(
            points = listOf(
                StrokePoint(10f, 20f),
                StrokePoint(50f, 80f),
                StrokePoint(30f, 10f)
            ),
            strokeWidth = 4f
        )

        val bounds = CoordinateTransform.computeStrokeBounds(stroke)
        assertNotNull(bounds)
        assertEquals(10f - 2f, bounds!!.left, 0.001f)
        assertEquals(10f - 2f, bounds.top, 0.001f)
        assertEquals(50f + 2f, bounds.right, 0.001f)
        assertEquals(80f + 2f, bounds.bottom, 0.001f)
    }

    @Test
    fun testDistancePointToSegment() {
        val dist = CoordinateTransform.distancePointToSegment(
            px = 10f, py = 10f,
            x1 = 0f, y1 = 0f,
            x2 = 20f, y2 = 0f
        )
        // Point is at (10, 10), segment is from (0,0) to (20,0). Closest point on segment is (10,0).
        assertEquals(10f, dist, 0.001f)
    }

    @Test
    fun testLassoPointInPolygon() {
        val polygon = listOf(
            StrokePoint(0f, 0f),
            StrokePoint(100f, 0f),
            StrokePoint(100f, 100f),
            StrokePoint(0f, 100f)
        )

        assertTrue(CoordinateTransform.isPointInPolygon(50f, 50f, polygon))
        assertFalse(CoordinateTransform.isPointInPolygon(150f, 50f, polygon))
    }

    @Test
    fun testStrokeIntersectingSegment() {
        val stroke = Stroke(
            points = listOf(
                StrokePoint(0f, 10f),
                StrokePoint(20f, 10f)
            ),
            strokeWidth = 2f
        )

        // Eraser segment crossing the stroke from (10, 0) to (10, 20)
        assertTrue(
            CoordinateTransform.isStrokeIntersectingSegment(
                stroke = stroke,
                x1 = 10f, y1 = 0f,
                x2 = 10f, y2 = 20f,
                tolerance = 4f
            )
        )

        // Eraser segment completely distant from stroke
        assertFalse(
            CoordinateTransform.isStrokeIntersectingSegment(
                stroke = stroke,
                x1 = 50f, y1 = 50f,
                x2 = 60f, y2 = 60f,
                tolerance = 4f
            )
        )
    }
}
