package com.example.shaalesmarttrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.shaalesmarttrack.data.local.AppDatabase
import com.example.shaalesmarttrack.ui.dashboard.DashboardScreen
import com.example.shaalesmarttrack.ui.assets.AssetsScreen
import com.example.shaalesmarttrack.ui.healthcheck.HealthCheckScreen
import com.example.shaalesmarttrack.ui.issues.IssuesScreen
import com.example.shaalesmarttrack.ui.repairs.RepairsScreen
import com.example.shaalesmarttrack.ui.reports.ReportsScreen
import com.example.shaalesmarttrack.ui.settings.SettingsScreen
import com.example.shaalesmarttrack.ui.theme.ShaaleSmartTrackTheme

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "shaale_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        setContent {
            ShaaleSmartTrackTheme {
                var currentScreen by remember { mutableStateOf("dashboard") }
                var previousScreen by remember { mutableStateOf("dashboard") }

                BackHandler(enabled = currentScreen != "dashboard") {
                    currentScreen = "dashboard"
                }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState != "dashboard") {
                            slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }) togetherWith
                                slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it / 3 })
                        } else {
                            slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it }) togetherWith
                                slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it / 3 })
                        }
                    },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        "dashboard" -> DashboardScreen(
                            db = db,
                            modifier = Modifier.fillMaxSize(),
                            onNavigate = { route -> currentScreen = route }
                        )
                        "assets" -> AssetsScreen(
                            db = db,
                            modifier = Modifier.fillMaxSize(),
                            onBack = { currentScreen = "dashboard" }
                        )
                        "health_check" -> HealthCheckScreen(
                            db = db,
                            modifier = Modifier.fillMaxSize(),
                            onBack = { currentScreen = "dashboard" }
                        )
                        "issues" -> IssuesScreen(
                            db = db,
                            modifier = Modifier.fillMaxSize(),
                            onBack = { currentScreen = "dashboard" }
                        )
                        "repairs" -> RepairsScreen(
                            db = db,
                            modifier = Modifier.fillMaxSize(),
                            onBack = { currentScreen = "dashboard" }
                        )
                        "reports" -> ReportsScreen(
                            db = db,
                            modifier = Modifier.fillMaxSize(),
                            onBack = { currentScreen = "dashboard" }
                        )
                        "settings" -> SettingsScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBack = { currentScreen = "dashboard" }
                        )
                    }
                }
            }
        }
    }
}
