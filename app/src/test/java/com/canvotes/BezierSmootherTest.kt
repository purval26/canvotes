package com.canvotes

import com.canvotes.core.math.BezierSmoother
import com.canvotes.core.model.StrokePoint
import org.junit.Assert.*
import org.junit.Test

class BezierSmootherTest {

    @Test
    fun testJitterFilterPreservesEndpoints() {
        val points = listOf(
            StrokePoint(0f, 0f),
            StrokePoint(0.2f, 0.2f), // Jitter
            StrokePoint(10f, 10f),
            StrokePoint(10.1f, 10.1f), // Jitter
            StrokePoint(20f, 20f)
        )

        val filtered = BezierSmoother.filterJitter(points, minDistance = 1.0f)
        assertEquals(3, filtered.size)
        assertEquals(0f, filtered.first().x, 0.001f)
        assertEquals(20f, filtered.last().x, 0.001f)
    }

    @Test
    fun testJitterFilterHandlesSmallInputs() {
        val single = listOf(StrokePoint(5f, 5f))
        val resultSingle = BezierSmoother.filterJitter(single)
        assertEquals(1, resultSingle.size)

        val pair = listOf(StrokePoint(1f, 1f), StrokePoint(1.2f, 1.2f))
        val resultPair = BezierSmoother.filterJitter(pair)
        assertEquals(2, resultPair.size)
    }
}
