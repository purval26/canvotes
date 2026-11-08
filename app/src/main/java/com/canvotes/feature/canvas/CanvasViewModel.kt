package com.canvotes.feature.canvas

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.canvotes.core.history.CanvasCommand
import com.canvotes.core.history.CommandHistory
import com.canvotes.core.math.BezierSmoother
import com.canvotes.core.math.CoordinateTransform
import com.canvotes.core.model.*
import com.canvotes.data.preferences.UserPreferencesRepository
import com.canvotes.domain.model.Page
import com.canvotes.domain.repository.CanvasRepository
import com.canvotes.domain.repository.NotebookRepository
import com.canvotes.feature.canvas.engine.CanvasInputEvent
import com.canvotes.feature.canvas.engine.StylusInputProcessor
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(FlowPreview::class)
class CanvasViewModel(
    val notebookId: String,
    val initialPageId: String,
    private val notebookRepository: NotebookRepository,
    private val canvasRepository: CanvasRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _currentPageId = MutableStateFlow(initialPageId)
    val currentPageId: StateFlow<String> = _currentPageId.asStateFlow()

    private val _document = MutableStateFlow(CanvasDocument(pageId = initialPageId))
    val document: StateFlow<CanvasDocument> = _document.asStateFlow()

    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    private val _currentTool = MutableStateFlow(CanvasToolMode.PEN)
    val currentTool: StateFlow<CanvasToolMode> = _currentTool.asStateFlow()

    private val _currentShapeType = MutableStateFlow(ShapeType.RECTANGLE)
    val currentShapeType: StateFlow<ShapeType> = _currentShapeType.asStateFlow()

    private val _strokeWidth = MutableStateFlow(3.0f)
    val strokeWidth: StateFlow<Float> = _strokeWidth.asStateFlow()

    private val _activeStroke = MutableStateFlow<Stroke?>(null)
    val activeStroke: StateFlow<Stroke?> = _activeStroke.asStateFlow()

    private val _activeShape = MutableStateFlow<ShapeItem?>(null)
    val activeShape: StateFlow<ShapeItem?> = _activeShape.asStateFlow()

    private val _selectedObjectId = MutableStateFlow<String?>(null)
    val selectedObjectId: StateFlow<String?> = _selectedObjectId.asStateFlow()

    private val _saveStatus = MutableStateFlow("Saved")
    val saveStatus: StateFlow<String> = _saveStatus.asStateFlow()

    private val commandHistory = CommandHistory()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    val inputProcessor = StylusInputProcessor()

    init {
        loadPages()
        loadDocument(initialPageId)
        observePreferences()

        // Debounced auto-save pipeline (triggers 800ms after last modification)
        viewModelScope.launch {
            _document
                .debounce(800)
                .collect { doc ->
                    if (doc.pageId.isNotEmpty()) {
                        _saveStatus.value = "Saving..."
                        val success = canvasRepository.savePageDocument(doc)
                        _saveStatus.value = if (success) "Saved" else "Save error"
                    }
                }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                _strokeWidth.value = prefs.defaultPenWidth
                inputProcessor.updateSettings(
                    palmRejection = prefs.palmRejectionEnabled,
                    fingerDrawing = prefs.fingerDrawingEnabled
                )
            }
        }
    }

    private fun loadPages() {
        viewModelScope.launch {
            notebookRepository.getPages(notebookId).collect { list ->
                _pages.value = list
            }
        }
    }

    fun loadDocument(pageId: String) {
        viewModelScope.launch {
            _currentPageId.value = pageId
            _saveStatus.value = "Loading..."
            val doc = canvasRepository.loadPageDocument(pageId)
            _document.value = doc
            commandHistory.clear()
            updateHistoryState()
            _saveStatus.value = "Saved"
            preferencesRepository.setLastSession(notebookId, pageId)
        }
    }

    fun setTool(tool: CanvasToolMode) {
        _currentTool.value = tool
        _selectedObjectId.value = null
    }

    fun setShapeType(type: ShapeType) {
        _currentShapeType.value = type
    }

    fun setStrokeWidth(width: Float) {
        _strokeWidth.value = width
    }

    fun selectObject(id: String?) {
        _selectedObjectId.value = id
    }

    fun transformViewport(panDelta: Offset, zoomFactor: Float) {
        val current = _document.value.viewport
        val newZoom = (current.zoom * zoomFactor).coerceIn(0.1f, 5.0f)
        val newPanX = current.panX + (panDelta.x / newZoom)
        val newPanY = current.panY + (panDelta.y / newZoom)
        _document.value = _document.value.copy(
            viewport = ViewportState(newPanX, newPanY, newZoom)
        )
    }

    fun updateDocument(newDoc: CanvasDocument) {
        val oldDoc = _document.value
        executeCommand(object : CanvasCommand {
            override val description = "Update canvas"
            override fun execute() {
                _document.value = newDoc
            }
            override fun undo() {
                _document.value = oldDoc
            }
        })
    }

    fun handleStylusEvent(event: CanvasInputEvent) {
        when (event) {
            is CanvasInputEvent.StrokeStart -> {
                when (_currentTool.value) {
                    CanvasToolMode.PEN, CanvasToolMode.PENCIL, CanvasToolMode.HIGHLIGHTER -> {
                        val strokeTool = when (_currentTool.value) {
                            CanvasToolMode.PEN -> StrokeTool.PEN
                            CanvasToolMode.PENCIL -> StrokeTool.PENCIL
                            CanvasToolMode.HIGHLIGHTER -> StrokeTool.HIGHLIGHTER
                            else -> StrokeTool.PEN
                        }
                        val opacity = if (strokeTool == StrokeTool.HIGHLIGHTER) 0.35f else 1.0f
                        _activeStroke.value = Stroke(
                            tool = strokeTool,
                            points = listOf(event.point),
                            strokeWidth = _strokeWidth.value,
                            colorHex = "#000000",
                            opacity = opacity
                        )
                    }
                    CanvasToolMode.ERASER -> {
                        eraseStrokesAt(event.point.x, event.point.y)
                    }
                    CanvasToolMode.SHAPE -> {
                        _activeShape.value = ShapeItem(
                            type = _currentShapeType.value,
                            startX = event.point.x,
                            startY = event.point.y,
                            endX = event.point.x,
                            endY = event.point.y,
                            strokeWidth = _strokeWidth.value,
                            strokeColorHex = "#000000"
                        )
                    }
                    else -> Unit
                }
            }
            is CanvasInputEvent.StrokeMove -> {
                val currentStroke = _activeStroke.value
                if (currentStroke != null) {
                    val updatedPoints = currentStroke.points + event.point
                    _activeStroke.value = currentStroke.copy(points = updatedPoints)
                }

                val currentShape = _activeShape.value
                if (currentShape != null) {
                    _activeShape.value = currentShape.copy(endX = event.point.x, endY = event.point.y)
                }

                if (_currentTool.value == CanvasToolMode.ERASER) {
                    eraseStrokesAt(event.point.x, event.point.y)
                }
            }
            is CanvasInputEvent.StrokeEnd -> {
                val stroke = _activeStroke.value
                if (stroke != null && stroke.points.isNotEmpty()) {
                    val smoothedPoints = BezierSmoother.filterJitter(stroke.points)
                    val finalizedStroke = stroke.copy(points = smoothedPoints)
                    addStroke(finalizedStroke)
                    _activeStroke.value = null
                }

                val shape = _activeShape.value
                if (shape != null) {
                    addShape(shape)
                    _activeShape.value = null
                }
            }
            else -> Unit
        }
    }

    private fun addStroke(stroke: Stroke) {
        val currentStrokes = _document.value.strokes
        executeCommand(object : CanvasCommand {
            override val description = "Add stroke"
            override fun execute() {
                _document.value = _document.value.copy(
                    strokes = currentStrokes + stroke,
                    lastModified = System.currentTimeMillis()
                )
            }
            override fun undo() {
                _document.value = _document.value.copy(
                    strokes = currentStrokes,
                    lastModified = System.currentTimeMillis()
                )
            }
        })
    }

    private fun addShape(shape: ShapeItem) {
        val currentShapes = _document.value.shapes
        executeCommand(object : CanvasCommand {
            override val description = "Add shape"
            override fun execute() {
                _document.value = _document.value.copy(
                    shapes = currentShapes + shape,
                    lastModified = System.currentTimeMillis()
                )
            }
            override fun undo() {
                _document.value = _document.value.copy(
                    shapes = currentShapes,
                    lastModified = System.currentTimeMillis()
                )
            }
        })
    }

    private fun eraseStrokesAt(worldX: Float, worldY: Float) {
        val tolerance = _strokeWidth.value * 2.5f
        val strokes = _document.value.strokes
        val hitStrokes = strokes.filter { stroke ->
            CoordinateTransform.isStrokeIntersectingPoint(stroke, worldX, worldY, tolerance)
        }
        if (hitStrokes.isNotEmpty()) {
            val remaining = strokes.filterNot { hitStrokes.contains(it) }
            executeCommand(object : CanvasCommand {
                override val description = "Erase strokes"
                override fun execute() {
                    _document.value = _document.value.copy(strokes = remaining)
                }
                override fun undo() {
                    _document.value = _document.value.copy(strokes = strokes)
                }
            })
        }
    }

    fun addTextboxAtCenter() {
        val viewport = _document.value.viewport
        val centerX = (-viewport.panX) + 150f
        val centerY = (-viewport.panY) + 150f

        val newTextbox = TextboxItem(
            id = UUID.randomUUID().toString(),
            x = centerX,
            y = centerY,
            text = "Note..."
        )
        val currentTextboxes = _document.value.textboxes
        executeCommand(object : CanvasCommand {
            override val description = "Add textbox"
            override fun execute() {
                _document.value = _document.value.copy(textboxes = currentTextboxes + newTextbox)
                _selectedObjectId.value = newTextbox.id
            }
            override fun undo() {
                _document.value = _document.value.copy(textboxes = currentTextboxes)
                _selectedObjectId.value = null
            }
        })
    }

    fun importImage(uri: Uri) {
        viewModelScope.launch {
            val path = canvasRepository.importImage(uri) ?: return@launch
            val viewport = _document.value.viewport
            val centerX = (-viewport.panX) + 100f
            val centerY = (-viewport.panY) + 100f

            val imageItem = ImageItem(
                id = UUID.randomUUID().toString(),
                x = centerX,
                y = centerY,
                width = 300f,
                height = 300f,
                assetPath = path
            )
            val currentImages = _document.value.images
            executeCommand(object : CanvasCommand {
                override val description = "Add image"
                override fun execute() {
                    _document.value = _document.value.copy(images = currentImages + imageItem)
                    _selectedObjectId.value = imageItem.id
                }
                override fun undo() {
                    _document.value = _document.value.copy(images = currentImages)
                    _selectedObjectId.value = null
                }
            })
        }
    }

    fun importPdf(uri: Uri) {
        viewModelScope.launch {
            val path = canvasRepository.importPdf(uri) ?: return@launch
            val viewport = _document.value.viewport
            val centerX = (-viewport.panX) + 50f
            val centerY = (-viewport.panY) + 50f

            val pdfItem = PdfPageItem(
                id = UUID.randomUUID().toString(),
                documentId = UUID.randomUUID().toString(),
                pageIndex = 0,
                x = centerX,
                y = centerY,
                width = 612f,
                height = 792f,
                assetPath = path
            )
            val currentPdfs = _document.value.pdfPages
            executeCommand(object : CanvasCommand {
                override val description = "Add PDF page"
                override fun execute() {
                    _document.value = _document.value.copy(pdfPages = currentPdfs + pdfItem)
                }
                override fun undo() {
                    _document.value = _document.value.copy(pdfPages = currentPdfs)
                }
            })
        }
    }

    private fun executeCommand(command: CanvasCommand) {
        commandHistory.execute(command)
        updateHistoryState()
    }

    fun undo() {
        if (commandHistory.undo()) {
            updateHistoryState()
        }
    }

    fun redo() {
        if (commandHistory.redo()) {
            updateHistoryState()
        }
    }

    private fun updateHistoryState() {
        _canUndo.value = commandHistory.canUndo
        _canRedo.value = commandHistory.canRedo
    }

    fun addNewPage() {
        viewModelScope.launch {
            val pageCount = _pages.value.size
            val newPage = notebookRepository.createPage(notebookId, "Page ${pageCount + 1}")
            loadDocument(newPage.id)
        }
    }

    fun navigateToPreviousPage() {
        val currentList = _pages.value
        val currentIndex = currentList.indexOfFirst { it.id == _currentPageId.value }
        if (currentIndex > 0) {
            loadDocument(currentList[currentIndex - 1].id)
        }
    }

    fun navigateToNextPage() {
        val currentList = _pages.value
        val currentIndex = currentList.indexOfFirst { it.id == _currentPageId.value }
        if (currentIndex != -1 && currentIndex < currentList.size - 1) {
            loadDocument(currentList[currentIndex + 1].id)
        }
    }
}

class CanvasViewModelFactory(
    private val notebookId: String,
    private val initialPageId: String,
    private val notebookRepository: NotebookRepository,
    private val canvasRepository: CanvasRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CanvasViewModel(
            notebookId = notebookId,
            initialPageId = initialPageId,
            notebookRepository = notebookRepository,
            canvasRepository = canvasRepository,
            preferencesRepository = preferencesRepository
        ) as T
    }
}
