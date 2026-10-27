package com.example.peers.features.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.auth.components.AuthButton
import com.example.peers.features.auth.components.AuthTextField
import com.example.peers.ui.theme.PeersTheme
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    onVerify: (String) -> Unit, // Updated to require a String
    onSendViaEmail: () -> Unit,
    onResendOtp: () -> Unit = {}
) {
    var otpCode by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(60) }

    // Coroutine to handle the countdown timer
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Verify your number",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Code sent to $phoneNumber",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) otpCode = it },
            placeholder = "Enter 6-digit OTP"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic timer logic
        if (timeLeft > 0) {
            Text(
                text = "Resend OTP in 00:${timeLeft.toString().padStart(2, '0')}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        } else {
            Text(
                text = "Resend OTP",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    timeLeft = 60 // Reset timer
                    onResendOtp() // Trigger resend logic
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        AuthButton(text = "Verify", onClick = { onVerify(otpCode) })

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSendViaEmail,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Send OTP via email instead", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF8F4)
@Composable
fun OtpVerificationScreenPreview() {
    PeersTheme {
        OtpVerificationScreen(
            phoneNumber = "+91 98765 43210",
            onVerify = {},
            onSendViaEmail = {}
        )
    }
}