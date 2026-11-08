package com.canvotes.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canvotes.core.model.CanvasThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Section: Appearance
            Text("APPEARANCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = preferences.themeMode == CanvasThemeMode.SYSTEM,
                    onClick = { viewModel.setThemeMode(CanvasThemeMode.SYSTEM) },
                    label = { Text("System") },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = preferences.themeMode == CanvasThemeMode.LIGHT,
                    onClick = { viewModel.setThemeMode(CanvasThemeMode.LIGHT) },
                    label = { Text("Light") },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = preferences.themeMode == CanvasThemeMode.DARK,
                    onClick = { viewModel.setThemeMode(CanvasThemeMode.DARK) },
                    label = { Text("Dark") },
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(20.dp))

            // Section: Canvas & Stylus Input
            Text("STYLUS & INPUT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Default Stroke Width: ${preferences.defaultPenWidth.toInt()}px", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = preferences.defaultPenWidth,
                onValueChange = viewModel::setDefaultPenWidth,
                valueRange = 1f..20f,
                steps = 19
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Palm Rejection", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Ignore accidental palm and wrist touches while writing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Switch(
                    checked = preferences.palmRejectionEnabled,
                    onCheckedChange = viewModel::setPalmRejectionEnabled
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Draw with Finger", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Allows single finger to draw strokes when no stylus is connected.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Switch(
                    checked = preferences.fingerDrawingEnabled,
                    onCheckedChange = viewModel::setFingerDrawingEnabled
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(20.dp))

            // Section: About
            Text("ABOUT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Canvotes", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Version 1.0.0 (Release 1)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "An infinite space for finite distractions. Local-first, stylus-optimized native Android workspace.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
