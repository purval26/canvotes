package com.canvotes.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvotes.design.components.CanvotesConfirmDialog
import com.canvotes.design.components.CanvotesInputDialog
import com.canvotes.domain.model.Notebook
import com.canvotes.feature.home.components.NotebookCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenNotebook: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notebooks by viewModel.notebooks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var notebookToRename by remember { mutableStateOf<Notebook?>(null) }
    var notebookToDelete by remember { mutableStateOf<Notebook?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CANVOTES",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "An infinite space for finite distractions.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
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
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Notebook")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Minimal Monochromatic Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Search notebooks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            if (notebooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No notebooks yet" else "No matching notebooks found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Create your first notebook to begin thinking freely." else "Try searching with different terms.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(notebooks, key = { it.id }) { notebook ->
                        NotebookCard(
                            notebook = notebook,
                            onClick = { onOpenNotebook(notebook.id) },
                            onRename = { notebookToRename = notebook },
                            onDelete = { notebookToDelete = notebook }
                        )
                    }
                }
            }
        }

        // Create Notebook Dialog
        if (showCreateDialog) {
            CanvotesInputDialog(
                title = "New Notebook",
                label = "Notebook Title",
                placeholder = "e.g. Sketches, Math, Ideas...",
                confirmText = "Create",
                onConfirm = { title ->
                    viewModel.createNotebook(title) { created ->
                        showCreateDialog = false
                        onOpenNotebook(created.id)
                    }
                },
                onDismiss = { showCreateDialog = false }
            )
        }

        // Rename Notebook Dialog
        notebookToRename?.let { nb ->
            CanvotesInputDialog(
                title = "Rename Notebook",
                initialValue = nb.title,
                label = "Title",
                confirmText = "Save",
                onConfirm = { newTitle ->
                    viewModel.updateNotebook(nb.id, newTitle, nb.description)
                    notebookToRename = null
                },
                onDismiss = { notebookToRename = null }
            )
        }

        // Delete Notebook Dialog
        notebookToDelete?.let { nb ->
            CanvotesConfirmDialog(
                title = "Delete Notebook?",
                message = "Are you sure you want to delete '${nb.title}' and all its pages? This action cannot be undone.",
                confirmText = "Delete",
                onConfirm = {
                    viewModel.deleteNotebook(nb.id)
                    notebookToDelete = null
                },
                onDismiss = { notebookToDelete = null }
            )
        }
    }
}
