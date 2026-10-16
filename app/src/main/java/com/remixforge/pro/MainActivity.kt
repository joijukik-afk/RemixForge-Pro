package com.remixforge.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.remixforge.pro.data.model.RemixPreset
import com.remixforge.pro.ui.screen.EditorScreen
import com.remixforge.pro.ui.screen.HomeScreen
import com.remixforge.pro.ui.theme.RemixForgeProTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemixForgeProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RemixForgeApp()
                }
            }
        }
    }
}

@Composable
private fun RemixForgeApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onPresetSelected = { uri, preset ->
                    val encodedUri = URLEncoder.encode(
                        uri.toString(),
                        StandardCharsets.UTF_8.name()
                    )
                    navController.navigate("editor/$encodedUri/${preset.id}")
                }
            )
        }

        composable("editor/{uri}/{presetId}") { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("uri") ?: return@composable
            val presetId = backStackEntry.arguments?.getString("presetId") ?: return@composable

            val uri = android.net.Uri.parse(
                java.net.URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.name())
            )
            val preset = com.remixforge.pro.data.repository.PresetRepository
                .getById(presetId) ?: return@composable

            EditorScreen(
                uri = uri,
                preset = preset,
                onBack = { navController.popBackStack() },
                onExported = { /* می‌تونی به صفحه Export بری */ }
            )
        }
    }
}
