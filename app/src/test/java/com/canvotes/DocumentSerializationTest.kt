package com.canvotes

import com.canvotes.core.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class DocumentSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testSerializationAndDeserialization() {
        val originalDoc = CanvasDocument(
            version = 1,
            pageId = "test-page-123",
            viewport = ViewportState(panX = 250f, panY = -120f, zoom = 1.25f),
            strokes = listOf(
                Stroke(
                    tool = StrokeTool.PEN,
                    points = listOf(StrokePoint(10f, 20f, 0.8f), StrokePoint(30f, 40f, 0.9f)),
                    strokeWidth = 4.5f,
                    colorHex = "#000000"
                )
            ),
            shapes = listOf(
                ShapeItem(
                    type = ShapeType.ROUNDED_RECTANGLE,
                    startX = 0f,
                    startY = 0f,
                    endX = 100f,
                    endY = 50f
                )
            ),
            textboxes = listOf(
                TextboxItem(
                    text = "Hello Canvotes",
                    x = 50f,
                    y = 60f,
                    isBold = true
                )
            ),
            images = listOf(
                ImageItem(
                    assetPath = "/data/data/com.canvotes/files/assets/sample.png",
                    width = 200f,
                    height = 150f
                )
            ),
            pdfPages = listOf(
                PdfPageItem(
                    documentId = "pdf-doc-1",
                    pageIndex = 0,
                    assetPath = "/data/data/com.canvotes/files/assets/document.pdf"
                )
            )
        )

        val serialized = json.encodeToString(originalDoc)
        assertTrue(serialized.contains("test-page-123"))
        assertTrue(serialized.contains("Hello Canvotes"))

        val deserialized = json.decodeFromString<CanvasDocument>(serialized)
        assertEquals(originalDoc.pageId, deserialized.pageId)
        assertEquals(originalDoc.viewport.zoom, deserialized.viewport.zoom, 0.001f)
        assertEquals(1, deserialized.strokes.size)
        assertEquals(StrokeTool.PEN, deserialized.strokes[0].tool)
        assertEquals(1, deserialized.shapes.size)
        assertEquals(ShapeType.ROUNDED_RECTANGLE, deserialized.shapes[0].type)
        assertEquals("Hello Canvotes", deserialized.textboxes[0].text)
        assertEquals(1, deserialized.images.size)
        assertEquals(1, deserialized.pdfPages.size)
    }
}
