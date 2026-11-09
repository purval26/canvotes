package com.canvotes.feature.canvas.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
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
            // Single unified high-performance pointer processor
            // Directly handles multi-touch pan/zoom and single-finger/stylus drawing without gesture conflicts
            .pointerInteropFilter { motionEvent ->
                inputProcessor.processMotionEvent(
                    event = motionEvent,
                    viewport = viewport,
                    isHandMode = currentTool == CanvasToolMode.HAND
                ) { event ->
                    if (event is CanvasInputEvent.PanZoom) {
                        onTransformViewport(Offset(event.deltaX, event.deltaY), event.zoomFactor)
                    } else {
                        onStylusEvent(event)
                    }
                }
            }
    ) {
        // LAYER 1: Background Polka-dot Infinite Grid
        // Cached in a dedicated hardware graphicsLayer; ONLY redrawn when viewport or theme changes
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer()
        ) {
            PolkaDotGridRenderer.drawGrid(this, viewport, isDark)
        }

        // LAYER 2: Committed Content (Document strokes & shapes)
        // Cached in a dedicated hardware graphicsLayer; ONLY redrawn when document commits
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer()
        ) {
            for (stroke in document.strokes) {
                StrokeRenderer.drawStroke(this, stroke, viewport, isDark)
            }
            for (shape in document.shapes) {
                StrokeRenderer.drawShape(this, shape, viewport, isDark)
            }
        }

        // LAYER 3: Active In-Progress Stroke / Shape Overlay
        // Lightweight real-time canvas dedicated to the stroke in flight; zero background re-renders
        if (activeStroke != null || activeShape != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (activeStroke != null) {
                    StrokeRenderer.drawStroke(this, activeStroke, viewport, isDark)
                }
                if (activeShape != null) {
                    StrokeRenderer.drawShape(this, activeShape, viewport, isDark)
                }
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
