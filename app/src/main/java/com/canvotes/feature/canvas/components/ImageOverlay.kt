package com.canvotes.feature.canvas.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.canvotes.core.model.ImageItem
import com.canvotes.core.model.ViewportState
import java.io.File
import kotlin.math.roundToInt

@Composable
fun ImageOverlay(
    image: ImageItem,
    viewport: ViewportState,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onUpdate: (ImageItem) -> Unit,
    onDelete: () -> Unit
) {
    val screenX = (image.x + viewport.panX) * viewport.zoom
    val screenY = (image.y + viewport.panY) * viewport.zoom
    val screenWidth = (image.width * viewport.zoom).coerceAtLeast(40f)
    val screenHeight = (image.height * viewport.zoom).coerceAtLeast(40f)

    Box(
        modifier = Modifier
            .offset { IntOffset(screenX.roundToInt(), screenY.roundToInt()) }
            .width(screenWidth.dp)
            .height(screenHeight.dp)
            .pointerInput(image.id) {
                detectDragGestures(
                    onDragStart = { onSelect() }
                ) { change, dragAmount ->
                    change.consume()
                    val deltaWorldX = dragAmount.x / viewport.zoom
                    val deltaWorldY = dragAmount.y / viewport.zoom
                    onUpdate(
                        image.copy(
                            x = image.x + deltaWorldX,
                            y = image.y + deltaWorldY
                        )
                    )
                }
            }
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
            color = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = File(image.assetPath),
                contentDescription = "Canvas Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp))
            )
        }

        if (isSelected) {
            // Delete button in top corner
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .clip(CircleShape)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Delete Image",
                        modifier = Modifier.padding(4.dp).size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Resize handle in bottom-right corner (preserving aspect ratio)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .offset(6.dp, 6.dp)
                    .clip(CircleShape)
                    .pointerInput(image.id + "_resize") {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaWorldX = dragAmount.x / viewport.zoom
                            val aspectRatio = if (image.height > 0) image.width / image.height else 1.0f
                            val newWidth = (image.width + deltaWorldX).coerceAtLeast(80f)
                            val newHeight = newWidth / aspectRatio
                            onUpdate(image.copy(width = newWidth, height = newHeight))
                        }
                    }
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                ) {}
            }
        }
    }
}
