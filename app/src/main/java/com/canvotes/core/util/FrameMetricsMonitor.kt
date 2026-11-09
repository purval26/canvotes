package com.canvotes.core.util

import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FrameMetricsMonitor {
    private var isRunning = false
    private var lastFrameTimeNanos: Long = 0L
    private var frameCount: Int = 0
    private var fpsStartTimeNanos: Long = 0L

    private val _fps = MutableStateFlow(60f)
    val fps: StateFlow<Float> = _fps.asStateFlow()

    private val _frameTimeMs = MutableStateFlow(8.33f)
    val frameTimeMs: StateFlow<Float> = _frameTimeMs.asStateFlow()

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return

            if (lastFrameTimeNanos > 0) {
                val deltaNanos = frameTimeNanos - lastFrameTimeNanos
                val deltaMs = (deltaNanos / 1_000_000f).coerceIn(0.1f, 100f)
                _frameTimeMs.value = deltaMs

                frameCount++
                val elapsedSinceFpsStart = frameTimeNanos - fpsStartTimeNanos
                if (elapsedSinceFpsStart >= 1_000_000_000L) { // 1 second rolling window
                    val calculatedFps = (frameCount * 1_000_000_000f) / elapsedSinceFpsStart
                    _fps.value = calculatedFps.coerceIn(10f, 240f)
                    frameCount = 0
                    fpsStartTimeNanos = frameTimeNanos
                }
            } else {
                fpsStartTimeNanos = frameTimeNanos
            }

            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        lastFrameTimeNanos = 0L
        frameCount = 0
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}
