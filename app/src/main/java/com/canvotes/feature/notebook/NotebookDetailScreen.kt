package com.canvotes.feature.notebook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canvotes.design.components.CanvotesConfirmDialog
import com.canvotes.design.components.CanvotesInputDialog
import com.canvotes.domain.model.Page
import com.canvotes.feature.notebook.components.PageCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookDetailScreen(
    viewModel: NotebookDetailViewModel,
    onNavigateBack: () -> Unit,
    onOpenPage: (notebookId: String, pageId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notebook by viewModel.notebook.collectAsState()
    val pages by viewModel.pages.collectAsState()

    var showAddPageDialog by remember { mutableStateOf(false) }
    var pageToRename by remember { mutableStateOf<Page?>(null) }
    var pageToDelete by remember { mutableStateOf<Page?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = notebook?.title ?: "Notebook",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${pages.size} ${if (pages.size == 1) "page" else "pages"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.addPage { newPage ->
                        onOpenPage(viewModel.notebookId, newPage.id)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Page")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (pages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pages in this notebook. Add a page to start drawing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pages, key = { it.id }) { page ->
                        PageCard(
                            page = page,
                            onClick = { onOpenPage(viewModel.notebookId, page.id) },
                            onRename = { pageToRename = page },
                            onDuplicate = { viewModel.duplicatePage(page.id) },
                            onDelete = { pageToDelete = page }
                        )
                    }
                }
            }
        }

        // Rename Page Dialog
        pageToRename?.let { page ->
            CanvotesInputDialog(
                title = "Rename Page",
                initialValue = page.title,
                label = "Page Title",
                confirmText = "Save",
                onConfirm = { newTitle ->
                    viewModel.renamePage(page.id, newTitle)
                    pageToRename = null
                },
                onDismiss = { pageToRename = null }
            )
        }

        // Delete Page Dialog
        pageToDelete?.let { page ->
            CanvotesConfirmDialog(
                title = "Delete Page?",
                message = "Are you sure you want to delete '${page.title}'? All notes and strokes on this page will be permanently erased.",
                confirmText = "Delete",
                onConfirm = {
                    viewModel.deletePage(page.id)
                    pageToDelete = null
                },
                onDismiss = { pageToDelete = null }
            )
        }
    }
}
