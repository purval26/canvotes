package com.canvotes.domain.repository

import android.net.Uri
import com.canvotes.core.model.CanvasDocument

interface CanvasRepository {
    suspend fun loadPageDocument(pageId: String): CanvasDocument
    suspend fun savePageDocument(document: CanvasDocument): Boolean
    suspend fun importImage(uri: Uri): String?
    suspend fun importPdf(uri: Uri): String?
}
