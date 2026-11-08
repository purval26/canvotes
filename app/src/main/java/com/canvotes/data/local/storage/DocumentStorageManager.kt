package com.canvotes.data.local.storage

import android.content.Context
import com.canvotes.core.model.CanvasDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class DocumentStorageManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val pagesDir: File
        get() = File(context.filesDir, "pages").apply {
            if (!exists()) mkdirs()
        }

    suspend fun saveDocument(document: CanvasDocument): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(pagesDir, "${document.pageId}.json")
            val tempFile = File(pagesDir, "${document.pageId}.tmp")

            val jsonString = json.encodeToString(document)
            tempFile.writeText(jsonString)

            if (tempFile.renameTo(file)) {
                true
            } else {
                tempFile.copyTo(file, overwrite = true)
                tempFile.delete()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun loadDocument(pageId: String): CanvasDocument = withContext(Dispatchers.IO) {
        try {
            val file = File(pagesDir, "$pageId.json")
            if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString<CanvasDocument>(jsonString)
            } else {
                CanvasDocument(pageId = pageId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CanvasDocument(pageId = pageId)
        }
    }

    suspend fun deleteDocument(pageId: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(pagesDir, "$pageId.json")
        if (file.exists()) file.delete() else true
    }
}
