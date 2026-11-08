package com.canvotes.feature.canvas.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvotes.core.model.TextboxItem
import com.canvotes.core.model.ViewportState
import kotlin.math.roundToInt

@Composable
fun TextboxOverlay(
    textbox: TextboxItem,
    viewport: ViewportState,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onUpdate: (TextboxItem) -> Unit,
    onDelete: () -> Unit,
    isDarkTheme: Boolean
) {
    val screenX = (textbox.x + viewport.panX) * viewport.zoom
    val screenY = (textbox.y + viewport.panY) * viewport.zoom
    val screenWidth = (textbox.width * viewport.zoom).coerceAtLeast(140f)
    val screenHeight = (textbox.height * viewport.zoom).coerceAtLeast(40f)
    val scaledFontSize = (textbox.fontSize * viewport.zoom).coerceIn(10f, 64f)

    val textColor = if (isDarkTheme) Color.White else Color.Black

    Box(
        modifier = Modifier
            .offset { IntOffset(screenX.roundToInt(), screenY.roundToInt()) }
            .width(screenWidth.dp)
            .heightIn(min = screenHeight.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.85f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .pointerInput(textbox.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val deltaWorldX = dragAmount.x / viewport.zoom
                    val deltaWorldY = dragAmount.y / viewport.zoom
                    onUpdate(
                        textbox.copy(
                            x = textbox.x + deltaWorldX,
                            y = textbox.y + deltaWorldY
                        )
                    )
                }
            }
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column {
                if (isSelected) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            IconButton(
                                onClick = { onUpdate(textbox.copy(isBold = !textbox.isBold)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.FormatBold,
                                    contentDescription = "Bold",
                                    tint = if (textbox.isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                            IconButton(
                                onClick = { onUpdate(textbox.copy(isItalic = !textbox.isItalic)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.FormatItalic,
                                    contentDescription = "Italic",
                                    tint = if (textbox.isItalic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                BasicTextField(
                    value = textbox.text,
                    onValueChange = { newText ->
                        onSelect()
                        onUpdate(textbox.copy(text = newText))
                    },
                    textStyle = TextStyle(
                        fontSize = scaledFontSize.sp,
                        fontWeight = if (textbox.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (textbox.isItalic) FontStyle.Italic else FontStyle.Normal,
                        color = textColor
                    ),
                    cursorBrush = SolidColor(textColor),
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                )
            }
        }
    }
}
