package com.canvotes

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.canvotes.design.theme.CanvotesTheme
import com.canvotes.navigation.CanvotesNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableHighRefreshRate()

        val app = application as CanvotesApplication

        setContent {
            val preferences by app.userPreferencesRepository.userPreferencesFlow.collectAsState(
                initial = com.canvotes.data.preferences.UserPreferences()
            )

            CanvotesTheme(themeMode = preferences.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CanvotesNavHost(app = app)
                }
            }
        }
    }

    private fun enableHighRefreshRate() {
        try {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && display != null) {
                val modes = display.supportedModes
                val highestMode = modes.maxByOrNull { it.refreshRate }
                if (highestMode != null && highestMode.refreshRate >= 90f) {
                    val params = window.attributes
                    params.preferredDisplayModeId = highestMode.modeId
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        params.preferredMinDisplayRefreshRate = highestMode.refreshRate
                        params.preferredMaxDisplayRefreshRate = highestMode.refreshRate
                    }
                    window.attributes = params
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
