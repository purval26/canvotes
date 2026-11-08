package com.canvotes.feature.canvas.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.canvotes.core.math.CoordinateTransform
import com.canvotes.core.model.*
import com.canvotes.design.theme.CanvasBgDark
import com.canvotes.design.theme.CanvasBgLight
import com.canvotes.feature.canvas.components.ImageOverlay
import com.canvotes.feature.canvas.components.TextboxOverlay

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun InfiniteCanvas(
    document: CanvasDocument,
    activeStroke: Stroke?,
    activeShape: ShapeItem?,
    currentTool: CanvasToolMode,
    selectedObjectId: String?,
    onSelectObject: (String?) -> Unit,
    onTransformViewport: (panDelta: Offset, zoomFactor: Float) -> Unit,
    onUpdateDocument: (CanvasDocument) -> Unit,
    onStylusEvent: (CanvasInputEvent) -> Unit,
    inputProcessor: StylusInputProcessor,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) CanvasBgDark else CanvasBgLight
    val viewport = document.viewport

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            // Gesture detector for two-finger pan/zoom or Hand tool navigation
            .pointerInput(currentTool) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (currentTool == CanvasToolMode.HAND || zoom != 1.0f) {
                        onTransformViewport(pan, zoom)
                    }
                }
            }
            // Pointer interop filter to capture Android MotionEvent with stylus tool type and pressure
            .pointerInteropFilter { motionEvent ->
                if (currentTool != CanvasToolMode.HAND) {
                    inputProcessor.processMotionEvent(motionEvent, viewport) { event ->
                        onStylusEvent(event)
                    }
                } else {
                    false
                }
            }
    ) {
        // Base Canvas Surface: Polka Dots, Strokes, Shapes, and Active In-Progress Strokes
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Draw polka-dot infinite grid
            PolkaDotGridRenderer.drawGrid(this, viewport, isDark)

            // 2. Draw committed strokes
            for (stroke in document.strokes) {
                StrokeRenderer.drawStroke(this, stroke, viewport, isDark)
            }

            // 3. Draw committed shapes
            for (shape in document.shapes) {
                StrokeRenderer.drawShape(this, shape, viewport, isDark)
            }

            // 4. Draw currently active stroke in progress
            if (activeStroke != null) {
                StrokeRenderer.drawStroke(this, activeStroke, viewport, isDark)
            }

            // 5. Draw currently active shape in progress
            if (activeShape != null) {
                StrokeRenderer.drawShape(this, activeShape, viewport, isDark)
            }
        }

        // Overlays: Images
        for (image in document.images) {
            ImageOverlay(
                image = image,
                viewport = viewport,
                isSelected = selectedObjectId == image.id,
                onSelect = { onSelectObject(image.id) },
                onUpdate = { updated ->
                    val newImages = document.images.map { if (it.id == updated.id) updated else it }
                    onUpdateDocument(document.copy(images = newImages))
                },
                onDelete = {
                    val newImages = document.images.filter { it.id != image.id }
                    onUpdateDocument(document.copy(images = newImages))
                    onSelectObject(null)
                }
            )
        }

        // Overlays: Textboxes
        for (textbox in document.textboxes) {
            TextboxOverlay(
                textbox = textbox,
                viewport = viewport,
                isSelected = selectedObjectId == textbox.id,
                onSelect = { onSelectObject(textbox.id) },
                onUpdate = { updated ->
                    val newTextboxes = document.textboxes.map { if (it.id == updated.id) updated else it }
                    onUpdateDocument(document.copy(textboxes = newTextboxes))
                },
                onDelete = {
                    val newTextboxes = document.textboxes.filter { it.id != textbox.id }
                    onUpdateDocument(document.copy(textboxes = newTextboxes))
                    onSelectObject(null)
                },
                isDarkTheme = isDark
            )
        }
    }
}
