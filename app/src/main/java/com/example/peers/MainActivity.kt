package com.example.peers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.peers.features.onboarding.OnboardingScreens
import com.example.peers.features.profile.DashboardPlaceholder
import com.example.peers.features.profile.ProfileFormScreen
import com.example.peers.ui.theme.PeersTheme
import com.example.peers.ui.theme.PeersThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(PeersThemeMode.AestheticLight) }
            PeersTheme(mode = themeMode) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PeersAuthApp(
                        themeMode = themeMode,
                        onToggleTheme = {
                            themeMode = if (themeMode == PeersThemeMode.AestheticLight) {
                                PeersThemeMode.NeonDark
                            } else {
                                PeersThemeMode.AestheticLight
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeersAuthApp(themeMode: PeersThemeMode, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1100)
                navController.navigate("welcome") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            OnboardingScreens.Splash()
        }
        composable("welcome") {
            OnboardingScreens.Welcome(
                onSignUp = { navController.navigate("profile") },
                onSignIn = { navController.navigate("dashboard") },
                onToggleTheme = onToggleTheme,
                isNeonDark = themeMode == PeersThemeMode.NeonDark
            )
        }
        composable("profile") {
            ProfileFormScreen(onBack = { navController.popBackStack() })
        }
        composable("dashboard") {
            DashboardPlaceholder(onBack = { navController.popBackStack() })
        }
    }
}
