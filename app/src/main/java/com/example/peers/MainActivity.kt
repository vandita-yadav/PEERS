package com.example.peers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.peers.features.auth.AuthViewModel
import com.example.peers.features.auth.LoginScreen
import com.example.peers.features.auth.OtpVerificationScreen
import com.example.peers.features.auth.SignupScreen
import com.example.peers.ui.theme.PeersTheme
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeersTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PeersAuthApp(this)
                }
            }
        }
    }
}

@Composable
fun PeersAuthApp(activity: ComponentActivity) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val isAuthenticated by authViewModel.isAuthenticated
    val authError by authViewModel.authError // Observe errors
    val context = LocalContext.current // Get context for Toasts

    // Listen for successful authentication and navigate away from the auth flow
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            // TODO: Navigate to Home/Dashboard Screen
            println("User successfully authenticated!")
        }
    }
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            Toast.makeText(context, "Authentication Successful!", Toast.LENGTH_LONG).show()
        }
    }

    // Show error popup
    LaunchedEffect(authError) {
        if (authError != null) {
            Toast.makeText(context, "Error: $authError", Toast.LENGTH_LONG).show()
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToSignup = { navController.navigate("signup") },
                onNavigateToOtp = { phoneNumber ->
                    val cleanNumber = phoneNumber.replace(" ", "") // Strip spaces
                    authViewModel.sendOtp(cleanNumber, activity)
                    navController.navigate("otp/$cleanNumber")
                },
                onForgotPassword = { /* TODO: Implement Forgot Password */ }
            )
        }
        composable("signup") {
            SignupScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToOtp = { phoneNumber ->
                    val cleanNumber = phoneNumber.replace(" ", "") // Strip spaces
                    authViewModel.sendOtp(cleanNumber, activity)
                    navController.navigate("otp/$cleanNumber")
                }
            )
        }
        composable(
            route = "otp/{phoneNumber}"
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""

            OtpVerificationScreen(
                phoneNumber = phoneNumber,
                onVerify = { code ->
                    authViewModel.verifyOtp(code)
                },
                onSendViaEmail = { /* TODO: Email OTP fallback */ },
                onResendOtp = {
                    authViewModel.sendOtp(phoneNumber, activity)
                }
            )
        }
    }
}