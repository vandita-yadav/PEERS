package com.example.peers.features.auth

import android.app.Activity
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peers.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Current authentication state of the user. */
sealed interface AuthState {
    /** Not signed in. */
    data object SignedOut : AuthState

    /** Google sign-in in progress. */
    data object SigningIn : AuthState

    /**
     * Signed in successfully.
     * @param isNewUser true when the Google account was not previously registered in Firestore.
     */
    data class SignedIn(
        val isNewUser: Boolean,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
    ) : AuthState
}

/**
 * Single authentication method for PEERS: Google OAuth via Credential Manager.
 *
 * Flow (per Project Documentation):
 *  - user taps "Continue with Google" → [signInWithGoogle]
 *  - new account  → registered in Firestore → route to Profile Onboarding
 *  - returning    → route directly to Dashboard ("Welcome back!" snackbar)
 *  - cancelled    → silent return, no error shown
 *  - failure      → error surfaced via [signInError] (snackbar)
 */
class AuthViewModel : ViewModel() {

    private companion object {
        const val TAG = "PeersAuth"
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError.asStateFlow()

    /** True when the user is already authenticated with Firebase (e.g. app restart). */
    fun isSessionActive(): Boolean = auth.currentUser != null

    /**
     * Launches the native Google account chooser via Credential Manager and signs the
     * user into Firebase with the resulting Google ID token.
     */
    fun signInWithGoogle(activity: Activity) {
        if (_authState.value == AuthState.SigningIn) return
        viewModelScope.launch {
            _authState.value = AuthState.SigningIn
            try {
                val credentialManager = CredentialManager.create(activity)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                val idTokenCredential = if (credential is GoogleIdTokenCredential) {
                    credential
                } else if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    // Credential Manager wraps the token in a CustomCredential — unwrap it.
                    GoogleIdTokenCredential.createFrom(credential.data)
                } else {
                    throw IllegalArgumentException("Unexpected credential type: ${credential.type}")
                }

                val firebaseCredential =
                    GoogleAuthProvider.getCredential(idTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = requireNotNull(authResult.user) { "Firebase returned no user" }

                val isNewUser = registerIfNewUser(user)
                _authState.value = AuthState.SignedIn(
                    isNewUser = isNewUser,
                    displayName = user.displayName,
                    email = user.email,
                    photoUrl = user.photoUrl?.toString(),
                )
            } catch (e: GetCredentialCancellationException) {
                // User dismissed the account chooser — silent return per documentation.
                Log.i(TAG, "Sign-in cancelled by user")
                _authState.value = AuthState.SignedOut
            } catch (e: Exception) {
                Log.e(TAG, "Google sign-in failed", e)
                _authState.value = AuthState.SignedOut
                _signInError.value = e.message ?: "Sign-in failed. Please check your internet connection and try again."
            }
        }
    }

    /**
     * Returns true if onboarding is needed for this Google account: either the account
     * has never been used with PEERS (registers it in Firestore in that case) or its
     * profile is still incomplete. Returning users with complete profiles are untouched.
     */
    private suspend fun registerIfNewUser(user: FirebaseUser): Boolean {
        val userDoc = firestore.collection("users").document(user.uid)
        val snapshot = userDoc.get().await()
        if (snapshot.exists()) return snapshot.getBoolean("profileCompleted") != true

        userDoc.set(
            mapOf(
                "uid" to user.uid,
                "name" to user.displayName,
                "email" to user.email,
                "photoUrl" to user.photoUrl?.toString(),
                "profileCompleted" to false,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
        return true
    }

    /**
     * Returns true if the signed-in user's profile is complete in Firestore.
     * Used by the splash route to decide between Dashboard and Onboarding.
     */
    suspend fun isProfileCompleted(): Boolean {
        val user = auth.currentUser ?: return false
        return runCatching {
            firestore.collection("users").document(user.uid)
                .get().await()
                .getBoolean("profileCompleted") == true
        }.getOrDefault(false)
    }

    /** Marks the Firestore profile as completed (called after Profile Onboarding). */
    fun markProfileCompleted(uid: String) {
        firestore.collection("users").document(uid)
            .update("profileCompleted", true)
    }

    fun consumeError() {
        _signInError.value = null
    }

    /** Signs out of Firebase and clears the Credential Manager state. */
    fun signOut(activity: Activity) {
        viewModelScope.launch {
            runCatching {
                CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
            }
            auth.signOut()
            _authState.value = AuthState.SignedOut
        }
    }
}
