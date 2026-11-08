package com.canvotes.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class StrokeTool {
    PEN,
    PENCIL,
    HIGHLIGHTER,
    ERASER
}

@Serializable
enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE,
    ELLIPSE,
    LINE,
    ARROW
}

@Serializable
enum class CanvasToolMode {
    HAND,       // Pan / zoom only
    PEN,
    PENCIL,
    HIGHLIGHTER,
    ERASER,
    TEXT,
    SHAPE,
    IMAGE,
    LASSO
}

@Serializable
enum class CanvasThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Serializable
data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Stroke(
    val id: String = UUID.randomUUID().toString(),
    val tool: StrokeTool = StrokeTool.PEN,
    val points: List<StrokePoint> = emptyList(),
    val strokeWidth: Float = 3.0f,
    val colorHex: String = "#000000",
    val opacity: Float = 1.0f
)

@Serializable
data class ShapeItem(
    val id: String = UUID.randomUUID().toString(),
    val type: ShapeType = ShapeType.RECTANGLE,
    val startX: Float = 0f,
    val startY: Float = 0f,
    val endX: Float = 100f,
    val endY: Float = 100f,
    val strokeWidth: Float = 3.0f,
    val strokeColorHex: String = "#000000",
    val fillColorHex: String? = null,
    val isFilled: Boolean = false
)

@Serializable
data class TextboxItem(
    val id: String = UUID.randomUUID().toString(),
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 240f,
    val height: Float = 80f,
    val text: String = "",
    val fontSize: Float = 16f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val colorHex: String = "#000000",
    val textAlign: String = "START"
)

@Serializable
data class ImageItem(
    val id: String = UUID.randomUUID().toString(),
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 300f,
    val height: Float = 300f,
    val assetPath: String = "",
    val originalWidth: Int = 0,
    val originalHeight: Int = 0
)

@Serializable
data class PdfPageItem(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String = "",
    val pageIndex: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 612f,
    val height: Float = 792f,
    val assetPath: String = ""
)

@Serializable
data class ViewportState(
    val panX: Float = 0f,
    val panY: Float = 0f,
    val zoom: Float = 1.0f
) {
    fun toScreenX(worldX: Float): Float = (worldX + panX) * zoom
    fun toScreenY(worldY: Float): Float = (worldY + panY) * zoom
    fun toWorldX(screenX: Float): Float = (screenX / zoom) - panX
    fun toWorldY(screenY: Float): Float = (screenY / zoom) - panY
}

@Serializable
data class CanvasDocument(
    val version: Int = 1,
    val id: String = UUID.randomUUID().toString(),
    val pageId: String = "",
    val viewport: ViewportState = ViewportState(),
    val strokes: List<Stroke> = emptyList(),
    val shapes: List<ShapeItem> = emptyList(),
    val textboxes: List<TextboxItem> = emptyList(),
    val images: List<ImageItem> = emptyList(),
    val pdfPages: List<PdfPageItem> = emptyList(),
    val lastModified: Long = System.currentTimeMillis()
)
