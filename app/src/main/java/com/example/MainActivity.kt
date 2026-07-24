package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GalleryPresetsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ModelSettingsScreen
import com.example.ui.screens.MotionPlayerScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.MotionAiTheme
import com.example.ui.viewmodel.MotionViewModel

enum class AppTab(val title: String, val icon: ImageVector) {
    STUDIO("Studio", Icons.Default.AutoAwesome),
    PRESETS("Presets", Icons.Default.Category),
    HISTORY("History", Icons.Default.VideoLibrary),
    SETTINGS("AI Models", Icons.Default.Psychology)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotionAiTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MotionViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(AppTab.STUDIO) }
    var isPlayerMode by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        bottomBar = {
            if (!isPlayerMode) {
                NavigationBar(
                    containerColor = Color(0xFF1E293B),
                    tonalElevation = 8.dp
                ) {
                    AppTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color(0xFF00E5FF),
                                indicatorColor = Color(0xFF00E5FF),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        if (isPlayerMode) {
            MotionPlayerScreen(
                viewModel = viewModel,
                onBack = { isPlayerMode = false },
                modifier = modifier
            )
        } else {
            when (currentTab) {
                AppTab.STUDIO -> {
                    StudioScreen(
                        viewModel = viewModel,
                        onNavigateToPlayer = { isPlayerMode = true },
                        modifier = modifier
                    )
                }
                AppTab.PRESETS -> {
                    GalleryPresetsScreen(
                        viewModel = viewModel,
                        onSelectSample = { sample ->
                            viewModel.loadSamplePreset(sample)
                            currentTab = AppTab.STUDIO
                        },
                        modifier = modifier
                    )
                }
                AppTab.HISTORY -> {
                    HistoryScreen(
                        viewModel = viewModel,
                        onOpenProject = { project ->
                            viewModel.loadProject(project)
                            isPlayerMode = true
                        },
                        modifier = modifier
                    )
                }
                AppTab.SETTINGS -> {
                    ModelSettingsScreen(
                        viewModel = viewModel,
                        modifier = modifier
                    )
                }
            }
        }
    }
}
