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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** Choice of authentication mode: Sign-Up or Log-In */
enum class AuthMode {
    SIGN_UP,
    LOG_IN
}

/** Current authentication state of the user. */
sealed interface AuthState {
    /** Not signed in. */
    data object SignedOut : AuthState

    /** Authentication in progress. */
    data object SigningIn : AuthState

    /**
     * Signed in successfully.
     * @param isNewUser true when the account was not previously registered in Firestore.
     */
    data class SignedIn(
        val isNewUser: Boolean,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
    ) : AuthState
}

/**
 * Authentication ViewModel for PEERS:
 * Supports Google OAuth (via Credential Manager) and Manual (Email & Password) Auth.
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

    private val _accountExistsError = MutableStateFlow<String?>(null)
    val accountExistsError: StateFlow<String?> = _accountExistsError.asStateFlow()

    /** True when the user is already authenticated with Firebase (e.g. app restart). */
    fun isSessionActive(): Boolean = auth.currentUser != null

    fun clearAccountExistsError() {
        _accountExistsError.value = null
    }

    /**
     * Launches the native Google account chooser via Credential Manager and signs the
     * user into Firebase with the resulting Google ID token.
     *
     * In SIGN_UP mode: If the account already exists and profile is complete,
     * surfaces "this account already exists, please login to access" with no navigation.
     * In LOG_IN mode: Directly enters dashboard without toasts/popups.
     */
    fun signInWithGoogle(activity: Activity, mode: AuthMode = AuthMode.SIGN_UP) {
        if (_authState.value == AuthState.SigningIn) return
        viewModelScope.launch {
            _authState.value = AuthState.SigningIn
            _accountExistsError.value = null
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

                val userDoc = firestore.collection("users").document(user.uid)
                val snapshot = runCatching { userDoc.get().await() }.getOrNull()
                val isExistingUser = snapshot != null && snapshot.exists()
                val isProfileCompleted = isExistingUser && snapshot.getBoolean("profileCompleted") == true

                if (mode == AuthMode.SIGN_UP && isExistingUser && isProfileCompleted) {
                    // Returning user clicked "Sign Up" instead of "Log In"
                    // Surface required message and prevent navigation
                    auth.signOut()
                    _authState.value = AuthState.SignedOut
                    _accountExistsError.value = "this account already exists, please login to access"
                    return@launch
                }

                if (mode == AuthMode.LOG_IN && (!isExistingUser || !isProfileCompleted)) {
                    // Incomplete or new account logged in via Google: take them to onboarding
                    if (!isExistingUser) {
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
                    }
                    _authState.value = AuthState.SignedIn(
                        isNewUser = true,
                        displayName = user.displayName,
                        email = user.email,
                        photoUrl = user.photoUrl?.toString(),
                    )
                    return@launch
                }

                if (mode == AuthMode.SIGN_UP && !isExistingUser) {
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
                }

                _authState.value = AuthState.SignedIn(
                    isNewUser = if (mode == AuthMode.LOG_IN) false else !isProfileCompleted,
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
     * Manual Sign-Up with Full Name, Email, and Password.
     * Creates account in Firebase Auth backend and initializes complete Dashboard in Firestore.
     */
    fun signUpManually(fullName: String, email: String, password: String) {
        if (_authState.value == AuthState.SigningIn) return
        viewModelScope.launch {
            _authState.value = AuthState.SigningIn
            _accountExistsError.value = null
            _signInError.value = null
            try {
                val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                val user = requireNotNull(authResult.user)
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.trim())
                    .build()
                runCatching { user.updateProfile(profileUpdates).await() }

                // Register initial identity document with profileCompleted = false
                // so the new user immediately enters the 4-step Profile Onboarding flow.
                val initialUserData = mapOf(
                    "uid" to user.uid,
                    "name" to fullName.trim(),
                    "email" to email.trim(),
                    "photoUrl" to "",
                    "profileCompleted" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
                firestore.collection("users").document(user.uid).set(initialUserData).await()

                _authState.value = AuthState.SignedIn(
                    isNewUser = true, // Routes straight into Profile Onboarding after Sign-Up
                    displayName = fullName.trim(),
                    email = email.trim(),
                    photoUrl = null,
                )
            } catch (e: Exception) {
                Log.e(TAG, "Manual sign-up failed", e)
                val msg = e.message ?: ""
                _authState.value = AuthState.SignedOut
                if (msg.contains("email address is already in use", ignoreCase = true) ||
                    msg.contains("already exists", ignoreCase = true)
                ) {
                    _accountExistsError.value = "this account already exists, please login to access"
                } else if (msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true)) {
                    _signInError.value = "Email/Password sign-in is not enabled in Firebase Console. Please enable it under Authentication > Sign-in method in Firebase Console."
                } else if (msg.contains("weak-password", ignoreCase = true)) {
                    _signInError.value = "Password should be at least 6 characters."
                } else if (msg.contains("invalid-email", ignoreCase = true)) {
                    _signInError.value = "The email address is badly formatted."
                } else {
                    _signInError.value = e.localizedMessage ?: "Sign up failed. Please try again."
                }
            }
        }
    }

    /**
     * Manual Log-In with Email and Password.
     * Authenticates with Firebase Auth and loads Dashboard from Firestore.
     */
    fun logInManually(email: String, password: String) {
        if (_authState.value == AuthState.SigningIn) return
        viewModelScope.launch {
            _authState.value = AuthState.SigningIn
            _accountExistsError.value = null
            _signInError.value = null
            try {
                val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
                val user = requireNotNull(authResult.user)
                val userDoc = firestore.collection("users").document(user.uid)
                val snapshot = runCatching { userDoc.get().await() }.getOrNull()
                if (snapshot == null || !snapshot.exists()) {
                    // Create default dashboard profile if not exists
                    val initialUserData = mapOf(
                        "uid" to user.uid,
                        "name" to (user.displayName ?: "Peer"),
                        "email" to user.email.orEmpty(),
                        "university" to "Campus Student",
                        "course" to "Student",
                        "yearOfStudy" to "1st Year",
                        "currentCity" to "Local Campus",
                        "countryOfOrigin" to "",
                        "currentCountry" to "",
                        "degreeLevel" to "Undergraduate",
                        "expectedGraduationYear" to "2027",
                        "bio" to "Hello, I am a new member of the PEERS community!",
                        "interests" to listOf("Networking", "Study Groups", "Campus Events"),
                        "needs" to listOf("Meet Peers", "Study Resources"),
                        "photoUrl" to "",
                        "profileCompleted" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                    )
                    userDoc.set(initialUserData).await()
                }
                _authState.value = AuthState.SignedIn(
                    isNewUser = false,
                    displayName = user.displayName ?: snapshot?.getString("name"),
                    email = user.email,
                    photoUrl = user.photoUrl?.toString(),
                )
            } catch (e: Exception) {
                Log.e(TAG, "Manual login failed", e)
                val msg = e.message ?: ""
                _authState.value = AuthState.SignedOut
                if (msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true)) {
                    _signInError.value = "Email/Password sign-in is not enabled in Firebase Console. Please enable it under Authentication > Sign-in method in Firebase Console."
                } else if (msg.contains("user-not-found", ignoreCase = true) || msg.contains("no user record", ignoreCase = true)) {
                    _signInError.value = "No account found with this email. Please sign up first."
                } else if (msg.contains("wrong-password", ignoreCase = true) || msg.contains("invalid-credential", ignoreCase = true)) {
                    _signInError.value = "Incorrect password. Please try again."
                } else {
                    _signInError.value = e.localizedMessage ?: "Invalid email or password. Please try again."
                }
            }
        }
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
