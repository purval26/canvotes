package com.canvotes.feature.canvas.engine

import android.view.MotionEvent
import com.canvotes.core.math.CoordinateTransform
import com.canvotes.core.model.StrokePoint
import com.canvotes.core.model.ViewportState

sealed interface CanvasInputEvent {
    data class StrokeStart(val point: StrokePoint, val isStylus: Boolean) : CanvasInputEvent
    data class StrokeMove(val point: StrokePoint) : CanvasInputEvent
    data class StrokeEnd(val point: StrokePoint) : CanvasInputEvent
    data class PanZoom(val deltaX: Float, val deltaY: Float, val zoomFactor: Float) : CanvasInputEvent
    data class Tap(val point: StrokePoint) : CanvasInputEvent
}

class StylusInputProcessor(
    private var palmRejectionEnabled: Boolean = true,
    private var fingerDrawingEnabled: Boolean = false
) {
    private var activePointerId: Int = -1
    private var isStylusActive: Boolean = false
    private var lastScreenX: Float = 0f
    private var lastScreenY: Float = 0f

    fun updateSettings(palmRejection: Boolean, fingerDrawing: Boolean) {
        this.palmRejectionEnabled = palmRejection
        this.fingerDrawingEnabled = fingerDrawing
    }

    fun processMotionEvent(
        event: MotionEvent,
        viewport: ViewportState,
        onInputEvent: (CanvasInputEvent) -> Unit
    ): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)
        val toolType = event.getToolType(pointerIndex)

        val isStylus = toolType == MotionEvent.TOOL_TYPE_STYLUS || toolType == MotionEvent.TOOL_TYPE_ERASER
        val isFinger = toolType == MotionEvent.TOOL_TYPE_FINGER || toolType == MotionEvent.TOOL_TYPE_UNKNOWN

        // Palm rejection heuristic:
        // 1. If stylus is currently drawing, ignore all finger pointers (they are likely palm).
        if (isStylusActive && isFinger) {
            return true // Consumed / rejected
        }

        // 2. Size / pressure palm detection on touch surfaces
        if (palmRejectionEnabled && isFinger && !fingerDrawingEnabled) {
            val touchMajor = event.getTouchMajor(pointerIndex)
            val touchMinor = event.getTouchMinor(pointerIndex)
            if (touchMajor > 120f || (touchMajor > 80f && touchMinor > 60f)) {
                // Large contact area => palm resting on screen
                return true
            }
        }

        // Multi-touch gestures (2 or more fingers) => Pan and Zoom
        if (event.pointerCount >= 2) {
            // Cancel any single-finger drawing if it transitioned to pinch
            if (activePointerId != -1 && !isStylus) {
                val (wx, wy) = CoordinateTransform.screenToWorld(lastScreenX, lastScreenY, viewport)
                onInputEvent(CanvasInputEvent.StrokeEnd(StrokePoint(wx, wy)))
                activePointerId = -1
            }
            return false // Let Compose detectTransformGestures handle pan and zoom
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (isStylus || fingerDrawingEnabled) {
                    activePointerId = pointerId
                    isStylusActive = isStylus
                    val screenX = event.x
                    val screenY = event.y
                    lastScreenX = screenX
                    lastScreenY = screenY

                    val (worldX, worldY) = CoordinateTransform.screenToWorld(screenX, screenY, viewport)
                    val pressure = if (isStylus) event.getPressure(pointerIndex).coerceIn(0.1f, 1.0f) else 1.0f
                    val point = StrokePoint(worldX, worldY, pressure, System.currentTimeMillis())
                    onInputEvent(CanvasInputEvent.StrokeStart(point, isStylus))
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == pointerId && (isStylus || fingerDrawingEnabled)) {
                    val screenX = event.x
                    val screenY = event.y
                    lastScreenX = screenX
                    lastScreenY = screenY

                    // Historical batch points for maximum input fidelity and low latency
                    val historySize = event.historySize
                    for (h in 0 until historySize) {
                        val hX = event.getHistoricalX(h)
                        val hY = event.getHistoricalY(h)
                        val hPressure = if (isStylus) event.getHistoricalPressure(h).coerceIn(0.1f, 1.0f) else 1.0f
                        val (hwX, hwY) = CoordinateTransform.screenToWorld(hX, hY, viewport)
                        onInputEvent(CanvasInputEvent.StrokeMove(StrokePoint(hwX, hwY, hPressure, event.getHistoricalEventTime(h))))
                    }

                    val (worldX, worldY) = CoordinateTransform.screenToWorld(screenX, screenY, viewport)
                    val pressure = if (isStylus) event.getPressure(pointerIndex).coerceIn(0.1f, 1.0f) else 1.0f
                    val point = StrokePoint(worldX, worldY, pressure, event.eventTime)
                    onInputEvent(CanvasInputEvent.StrokeMove(point))
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (activePointerId == pointerId) {
                    val screenX = event.x
                    val screenY = event.y
                    val (worldX, worldY) = CoordinateTransform.screenToWorld(screenX, screenY, viewport)
                    val pressure = if (isStylus) event.getPressure(pointerIndex).coerceIn(0.1f, 1.0f) else 1.0f
                    val point = StrokePoint(worldX, worldY, pressure, event.eventTime)
                    onInputEvent(CanvasInputEvent.StrokeEnd(point))
                    activePointerId = -1
                    isStylusActive = false
                    return true
                }
            }
        }

        return false
    }
}
