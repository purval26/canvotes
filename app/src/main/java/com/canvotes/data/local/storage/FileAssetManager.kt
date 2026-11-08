package com.canvotes.data.local.storage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileAssetManager(private val context: Context) {

    private val assetsDir: File
        get() = File(context.filesDir, "assets").apply {
            if (!exists()) mkdirs()
        }

    suspend fun importAsset(uri: Uri, extension: String = "bin"): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "${UUID.randomUUID()}.$extension"
            val destFile = File(assetsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteAsset(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            false
        }
    }
}
