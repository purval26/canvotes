package com.canvotes.feature.canvas.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.canvotes.core.model.CanvasToolMode
import com.canvotes.core.model.ShapeType

@Composable
fun CanvasToolbar(
    currentTool: CanvasToolMode,
    onToolSelected: (CanvasToolMode) -> Unit,
    currentShapeType: ShapeType,
    onShapeTypeSelected: (ShapeType) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onNavigateBack: () -> Unit,
    onAddImage: () -> Unit,
    onImportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showWidthSlider by remember { mutableStateOf(false) }
    var showShapeSelector by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                // Back button
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 2.dp))

                // Tools
                ToolbarToolButton(
                    icon = Icons.Default.PanTool,
                    label = "Pan",
                    isSelected = currentTool == CanvasToolMode.HAND,
                    onClick = { onToolSelected(CanvasToolMode.HAND) }
                )

                ToolbarToolButton(
                    icon = Icons.Default.Edit,
                    label = "Pen",
                    isSelected = currentTool == CanvasToolMode.PEN,
                    onClick = {
                        if (currentTool == CanvasToolMode.PEN) {
                            showWidthSlider = !showWidthSlider
                        } else {
                            onToolSelected(CanvasToolMode.PEN)
                        }
                    }
                )

                ToolbarToolButton(
                    icon = Icons.Default.Brush,
                    label = "Pencil",
                    isSelected = currentTool == CanvasToolMode.PENCIL,
                    onClick = { onToolSelected(CanvasToolMode.PENCIL) }
                )

                ToolbarToolButton(
                    icon = Icons.Default.FormatPaint,
                    label = "Highlighter",
                    isSelected = currentTool == CanvasToolMode.HIGHLIGHTER,
                    onClick = { onToolSelected(CanvasToolMode.HIGHLIGHTER) }
                )

                ToolbarToolButton(
                    icon = Icons.Default.AutoFixNormal,
                    label = "Eraser",
                    isSelected = currentTool == CanvasToolMode.ERASER,
                    onClick = { onToolSelected(CanvasToolMode.ERASER) }
                )

                ToolbarToolButton(
                    icon = Icons.Default.TextFields,
                    label = "Text",
                    isSelected = currentTool == CanvasToolMode.TEXT,
                    onClick = { onToolSelected(CanvasToolMode.TEXT) }
                )

                ToolbarToolButton(
                    icon = Icons.Default.Category,
                    label = "Shape",
                    isSelected = currentTool == CanvasToolMode.SHAPE,
                    onClick = {
                        if (currentTool == CanvasToolMode.SHAPE) {
                            showShapeSelector = !showShapeSelector
                        } else {
                            onToolSelected(CanvasToolMode.SHAPE)
                            showShapeSelector = true
                        }
                    }
                )

                ToolbarToolButton(
                    icon = Icons.Default.Image,
                    label = "Image",
                    isSelected = false,
                    onClick = onAddImage
                )

                ToolbarToolButton(
                    icon = Icons.Default.PictureAsPdf,
                    label = "PDF",
                    isSelected = false,
                    onClick = onImportPdf
                )

                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 2.dp))

                // Undo / Redo
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(onClick = onRedo, enabled = canRedo) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Stroke Width Slider Dropdown
            if (showWidthSlider) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Thickness: ${strokeWidth.toInt()}px",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(96.dp)
                    )
                    Slider(
                        value = strokeWidth,
                        onValueChange = onStrokeWidthChange,
                        valueRange = 1f..32f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Shape Selection Row
            if (showShapeSelector) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    ShapeSelectorChip("Rect", ShapeType.RECTANGLE, currentShapeType) { onShapeTypeSelected(it); showShapeSelector = false }
                    Spacer(Modifier.width(6.dp))
                    ShapeSelectorChip("Round", ShapeType.ROUNDED_RECTANGLE, currentShapeType) { onShapeTypeSelected(it); showShapeSelector = false }
                    Spacer(Modifier.width(6.dp))
                    ShapeSelectorChip("Circle", ShapeType.CIRCLE, currentShapeType) { onShapeTypeSelected(it); showShapeSelector = false }
                    Spacer(Modifier.width(6.dp))
                    ShapeSelectorChip("Line", ShapeType.LINE, currentShapeType) { onShapeTypeSelected(it); showShapeSelector = false }
                    Spacer(Modifier.width(6.dp))
                    ShapeSelectorChip("Arrow", ShapeType.ARROW, currentShapeType) { onShapeTypeSelected(it); showShapeSelector = false }
                }
            }
        }
    }
}

@Composable
private fun ToolbarToolButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ShapeSelectorChip(
    label: String,
    type: ShapeType,
    currentType: ShapeType,
    onSelect: (ShapeType) -> Unit
) {
    FilterChip(
        selected = currentType == type,
        onClick = { onSelect(type) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        shape = RoundedCornerShape(12.dp)
    )
}
