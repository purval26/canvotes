package com.canvotes.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.canvotes.core.model.CanvasThemeMode
import com.canvotes.core.model.CanvasToolMode
import com.canvotes.data.preferences.UserPreferences
import com.canvotes.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setThemeMode(mode: CanvasThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDefaultTool(tool: CanvasToolMode) {
        viewModelScope.launch {
            preferencesRepository.setDefaultTool(tool)
        }
    }

    fun setDefaultPenWidth(width: Float) {
        viewModelScope.launch {
            preferencesRepository.setDefaultPenWidth(width)
        }
    }

    fun setPalmRejectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPalmRejectionEnabled(enabled)
        }
    }

    fun setFingerDrawingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setFingerDrawingEnabled(enabled)
        }
    }
}

class SettingsViewModelFactory(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(preferencesRepository) as T
    }
}
