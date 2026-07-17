package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.TvViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force Landscape Mode on TV screen launch
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TvAppNavigation()
                }
            }
        }
    }
}

@Composable
fun TvAppNavigation() {
    val navController = rememberNavController()
    val viewModel: TvViewModel = viewModel()
    val channels by viewModel.channels.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                viewModel = viewModel,
                onNavigateNext = { bootInPlayer ->
                    if (bootInPlayer) {
                        // Ensure a default channel is selected when booting into player directly
                        if (viewModel.currentPlayingChannel.value == null && channels.isNotEmpty()) {
                            viewModel.selectChannel(channels.first())
                        }
                        navController.navigate("player") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToPlayer = {
                    navController.navigate("player")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("player") {
            // Select default playing channel if none selected (fallback)
            LaunchedEffect(channels) {
                if (viewModel.currentPlayingChannel.value == null && channels.isNotEmpty()) {
                    viewModel.selectChannel(channels.first())
                }
            }

            PlayerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigate("home") {
                        popUpTo("player") { inclusive = true }
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
