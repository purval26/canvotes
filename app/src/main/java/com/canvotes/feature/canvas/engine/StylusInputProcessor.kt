package com.canvotes.feature.canvas.engine

import android.view.MotionEvent
import com.canvotes.core.math.CoordinateTransform
import com.canvotes.core.model.StrokePoint
import com.canvotes.core.model.ViewportState
import kotlin.math.hypot

sealed interface CanvasInputEvent {
    data class StrokeStart(val point: StrokePoint, val isStylus: Boolean) : CanvasInputEvent
    data class StrokeMove(val point: StrokePoint) : CanvasInputEvent
    data class StrokeEnd(val point: StrokePoint) : CanvasInputEvent
    data object StrokeCancel : CanvasInputEvent
    data class PanZoom(val deltaX: Float, val deltaY: Float, val zoomFactor: Float) : CanvasInputEvent
    data class Tap(val point: StrokePoint) : CanvasInputEvent
}

class StylusInputProcessor(
    private var palmRejectionEnabled: Boolean = true,
    private var fingerDrawingEnabled: Boolean = true
) {
    private var activePointerId: Int = -1
    private var isStrokeActive: Boolean = false
    private var isStylusActive: Boolean = false
    private var isMultiTouch: Boolean = false

    // Multi-touch tracking
    private var lastCentroidX: Float = 0f
    private var lastCentroidY: Float = 0f
    private var lastDistance: Float = 0f

    // Hand tool tracking
    private var lastPanX: Float = 0f
    private var lastPanY: Float = 0f

    fun updateSettings(palmRejection: Boolean, fingerDrawing: Boolean) {
        this.palmRejectionEnabled = palmRejection
        this.fingerDrawingEnabled = fingerDrawing
    }

    fun processMotionEvent(
        event: MotionEvent,
        viewport: ViewportState,
        isHandMode: Boolean,
        onInputEvent: (CanvasInputEvent) -> Unit
    ): Boolean {
        val action = event.actionMasked
        val pointerCount = event.pointerCount

        // ---------------------------------------------------------
        // 1. MULTI-TOUCH GESTURE (2 or more pointers -> Pan & Zoom)
        // ---------------------------------------------------------
        if (pointerCount >= 2) {
            // Cancel any single-finger stroke that was started before the second finger touched down
            if (isStrokeActive) {
                onInputEvent(CanvasInputEvent.StrokeCancel)
                isStrokeActive = false
                activePointerId = -1
                isStylusActive = false
            }

            val p0x = event.getX(0)
            val p0y = event.getY(0)
            val p1x = event.getX(1)
            val p1y = event.getY(1)
            val centroidX = (p0x + p1x) / 2f
            val centroidY = (p0y + p1y) / 2f
            val distance = hypot(p1x - p0x, p1y - p0y)

            when (action) {
                MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                    isMultiTouch = true
                    lastCentroidX = centroidX
                    lastCentroidY = centroidY
                    lastDistance = distance
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isMultiTouch) {
                        isMultiTouch = true
                        lastCentroidX = centroidX
                        lastCentroidY = centroidY
                        lastDistance = distance
                        return true
                    }

                    val deltaX = centroidX - lastCentroidX
                    val deltaY = centroidY - lastCentroidY
                    val zoomFactor = if (lastDistance > 20f && distance > 20f) {
                        (distance / lastDistance).coerceIn(0.7f, 1.4f)
                    } else {
                        1.0f
                    }

                    lastCentroidX = centroidX
                    lastCentroidY = centroidY
                    lastDistance = distance

                    onInputEvent(CanvasInputEvent.PanZoom(deltaX, deltaY, zoomFactor))
                    return true
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    // One finger lifted, but other fingers may still be touching
                    isMultiTouch = true
                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isMultiTouch = false
                    return true
                }
            }

            return true
        }

        // ---------------------------------------------------------
        // 2. MULTI-TOUCH RELEASE RECOVERY
        // ---------------------------------------------------------
        // If we were just doing a two-finger pinch-zoom and one finger lifted,
        // do not let the remaining finger accidentally draw a line!
        if (isMultiTouch) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                isMultiTouch = false
            }
            return true
        }

        // ---------------------------------------------------------
        // 3. SINGLE POINTER: HAND TOOL (Pan only)
        // ---------------------------------------------------------
        if (isHandMode) {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    lastPanX = event.x
                    lastPanY = event.y
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastPanX
                    val dy = event.y - lastPanY
                    lastPanX = event.x
                    lastPanY = event.y
                    onInputEvent(CanvasInputEvent.PanZoom(dx, dy, 1.0f))
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    return true
                }
            }
            return false
        }

        // ---------------------------------------------------------
        // 4. SINGLE POINTER: DRAWING MODE (Pen, Pencil, Shape, Eraser)
        // ---------------------------------------------------------
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val toolType = event.getToolType(pointerIndex)
        val isStylus = toolType == MotionEvent.TOOL_TYPE_STYLUS || toolType == MotionEvent.TOOL_TYPE_ERASER
        val isFinger = toolType == MotionEvent.TOOL_TYPE_FINGER || toolType == MotionEvent.TOOL_TYPE_UNKNOWN

        // Palm rejection heuristic:
        // Ignore large surface touches (wrist/palm resting on display)
        if (palmRejectionEnabled && isFinger) {
            val touchMajor = event.getTouchMajor(pointerIndex)
            val touchMinor = event.getTouchMinor(pointerIndex)
            if (touchMajor > 180f || (touchMajor > 120f && touchMinor > 80f)) {
                return true // Ignored as palm contact
            }
        }

        if (!isStylus && !fingerDrawingEnabled) {
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = pointerId
                isStrokeActive = true
                isStylusActive = isStylus

                val (worldX, worldY) = CoordinateTransform.screenToWorld(event.x, event.y, viewport)
                val pressure = if (isStylus) event.getPressure(pointerIndex).coerceIn(0.1f, 1.0f) else 1.0f
                val point = StrokePoint(worldX, worldY, pressure, System.currentTimeMillis())
                onInputEvent(CanvasInputEvent.StrokeStart(point, isStylus))
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isStrokeActive && activePointerId == pointerId) {
                    // Extract historical points for ultra-smooth 120fps curve capture
                    val historySize = event.historySize
                    for (h in 0 until historySize) {
                        val (hwX, hwY) = CoordinateTransform.screenToWorld(event.getHistoricalX(h), event.getHistoricalY(h), viewport)
                        val hPressure = if (isStylus) event.getHistoricalPressure(h).coerceIn(0.1f, 1.0f) else 1.0f
                        onInputEvent(CanvasInputEvent.StrokeMove(StrokePoint(hwX, hwY, hPressure, event.getHistoricalEventTime(h))))
                    }

                    val (worldX, worldY) = CoordinateTransform.screenToWorld(event.x, event.y, viewport)
                    val pressure = if (isStylus) event.getPressure(pointerIndex).coerceIn(0.1f, 1.0f) else 1.0f
                    val point = StrokePoint(worldX, worldY, pressure, event.eventTime)
                    onInputEvent(CanvasInputEvent.StrokeMove(point))
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isStrokeActive && activePointerId == pointerId) {
                    val (worldX, worldY) = CoordinateTransform.screenToWorld(event.x, event.y, viewport)
                    val pressure = if (isStylus) event.getPressure(pointerIndex).coerceIn(0.1f, 1.0f) else 1.0f
                    val point = StrokePoint(worldX, worldY, pressure, event.eventTime)
                    onInputEvent(CanvasInputEvent.StrokeEnd(point))
                    activePointerId = -1
                    isStrokeActive = false
                    isStylusActive = false
                    return true
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (isStrokeActive) {
                    onInputEvent(CanvasInputEvent.StrokeCancel)
                    activePointerId = -1
                    isStrokeActive = false
                    isStylusActive = false
                    return true
                }
            }
        }

        return false
    }
}
