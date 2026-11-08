package com.canvotes.feature.canvas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canvotes.core.model.CanvasToolMode
import com.canvotes.feature.canvas.components.CanvasToolbar
import com.canvotes.feature.canvas.components.PageQuickNav
import com.canvotes.feature.canvas.engine.InfiniteCanvas

@Composable
fun CanvasScreen(
    viewModel: CanvasViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val document by viewModel.document.collectAsState()
    val currentTool by viewModel.currentTool.collectAsState()
    val currentShapeType by viewModel.currentShapeType.collectAsState()
    val strokeWidth by viewModel.strokeWidth.collectAsState()
    val activeStroke by viewModel.activeStroke.collectAsState()
    val activeShape by viewModel.activeShape.collectAsState()
    val selectedObjectId by viewModel.selectedObjectId.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val pages by viewModel.pages.collectAsState()
    val currentPageId by viewModel.currentPageId.collectAsState()

    val currentPageIndex = pages.indexOfFirst { it.id == currentPageId }.let { if (it == -1) 1 else it + 1 }
    val totalPages = maxOf(pages.size, 1)

    // Image Picker Launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importImage(it) }
    }

    // PDF Picker Launcher
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importPdf(it) }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Infinite Canvas Engine
            InfiniteCanvas(
                document = document,
                activeStroke = activeStroke,
                activeShape = activeShape,
                currentTool = currentTool,
                selectedObjectId = selectedObjectId,
                onSelectObject = { viewModel.selectObject(it) },
                onTransformViewport = { pan, zoom -> viewModel.transformViewport(pan, zoom) },
                onUpdateDocument = { viewModel.updateDocument(it) },
                onStylusEvent = { viewModel.handleStylusEvent(it) },
                inputProcessor = viewModel.inputProcessor,
                modifier = Modifier.fillMaxSize()
            )

            // Top Floating Toolbar
            CanvasToolbar(
                currentTool = currentTool,
                onToolSelected = { tool ->
                    if (tool == CanvasToolMode.TEXT) {
                        viewModel.addTextboxAtCenter()
                    } else {
                        viewModel.setTool(tool)
                    }
                },
                currentShapeType = currentShapeType,
                onShapeTypeSelected = { viewModel.setShapeType(it) },
                strokeWidth = strokeWidth,
                onStrokeWidthChange = { viewModel.setStrokeWidth(it) },
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onNavigateBack = onNavigateBack,
                onAddImage = { imagePicker.launch("image/*") },
                onImportPdf = { pdfPicker.launch("application/pdf") },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            )

            // Bottom Quick Page Navigation & Auto-Save Indicator
            PageQuickNav(
                currentPageNumber = currentPageIndex,
                totalPages = totalPages,
                onPreviousPage = { viewModel.navigateToPreviousPage() },
                onNextPage = { viewModel.navigateToNextPage() },
                onAddPage = { viewModel.addNewPage() },
                saveStatusText = saveStatus,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}
