package com.canvotes.data.repository

import android.net.Uri
import com.canvotes.core.model.CanvasDocument
import com.canvotes.data.local.storage.DocumentStorageManager
import com.canvotes.data.local.storage.FileAssetManager
import com.canvotes.domain.repository.CanvasRepository

class CanvasRepositoryImpl(
    private val storageManager: DocumentStorageManager,
    private val assetManager: FileAssetManager
) : CanvasRepository {

    override suspend fun loadPageDocument(pageId: String): CanvasDocument {
        return storageManager.loadDocument(pageId)
    }

    override suspend fun savePageDocument(document: CanvasDocument): Boolean {
        return storageManager.saveDocument(document)
    }

    override suspend fun importImage(uri: Uri): String? {
        return assetManager.importAsset(uri, "jpg")
    }

    override suspend fun importPdf(uri: Uri): String? {
        return assetManager.importAsset(uri, "pdf")
    }
}
