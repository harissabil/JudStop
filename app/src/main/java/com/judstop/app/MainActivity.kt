package com.judstop.app // Sesuaikan dengan package Anda

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.judstop.app.ui.screen.chat.ChatScreen
import com.judstop.app.ui.screen.main.MainScreen
import com.judstop.app.ui.theme.JudStopTheme

class MainActivity : ComponentActivity() {

    private var refreshKey by mutableIntStateOf(0)
    private var isInitialLaunchHandled by mutableStateOf(false) // State untuk menandai dialog awal

    companion object {
        const val PREFS_APP_LAUNCH = "JudStopAppLaunchPrefs"
        const val KEY_FIRST_LAUNCH_DIALOG_SHOWN = "firstLaunchDialogShown"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        // Cek apakah dialog awal perlu ditampilkan (hanya sekali seumur hidup aplikasi)
        // atau bisa juga per sesi jika isInitialLaunchHandled tidak disimpan permanen
        val appLaunchPrefs = getSharedPreferences(PREFS_APP_LAUNCH, MODE_PRIVATE)
        val shouldShowInitialDialogBasedOnPrefs =
            !appLaunchPrefs.getBoolean(KEY_FIRST_LAUNCH_DIALOG_SHOWN, false)
        if (savedInstanceState == null) { // Hanya set pada pembuatan activity baru, bukan saat rotasi
            isInitialLaunchHandled = !shouldShowInitialDialogBasedOnPrefs
        }


        setContent {
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted -> /* ... */ }

            LaunchedEffect(key1 = Unit) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            JudStopTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "main_screen") {
                    composable("main_screen") {
                        MainScreen(
                            onEnableServiceClick = {
                                requestAccessibilityPermission()
                            },
                            onChatbotClick = { navController.navigate("chat_screen") },
                            onWatchTutorialClick = {
                                val tutorialUrl =
                                    "https://www.youtube.com/watch/vkDV5WQICr0?si=EQRBaVLeLHEUpeZy"
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, tutorialUrl.toUri())
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Tidak bisa membuka link tutorial.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e(
                                        "MainActivity",
                                        "Error opening tutorial link: ${e.message}"
                                    )
                                }
                            },
                            refreshKey = refreshKey,
                            initialLaunch = !isInitialLaunchHandled,
                            onInitialLaunchHandled = {
                                isInitialLaunchHandled = true
                                appLaunchPrefs.edit {
                                    putBoolean(
                                        KEY_FIRST_LAUNCH_DIALOG_SHOWN,
                                        true
                                    )
                                }
                            }
                        )
                    }

                    composable("chat_screen") {
                        ChatScreen(onNavigateBack = { navController.navigateUp() })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshKey++
        Log.d("MainActivity", "onResume - refreshKey updated to: $refreshKey")
    }

    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}