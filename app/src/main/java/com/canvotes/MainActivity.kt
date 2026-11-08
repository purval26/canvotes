package com.canvotes

import android.os.Bundle
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
}
