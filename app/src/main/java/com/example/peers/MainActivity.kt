package com.example.peers

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.peers.features.auth.AuthState
import com.example.peers.features.auth.AuthViewModel
import com.example.peers.features.onboarding.OnboardingScreens
import com.example.peers.features.dashboard.DashboardPlaceholder
import com.example.peers.features.profile.ProfileSetupScreen
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
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // Navigate after a successful sign-in:
    //   new user      → Profile Onboarding
    //   returning     → Dashboard
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.SignedIn -> {
                if (state.isNewUser) {
                    navController.navigate("profile") { popUpTo("welcome") { inclusive = true } }
                } else {
                    Toast.makeText(
                        navController.context,
                        "Welcome back! You already have a PEERS account.",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigate("dashboard") { popUpTo("welcome") { inclusive = true } }
                }
            }
            else -> Unit
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1100)
                // Returning users with an active Firebase session skip auth entirely.
                if (authViewModel.isSessionActive()) {
                    navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
                } else {
                    navController.navigate("welcome") { popUpTo("splash") { inclusive = true } }
                }
            }
            OnboardingScreens.Splash()
        }
        composable("welcome") {
            OnboardingScreens.Welcome(
                isSigningIn = authState == AuthState.SigningIn,
                onContinueWithGoogle = {
                    authViewModel.signInWithGoogle(navController.context as ComponentActivity)
                },
                onToggleTheme = onToggleTheme,
                isNeonDark = themeMode == PeersThemeMode.NeonDark
            )
        }
        composable("profile") {
            ProfileSetupScreen(
                onProfileComplete = {
                    navController.navigate("dashboard") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardPlaceholder(onBack = { navController.popBackStack() })
        }
    }
}
