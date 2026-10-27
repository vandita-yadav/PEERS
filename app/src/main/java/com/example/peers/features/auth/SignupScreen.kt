package com.example.peers.features.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.auth.components.AuthButton
import com.example.peers.features.auth.components.AuthTextField

@Composable
//@Preview(showBackground = true)
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToOtp: (String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var collegeId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Create Your Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(value = fullName, onValueChange = { fullName = it }, placeholder = "Full Name")
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            placeholder = "Phone Number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(value = college, onValueChange = { college = it }, placeholder = "College or University")
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(value = collegeId, onValueChange = { collegeId = it }, placeholder = "College Unique ID")
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Create a password",
            isPassword = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Min 8 characters, 1 special character, 1 number, 1 uppercase",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        AuthButton(text = "Sign Up", onClick = { onNavigateToOtp(phoneNumber) })

        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Already have an account? ", color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = "Log In",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF8F4)
@Composable
fun SignupPreview() {
    MaterialTheme {
        SignupScreen(
            onNavigateToLogin = {},
            onNavigateToOtp = {}
        )
    }
}