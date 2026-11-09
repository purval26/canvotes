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

/**
 * Unified pointer event architecture for Canvotes.
 *
 * Rules:
 * - Single stylus pointer draws.
 * - Single finger pans by default (or draws when fingerDrawingEnabled is true).
 * - Two fingers perform simultaneous pan and pinch-zoom in any mode.
 * - When a second pointer arrives during active drawing, the active stroke is safely cancelled
 *   (dispatched as [CanvasInputEvent.StrokeCancel]) without document corruption, transitioning
 *   seamlessly into two-finger pan/zoom navigation.
 * - Accidental palm contact is filtered out using touch major/minor dimensions.
 * - High-refresh-rate historical motion event coordinates are extracted for 120Hz fidelity.
 */
class StylusInputProcessor(
    private var palmRejectionEnabled: Boolean = true,
    private var fingerDrawingEnabled: Boolean = false
) {
    private var activePointerId: Int = -1
    private var isStrokeActive: Boolean = false
    private var isStylusActive: Boolean = false
    private var isSingleFingerPanning: Boolean = false
    private var isMultiTouch: Boolean = false

    // Multi-touch tracking
    private var lastCentroidX: Float = 0f
    private var lastCentroidY: Float = 0f
    private var lastDistance: Float = 0f

    // Single-finger / Hand-tool pan tracking
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
            // Cancel any single-pointer stroke safely without document corruption
            if (isStrokeActive) {
                onInputEvent(CanvasInputEvent.StrokeCancel)
                isStrokeActive = false
                activePointerId = -1
                isStylusActive = false
            }
            isSingleFingerPanning = false

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
                    // One finger lifted during multi-touch; keep multi-touch flag until all lift
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
        // Prevent remaining finger from drawing when lifting from pinch-zoom
        if (isMultiTouch) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                isMultiTouch = false
            }
            return true
        }

        // ---------------------------------------------------------
        // 3. SINGLE POINTER DISPATCH
        // ---------------------------------------------------------
        val pointerIndex = event.actionIndex
        val toolType = if (action == MotionEvent.ACTION_DOWN) {
            event.getToolType(0)
        } else if (activePointerId != -1) {
            val idx = event.findPointerIndex(activePointerId)
            if (idx >= 0) event.getToolType(idx) else event.getToolType(0)
        } else {
            event.getToolType(pointerIndex)
        }

        val isStylus = toolType == MotionEvent.TOOL_TYPE_STYLUS || toolType == MotionEvent.TOOL_TYPE_ERASER
        val isFinger = !isStylus

        // Palm rejection heuristic for large touch contacts
        if (palmRejectionEnabled && isFinger) {
            val touchMajor = event.getTouchMajor(pointerIndex)
            val touchMinor = event.getTouchMinor(pointerIndex)
            if (touchMajor > 180f || (touchMajor > 120f && touchMinor > 80f)) {
                return true // Accidental palm touch suppressed
            }
        }

        // Determine behavior: Draw vs Pan
        val shouldDraw = !isHandMode && (isStylus || fingerDrawingEnabled)

        if (!shouldDraw) {
            // SINGLE POINTER NAVIGATION (Hand mode OR single finger pan default)
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    activePointerId = event.getPointerId(0)
                    lastPanX = event.getX(0)
                    lastPanY = event.getY(0)
                    isSingleFingerPanning = true
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isSingleFingerPanning && activePointerId != -1) {
                        val idx = event.findPointerIndex(activePointerId)
                        if (idx >= 0) {
                            val curX = event.getX(idx)
                            val curY = event.getY(idx)
                            val dx = curX - lastPanX
                            val dy = curY - lastPanY
                            lastPanX = curX
                            lastPanY = curY
                            onInputEvent(CanvasInputEvent.PanZoom(dx, dy, 1.0f))
                            return true
                        }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isSingleFingerPanning = false
                    activePointerId = -1
                    return true
                }
            }
            return false
        }

        // SINGLE POINTER DRAWING (Stylus OR finger-draw mode)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                isStrokeActive = true
                isStylusActive = isStylus

                val (worldX, worldY) = CoordinateTransform.screenToWorld(event.getX(0), event.getY(0), viewport)
                val pressure = if (isStylus) event.getPressure(0).coerceIn(0.1f, 1.0f) else 1.0f
                val point = StrokePoint(worldX, worldY, pressure, event.eventTime)
                onInputEvent(CanvasInputEvent.StrokeStart(point, isStylus))
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isStrokeActive && activePointerId != -1) {
                    val idx = event.findPointerIndex(activePointerId)
                    if (idx >= 0) {
                        // Extract historical points for 120Hz display precision
                        val historySize = event.historySize
                        for (h in 0 until historySize) {
                            val (hwX, hwY) = CoordinateTransform.screenToWorld(
                                event.getHistoricalX(idx, h),
                                event.getHistoricalY(idx, h),
                                viewport
                            )
                            val hp = if (isStylus) event.getHistoricalPressure(idx, h).coerceIn(0.1f, 1.0f) else 1.0f
                            onInputEvent(
                                CanvasInputEvent.StrokeMove(
                                    StrokePoint(hwX, hwY, hp, event.getHistoricalEventTime(h))
                                )
                            )
                        }

                        val (worldX, worldY) = CoordinateTransform.screenToWorld(event.getX(idx), event.getY(idx), viewport)
                        val pressure = if (isStylus) event.getPressure(idx).coerceIn(0.1f, 1.0f) else 1.0f
                        val point = StrokePoint(worldX, worldY, pressure, event.eventTime)
                        onInputEvent(CanvasInputEvent.StrokeMove(point))
                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isStrokeActive && activePointerId != -1) {
                    val idx = event.findPointerIndex(activePointerId)
                    val (worldX, worldY) = if (idx >= 0) {
                        CoordinateTransform.screenToWorld(event.getX(idx), event.getY(idx), viewport)
                    } else {
                        CoordinateTransform.screenToWorld(event.x, event.y, viewport)
                    }
                    val pressure = if (isStylus && idx >= 0) event.getPressure(idx).coerceIn(0.1f, 1.0f) else 1.0f
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
