package com.canvotes.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.canvotes.core.model.CanvasThemeMode
import com.canvotes.core.model.CanvasToolMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "canvotes_settings")

data class UserPreferences(
    val themeMode: CanvasThemeMode = CanvasThemeMode.SYSTEM,
    val defaultTool: CanvasToolMode = CanvasToolMode.PEN,
    val defaultPenWidth: Float = 3.0f,
    val palmRejectionEnabled: Boolean = true,
    val fingerDrawingEnabled: Boolean = false,
    val lastOpenedNotebookId: String? = null,
    val lastOpenedPageId: String? = null
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_TOOL = stringPreferencesKey("default_tool")
        val DEFAULT_PEN_WIDTH = floatPreferencesKey("default_pen_width")
        val PALM_REJECTION = booleanPreferencesKey("palm_rejection")
        val FINGER_DRAWING = booleanPreferencesKey("finger_drawing")
        val LAST_NOTEBOOK_ID = stringPreferencesKey("last_notebook_id")
        val LAST_PAGE_ID = stringPreferencesKey("last_page_id")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: CanvasThemeMode.SYSTEM.name
            val toolStr = preferences[PreferencesKeys.DEFAULT_TOOL] ?: CanvasToolMode.PEN.name
            UserPreferences(
                themeMode = runCatching { CanvasThemeMode.valueOf(themeStr) }.getOrDefault(CanvasThemeMode.SYSTEM),
                defaultTool = runCatching { CanvasToolMode.valueOf(toolStr) }.getOrDefault(CanvasToolMode.PEN),
                defaultPenWidth = preferences[PreferencesKeys.DEFAULT_PEN_WIDTH] ?: 3.0f,
                palmRejectionEnabled = preferences[PreferencesKeys.PALM_REJECTION] ?: true,
                fingerDrawingEnabled = preferences[PreferencesKeys.FINGER_DRAWING] ?: false,
                lastOpenedNotebookId = preferences[PreferencesKeys.LAST_NOTEBOOK_ID],
                lastOpenedPageId = preferences[PreferencesKeys.LAST_PAGE_ID]
            )
        }

    suspend fun setThemeMode(themeMode: CanvasThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setDefaultTool(tool: CanvasToolMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_TOOL] = tool.name
        }
    }

    suspend fun setDefaultPenWidth(width: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PEN_WIDTH] = width
        }
    }

    suspend fun setPalmRejectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PALM_REJECTION] = enabled
        }
    }

    suspend fun setFingerDrawingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FINGER_DRAWING] = enabled
        }
    }

    suspend fun setLastSession(notebookId: String, pageId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_NOTEBOOK_ID] = notebookId
            preferences[PreferencesKeys.LAST_PAGE_ID] = pageId
        }
    }
}
