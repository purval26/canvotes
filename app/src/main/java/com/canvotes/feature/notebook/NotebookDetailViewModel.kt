package com.canvotes.feature.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.canvotes.domain.model.Notebook
import com.canvotes.domain.model.Page
import com.canvotes.domain.repository.NotebookRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotebookDetailViewModel(
    val notebookId: String,
    private val notebookRepository: NotebookRepository
) : ViewModel() {

    private val _notebook = MutableStateFlow<Notebook?>(null)
    val notebook: StateFlow<Notebook?> = _notebook.asStateFlow()

    val pages: StateFlow<List<Page>> = notebookRepository.getPages(notebookId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadNotebook()
    }

    private fun loadNotebook() {
        viewModelScope.launch {
            _notebook.value = notebookRepository.getNotebook(notebookId)
        }
    }

    fun addPage(title: String? = null, onCreated: (Page) -> Unit) {
        viewModelScope.launch {
            val count = pages.value.size
            val pageTitle = title?.takeIf { it.isNotBlank() } ?: "Page ${count + 1}"
            val newPage = notebookRepository.createPage(notebookId, pageTitle)
            loadNotebook()
            onCreated(newPage)
        }
    }

    fun renamePage(pageId: String, newTitle: String) {
        viewModelScope.launch {
            notebookRepository.updatePageTitle(pageId, newTitle)
        }
    }

    fun duplicatePage(pageId: String) {
        viewModelScope.launch {
            notebookRepository.duplicatePage(pageId)
            loadNotebook()
        }
    }

    fun deletePage(pageId: String) {
        viewModelScope.launch {
            notebookRepository.deletePage(pageId)
            loadNotebook()
        }
    }
}

class NotebookDetailViewModelFactory(
    private val notebookId: String,
    private val notebookRepository: NotebookRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotebookDetailViewModel(notebookId, notebookRepository) as T
    }
}
