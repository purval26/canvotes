package com.canvotes

import android.app.Application
import com.canvotes.data.local.CanvotesDatabase
import com.canvotes.data.local.storage.DocumentStorageManager
import com.canvotes.data.local.storage.FileAssetManager
import com.canvotes.data.preferences.UserPreferencesRepository
import com.canvotes.data.repository.CanvasRepositoryImpl
import com.canvotes.data.repository.NotebookRepositoryImpl
import com.canvotes.domain.repository.CanvasRepository
import com.canvotes.domain.repository.NotebookRepository

class CanvotesApplication : Application() {

    lateinit var database: CanvotesDatabase
        private set

    lateinit var documentStorageManager: DocumentStorageManager
        private set

    lateinit var fileAssetManager: FileAssetManager
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var notebookRepository: NotebookRepository
        private set

    lateinit var canvasRepository: CanvasRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = CanvotesDatabase.getInstance(this)
        documentStorageManager = DocumentStorageManager(this)
        fileAssetManager = FileAssetManager(this)
        userPreferencesRepository = UserPreferencesRepository(this)

        notebookRepository = NotebookRepositoryImpl(
            notebookDao = database.notebookDao(),
            pageDao = database.pageDao(),
            storageManager = documentStorageManager
        )

        canvasRepository = CanvasRepositoryImpl(
            storageManager = documentStorageManager,
            assetManager = fileAssetManager
        )
    }
}
