package com.remixforge.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.remixforge.pro.data.repository.PresetRepository
import com.remixforge.pro.ui.screen.EditorScreen
import com.remixforge.pro.ui.screen.HomeScreen
import com.remixforge.pro.ui.theme.RemixForgeProTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemixForgeProTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { AppNav() }
            }
        }
    }
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "home") {
        composable("home") {
            HomeScreen(onPresetSelected = { uri, preset ->
                val enc = URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.name())
                nav.navigate("editor/$enc/${preset.id}")
            })
        }
        composable("editor/{uri}/{presetId}") { entry ->
            val enc = entry.arguments?.getString("uri") ?: return@composable
            val pid = entry.arguments?.getString("presetId") ?: return@composable
            val uri = android.net.Uri.parse(URLDecoder.decode(enc, StandardCharsets.UTF_8.name()))
            val preset = PresetRepository.getById(pid) ?: return@composable
            EditorScreen(localUri = uri, preset = preset, onBack = { nav.popBackStack() }, onExported = {})
        }
    }
}
