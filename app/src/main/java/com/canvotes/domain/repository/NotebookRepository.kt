package com.canvotes.domain.repository

import com.canvotes.domain.model.Notebook
import com.canvotes.domain.model.Page
import kotlinx.coroutines.flow.Flow

interface NotebookRepository {
    fun getNotebooks(): Flow<List<Notebook>>
    fun searchNotebooks(query: String): Flow<List<Notebook>>
    suspend fun getNotebook(id: String): Notebook?
    suspend fun createNotebook(title: String, description: String = ""): Notebook
    suspend fun updateNotebook(id: String, title: String, description: String)
    suspend fun deleteNotebook(id: String)

    fun getPages(notebookId: String): Flow<List<Page>>
    suspend fun getPage(id: String): Page?
    suspend fun createPage(notebookId: String, title: String): Page
    suspend fun updatePageTitle(pageId: String, title: String)
    suspend fun duplicatePage(pageId: String): Page?
    suspend fun deletePage(pageId: String)
    suspend fun reorderPages(pages: List<Page>)
}
