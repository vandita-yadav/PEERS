package com.example.peers.features.auth

import android.app.Activity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import com.google.firebase.FirebaseException

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _verificationId = mutableStateOf("")

    private val _authError = mutableStateOf<String?>(null)
    val authError: State<String?> = _authError

    private val _isOtpSent = mutableStateOf(false)
    val isOtpSent: State<Boolean> = _isOtpSent

    private val _isAuthenticated = mutableStateOf(false)
    val isAuthenticated: State<Boolean> = _isAuthenticated

    fun sendOtp(phoneNumber: String, activity: Activity) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Must include country code, e.g., +91
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-retrieval of SMS completed
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _authError.value = e.localizedMessage
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    _verificationId.value = verificationId
                    _isOtpSent.value = true
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String) {
        try {
            val credential = PhoneAuthProvider.getCredential(_verificationId.value, code)
            signInWithCredential(credential)
        } catch (e: Exception) {
            _authError.value = "Invalid Code"
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isAuthenticated.value = true
                    _authError.value = null
                } else {
                    _authError.value = task.exception?.localizedMessage
                }
            }
    }
}