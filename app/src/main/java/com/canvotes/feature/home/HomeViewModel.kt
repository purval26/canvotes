package com.canvotes.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.canvotes.domain.model.Notebook
import com.canvotes.domain.repository.NotebookRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val notebookRepository: NotebookRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val notebooks: StateFlow<List<Notebook>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                notebookRepository.getNotebooks()
            } else {
                notebookRepository.searchNotebooks(query.trim())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun createNotebook(title: String, description: String = "", onCreated: (Notebook) -> Unit) {
        viewModelScope.launch {
            val notebook = notebookRepository.createNotebook(title, description)
            onCreated(notebook)
        }
    }

    fun updateNotebook(id: String, title: String, description: String = "") {
        viewModelScope.launch {
            notebookRepository.updateNotebook(id, title, description)
        }
    }

    fun deleteNotebook(id: String) {
        viewModelScope.launch {
            notebookRepository.deleteNotebook(id)
        }
    }
}

class HomeViewModelFactory(
    private val notebookRepository: NotebookRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(notebookRepository) as T
    }
}
