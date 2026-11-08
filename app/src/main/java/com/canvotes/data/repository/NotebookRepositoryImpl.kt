package com.canvotes.data.repository

import com.canvotes.data.local.dao.NotebookDao
import com.canvotes.data.local.dao.PageDao
import com.canvotes.data.local.entity.NotebookEntity
import com.canvotes.data.local.entity.PageEntity
import com.canvotes.data.local.storage.DocumentStorageManager
import com.canvotes.domain.model.Notebook
import com.canvotes.domain.model.Page
import com.canvotes.domain.repository.NotebookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NotebookRepositoryImpl(
    private val notebookDao: NotebookDao,
    private val pageDao: PageDao,
    private val storageManager: DocumentStorageManager
) : NotebookRepository {

    override fun getNotebooks(): Flow<List<Notebook>> {
        return notebookDao.getNotebooksWithPageCount().map { list ->
            list.map { item ->
                Notebook(
                    id = item.notebook.id,
                    title = item.notebook.title,
                    description = item.notebook.description,
                    pageCount = item.pageCount,
                    createdAt = item.notebook.createdAt,
                    updatedAt = item.notebook.updatedAt,
                    isPinned = item.notebook.isPinned
                )
            }
        }
    }

    override fun searchNotebooks(query: String): Flow<List<Notebook>> {
        return notebookDao.searchNotebooks(query).map { list ->
            list.map { item ->
                Notebook(
                    id = item.notebook.id,
                    title = item.notebook.title,
                    description = item.notebook.description,
                    pageCount = item.pageCount,
                    createdAt = item.notebook.createdAt,
                    updatedAt = item.notebook.updatedAt,
                    isPinned = item.notebook.isPinned
                )
            }
        }
    }

    override suspend fun getNotebook(id: String): Notebook? {
        val entity = notebookDao.getNotebookById(id) ?: return null
        val pageCount = pageDao.getPageCountForNotebook(id)
        return Notebook(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            pageCount = pageCount,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isPinned = entity.isPinned
        )
    }

    override suspend fun createNotebook(title: String, description: String): Notebook {
        val notebookId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val notebook = NotebookEntity(
            id = notebookId,
            title = title,
            description = description,
            createdAt = now,
            updatedAt = now
        )
        notebookDao.insertNotebook(notebook)

        // Automatically create initial Page 1 for a new notebook
        val initialPage = PageEntity(
            id = UUID.randomUUID().toString(),
            notebookId = notebookId,
            title = "Page 1",
            pageNumber = 1,
            createdAt = now,
            updatedAt = now
        )
        pageDao.insertPage(initialPage)

        return Notebook(
            id = notebookId,
            title = title,
            description = description,
            pageCount = 1,
            createdAt = now,
            updatedAt = now
        )
    }

    override suspend fun updateNotebook(id: String, title: String, description: String) {
        val existing = notebookDao.getNotebookById(id) ?: return
        notebookDao.updateNotebook(
            existing.copy(
                title = title,
                description = description,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteNotebook(id: String) {
        notebookDao.deleteNotebookById(id)
    }

    override fun getPages(notebookId: String): Flow<List<Page>> {
        return pageDao.getPagesForNotebook(notebookId).map { list ->
            list.map { p ->
                Page(
                    id = p.id,
                    notebookId = p.notebookId,
                    title = p.title,
                    pageNumber = p.pageNumber,
                    createdAt = p.createdAt,
                    updatedAt = p.updatedAt
                )
            }
        }
    }

    override suspend fun getPage(id: String): Page? {
        val entity = pageDao.getPageById(id) ?: return null
        return Page(
            id = entity.id,
            notebookId = entity.notebookId,
            title = entity.title,
            pageNumber = entity.pageNumber,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override suspend fun createPage(notebookId: String, title: String): Page {
        val count = pageDao.getPageCountForNotebook(notebookId)
        val now = System.currentTimeMillis()
        val page = PageEntity(
            id = UUID.randomUUID().toString(),
            notebookId = notebookId,
            title = title,
            pageNumber = count + 1,
            createdAt = now,
            updatedAt = now
        )
        pageDao.insertPage(page)
        return Page(
            id = page.id,
            notebookId = page.notebookId,
            title = page.title,
            pageNumber = page.pageNumber,
            createdAt = page.createdAt,
            updatedAt = page.updatedAt
        )
    }

    override suspend fun updatePageTitle(pageId: String, title: String) {
        val page = pageDao.getPageById(pageId) ?: return
        pageDao.updatePage(page.copy(title = title, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun duplicatePage(pageId: String): Page? {
        val original = pageDao.getPageById(pageId) ?: return null
        val count = pageDao.getPageCountForNotebook(original.notebookId)
        val now = System.currentTimeMillis()
        val newPageId = UUID.randomUUID().toString()

        val copy = PageEntity(
            id = newPageId,
            notebookId = original.notebookId,
            title = "${original.title} (Copy)",
            pageNumber = count + 1,
            createdAt = now,
            updatedAt = now
        )
        pageDao.insertPage(copy)

        // Duplicate canvas JSON document content
        val doc = storageManager.loadDocument(pageId)
        storageManager.saveDocument(
            doc.copy(
                id = UUID.randomUUID().toString(),
                pageId = newPageId,
                lastModified = now
            )
        )

        return Page(
            id = copy.id,
            notebookId = copy.notebookId,
            title = copy.title,
            pageNumber = copy.pageNumber,
            createdAt = copy.createdAt,
            updatedAt = copy.updatedAt
        )
    }

    override suspend fun deletePage(pageId: String) {
        pageDao.deletePageById(pageId)
        storageManager.deleteDocument(pageId)
    }

    override suspend fun reorderPages(pages: List<Page>) {
        val entities = pages.mapIndexed { index, p ->
            PageEntity(
                id = p.id,
                notebookId = p.notebookId,
                title = p.title,
                pageNumber = index + 1,
                createdAt = p.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        }
        pageDao.reorderPages(entities)
    }
}
