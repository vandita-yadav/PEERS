package com.example.peers.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface ProfileCompletion {
    data object Idle : ProfileCompletion
    data object Saving : ProfileCompletion
    data class Success(val userId: String) : ProfileCompletion
    data class Error(val message: String) : ProfileCompletion
}

class ProfileSetupViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(ProfileSetupState())
    val state: StateFlow<ProfileSetupState> = _state.asStateFlow()

    private val _completion = MutableStateFlow<ProfileCompletion>(ProfileCompletion.Idle)
    val completion: StateFlow<ProfileCompletion> = _completion.asStateFlow()

    init {
        // Prefill identity from the signed-in Firebase account (Google or Manual)
        auth.currentUser?.let { user ->
            _state.update {
                it.copy(
                    name = user.displayName ?: it.name,
                    email = user.email ?: it.email,
                    photoUrl = user.photoUrl?.toString() ?: it.photoUrl
                )
            }
            // Also hydrate any pre-saved Firestore name/fields if present
            viewModelScope.launch {
                val snap = runCatching {
                    firestore.collection("users").document(user.uid).get().await()
                }.getOrNull()
                if (snap != null && snap.exists()) {
                    _state.update { s ->
                        s.copy(
                            name = snap.getString("name")?.takeIf { it.isNotBlank() } ?: s.name,
                            email = snap.getString("email")?.takeIf { it.isNotBlank() } ?: s.email,
                        )
                    }
                }
            }
        }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // --- Navigation & Validation Logic ---

    fun nextStep() {
        val currentState = _state.value
        if (currentState.currentStep < 4 && canProceedToNextStep()) {
            _state.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        if (_state.value.currentStep > 1) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun canProceedToNextStep(): Boolean {
        val s = _state.value
        return when (s.currentStep) {
            1 -> {
                val isUniversityValid = if (s.university == "Other") {
                    s.customUniversity.isNotBlank()
                } else {
                    s.university.isNotBlank()
                }
                val isCourseValid = if (s.course == "Other") {
                    s.customCourse.isNotBlank()
                } else {
                    s.course.isNotBlank()
                }

                s.countryOfOrigin.isNotBlank() &&
                    s.currentCountry.isNotBlank() &&
                    s.currentCity.isNotBlank() &&
                    isUniversityValid &&
                    isCourseValid &&
                    s.degreeLevel.isNotBlank() &&
                    s.yearOfStudy.isNotBlank()
            }
            2 -> true // Photo or default avatar
            3 -> s.selectedInterests.size >= 3 // Min 3 required
            4 -> s.selectedNeeds.size >= 2     // Min 2 required
            else -> false
        }
    }

    // --- Step 1 Updates ---

    fun updateCountryOfOrigin(country: String) = _state.update { it.copy(countryOfOrigin = country) }
    fun updateCurrentCountry(country: String) = _state.update { it.copy(currentCountry = country) }
    fun updateCurrentCity(city: String) = _state.update { it.copy(currentCity = city) }

    fun updateUniversity(uni: String) = _state.update {
        it.copy(
            university = uni,
            customUniversity = if (uni != "Other") "" else it.customUniversity
        )
    }

    fun updateCustomUniversity(uni: String) = _state.update { it.copy(customUniversity = uni) }

    fun updateCourse(course: String) = _state.update {
        it.copy(
            course = course,
            customCourse = if (course != "Other") "" else it.customCourse
        )
    }

    fun updateCustomCourse(course: String) = _state.update { it.copy(customCourse = course) }
    fun updateDegreeLevel(level: String) = _state.update { it.copy(degreeLevel = level) }
    fun updateYearOfStudy(year: String) = _state.update { it.copy(yearOfStudy = year) }
    fun updateExpectedGraduationYear(year: String) = _state.update { it.copy(expectedGraduationYear = year) }
    fun updateBio(bio: String) = _state.update { it.copy(bio = bio) }

    fun toggleLanguage(language: String) = _state.update { s ->
        val updated = s.languagesKnown.toMutableList()
        if (updated.contains(language)) updated.remove(language) else updated.add(language)
        s.copy(languagesKnown = updated)
    }

    // --- Step 2 Updates ---

    fun updateProfileImage(localUri: String?, dataUri: String? = null) = _state.update {
        // Clear predefined avatar when user uploads/captures a personal photo
        it.copy(
            profileImageUri = localUri,
            croppedPhotoDataUri = dataUri,
            selectedAvatar = null
        )
    }

    fun updateSelectedAvatar(avatarPath: String?) = _state.update {
        // Clear personal photo when user switches to a predefined PEERS avatar
        it.copy(
            selectedAvatar = avatarPath,
            profileImageUri = null,
            croppedPhotoDataUri = null
        )
    }

    // --- Step 3 Updates ---

    fun toggleInterest(interest: String) {
        val currentInterests = _state.value.selectedInterests.toMutableSet()
        if (currentInterests.contains(interest)) {
            currentInterests.remove(interest)
        } else {
            currentInterests.add(interest)
        }
        _state.update { it.copy(selectedInterests = currentInterests) }
    }

    // --- Step 4 Updates ---

    fun toggleNeed(need: String) {
        val currentNeeds = _state.value.selectedNeeds.toMutableSet()
        if (currentNeeds.contains(need)) {
            currentNeeds.remove(need)
        } else {
            currentNeeds.add(need)
        }
        _state.update { it.copy(selectedNeeds = currentNeeds) }
    }

    // --- Final Submission ---

    fun completeProfile() {
        val uid = currentUserId
        if (uid == null) {
            _completion.value = ProfileCompletion.Error("Not signed in. Please sign in again.")
            return
        }
        if (!canProceedToNextStep()) return

        viewModelScope.launch {
            _completion.value = ProfileCompletion.Saving
            try {
                val s = _state.value
                val authUser = FirebaseAuth.getInstance().currentUser
                val finalUni = if (s.university == "Other") s.customUniversity.trim() else s.university.trim()
                val finalCourse = if (s.course == "Other") s.customCourse.trim() else s.course.trim()
                val finalPhotoUrl = when {
                    s.selectedAvatar != null -> ""
                    !s.croppedPhotoDataUri.isNullOrBlank() -> s.croppedPhotoDataUri
                    !s.profileImageUri.isNullOrBlank() -> s.profileImageUri
                    else -> s.photoUrl?.takeIf { it.isNotBlank() } ?: authUser?.photoUrl?.toString() ?: ""
                }
                val finalAvatar = if (!s.croppedPhotoDataUri.isNullOrBlank() || !s.profileImageUri.isNullOrBlank()) {
                    null
                } else {
                    s.selectedAvatar ?: "neutral/neutral1.png"
                }

                firestore.collection("users").document(uid).set(
                    mapOf(
                        "uid" to uid,
                        "name" to s.name.ifBlank { authUser?.displayName ?: "Peer" },
                        "email" to s.email.ifBlank { authUser?.email ?: "" },
                        "photoUrl" to finalPhotoUrl,
                        "countryOfOrigin" to s.countryOfOrigin.trim(),
                        "currentCountry" to s.currentCountry.trim(),
                        "currentCity" to s.currentCity.trim(),
                        "university" to finalUni,
                        "course" to finalCourse,
                        "degreeLevel" to s.degreeLevel,
                        "yearOfStudy" to s.yearOfStudy,
                        "expectedGraduationYear" to s.expectedGraduationYear.trim(),
                        "bio" to s.bio.trim(),
                        "languagesKnown" to s.languagesKnown,
                        "avatarIndex" to finalAvatar,
                        "interests" to s.selectedInterests.toList(),
                        "needs" to s.selectedNeeds.toList(),
                        "profileCompleted" to true
                    ),
                    SetOptions.merge()
                ).await()

                _state.update {
                    it.copy(isCompletedScreen = true, completedUserId = uid)
                }
                _completion.value = ProfileCompletion.Success(uid)
            } catch (e: Exception) {
                _completion.value = ProfileCompletion.Error(e.message ?: "Failed to save profile.")
            }
        }
    }

    fun consumeCompletion() {
        _completion.value = ProfileCompletion.Idle
    }
}
