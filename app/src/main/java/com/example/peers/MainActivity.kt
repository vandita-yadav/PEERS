package com.example.peers

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.peers.features.auth.AuthMode
import com.example.peers.features.auth.AuthState
import com.example.peers.features.auth.AuthViewModel
import com.example.peers.features.dashboard.DashboardScreen
import com.example.peers.features.onboarding.OnboardingScreens
import com.example.peers.features.profile.ProfileSetupScreen
import com.example.peers.ui.theme.PeersTheme
import com.example.peers.ui.theme.PeersThemeMode
import com.example.peers.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.argb(0xF5, 0xF3, 0xF4, 0xF6), AndroidColor.argb(0xFF, 0x1C, 0x1D, 0x24)),
        )
        setContent {
            var themeMode by remember {
                mutableStateOf(ThemePreferences.load(applicationContext))
            }
            val isDark = when (themeMode) {
                PeersThemeMode.NeonDark -> true
                PeersThemeMode.AestheticLight -> false
                PeersThemeMode.SystemDefault -> isSystemInDarkTheme()
            }
            LaunchedEffect(isDark) {
                if (isDark) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.dark(AndroidColor.argb(0xFF, 0x1C, 0x1D, 0x24)),
                    )
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.light(
                            AndroidColor.argb(0xFF, 0xF3, 0xF4, 0xF6),
                            AndroidColor.argb(0xFF, 0xE5, 0xE7, 0xEB)
                        ),
                    )
                }
            }
            val onSetTheme: (PeersThemeMode) -> Unit = { newMode ->
                themeMode = newMode
                ThemePreferences.save(applicationContext, newMode)
            }
            PeersTheme(mode = themeMode) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PeersAuthApp(
                        themeMode = themeMode,
                        onToggleTheme = {
                            val next = if (themeMode == PeersThemeMode.AestheticLight) PeersThemeMode.NeonDark else PeersThemeMode.AestheticLight
                            onSetTheme(next)
                        },
                        onSetTheme = onSetTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun PeersAuthApp(
    themeMode: PeersThemeMode,
    onToggleTheme: () -> Unit,
    onSetTheme: (PeersThemeMode) -> Unit,
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val signInError by authViewModel.signInError.collectAsState()
    val accountExistsError by authViewModel.accountExistsError.collectAsState()

    // Surface sign-in failures as a toast
    LaunchedEffect(signInError) {
        signInError?.let {
            Toast.makeText(navController.context, it, Toast.LENGTH_LONG).show()
            authViewModel.consumeError()
        }
    }

    // Navigate after a successful sign-in:
    //   new user      → Profile Onboarding
    //   returning     → Dashboard with NO toast or snackbar
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.SignedIn -> {
                if (state.isNewUser) {
                    navController.navigate("profile") { popUpTo(0) { inclusive = true } }
                } else {
                    navController.navigate("dashboard") { popUpTo(0) { inclusive = true } }
                }
            }
            else -> Unit
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500) // 1.5 seconds per user request
                val destination = if (authViewModel.isSessionActive()) {
                    if (authViewModel.isProfileCompleted()) "dashboard" else "profile"
                } else {
                    "welcome"
                }
                navController.navigate(destination) { popUpTo("splash") { inclusive = true } }
            }
            OnboardingScreens.Splash()
        }

        composable("welcome") {
            OnboardingScreens.Welcome(
                isSigningIn = authState == AuthState.SigningIn,
                accountExistsError = accountExistsError,
                onContinueWithGoogle = { mode ->
                    authViewModel.signInWithGoogle(navController.context as ComponentActivity, mode)
                },
                onNavigateToManualSignUp = {
                    navController.navigate("manual_signup")
                },
                onNavigateToManualLogIn = {
                    navController.navigate("manual_login")
                },
                onToggleTheme = onToggleTheme,
                isNeonDark = themeMode == PeersThemeMode.NeonDark,
                onDismissAccountExistsError = {
                    authViewModel.clearAccountExistsError()
                }
            )
        }

        composable("manual_signup") {
            OnboardingScreens.ManualSignUp(
                isSigningIn = authState == AuthState.SigningIn,
                accountExistsError = accountExistsError,
                signInError = signInError,
                onSignUp = { name, email, password ->
                    authViewModel.signUpManually(name, email, password)
                },
                onContinueWithGoogle = {
                    authViewModel.signInWithGoogle(navController.context as ComponentActivity, AuthMode.SIGN_UP)
                },
                onNavigateToLogIn = {
                    navController.navigate("manual_login") {
                        popUpTo("manual_signup") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("manual_login") {
            OnboardingScreens.ManualLogIn(
                isSigningIn = authState == AuthState.SigningIn,
                signInError = signInError,
                onLogIn = { email, password ->
                    authViewModel.logInManually(email, password)
                },
                onContinueWithGoogle = {
                    authViewModel.signInWithGoogle(navController.context as ComponentActivity, AuthMode.LOG_IN)
                },
                onNavigateToSignUp = {
                    navController.navigate("manual_signup") {
                        popUpTo("manual_login") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
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
            DashboardScreen(
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onSetTheme = onSetTheme,
                onSignOut = {
                    authViewModel.signOut(navController.context as ComponentActivity)
                    navController.navigate("welcome") { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}
