package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthlySummaryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LedgerViewModel

sealed class Screen {
    object Home : Screen()
    object Summary : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: LedgerViewModel = viewModel()
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    when (currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToSummary = { currentScreen = Screen.Summary },
                                onNavigateToSettings = { currentScreen = Screen.Settings }
                            )
                        }
                        is Screen.Summary -> {
                            BackHandler { currentScreen = Screen.Home }
                            MonthlySummaryScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.Home }
                            )
                        }
                        is Screen.Settings -> {
                            BackHandler { currentScreen = Screen.Home }
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = Screen.Home }
                            )
                        }
                    }
                }
            }
        }
    }
}
