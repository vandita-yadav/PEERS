package com.example.peers.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

class ProfileSetupViewModel : ViewModel()
{
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(ProfileSetupState())
    val state: StateFlow<ProfileSetupState> = _state.asStateFlow()

    private val _completion = MutableStateFlow<ProfileCompletion>(ProfileCompletion.Idle)
    val completion: StateFlow<ProfileCompletion> = _completion.asStateFlow()

    init {
        // Prefill identity from the signed-in Google account (per documentation).
        auth.currentUser?.let { user ->
            _state.update {
                it.copy(
                    name = user.displayName ?: it.name,
                    email = user.email ?: it.email,
                    photoUrl = user.photoUrl?.toString() ?: it.photoUrl
                )
            }
        }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // --- Navigation & Validation Logic ---

    fun nextStep()
    {
        val currentState = _state.value
        if (currentState.currentStep < 4 && canProceedToNextStep())
        {
            _state.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep()
    {
        if (_state.value.currentStep > 1)
        {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun canProceedToNextStep(): Boolean
    {
        val currentState = _state.value
        return when (currentState.currentStep)
        {
            1 -> {
                // Required fields actually shown in BasicInformationStep
                val isUniversityValid = if (currentState.university == "Other") {
                    currentState.customUniversity.isNotBlank()
                }
                else
                {
                    currentState.university.isNotBlank()
                }

                currentState.currentCountry.isNotBlank() &&
                        isUniversityValid &&
                        currentState.course.isNotBlank()
            }
            2 -> true // Photo is technically optional based on docs, or has a default avatar
            3 -> currentState.selectedInterests.size >= 3 // Min 3 required
            4 -> currentState.selectedNeeds.size >= 2     // Min 2 required
            else -> false
        }
    }

    fun updateName(name: String) = _state.update { it.copy(name = name) }
    fun updateEmail(email: String) = _state.update { it.copy(email = email) }

    // --- Step 1 Updates ---

    fun updateCountryOfOrigin(country: String) = _state.update { it.copy(countryOfOrigin = country) }
    fun updateCurrentCountry(country: String) = _state.update { it.copy(currentCountry = country) }
    fun updateCurrentCity(city: String) = _state.update { it.copy(currentCity = city) }

    fun updateUniversity(uni: String) = _state.update{
        it.copy(
            university = uni,
            // Clear custom input if they switch away from "Other"
            customUniversity = if (uni != "Other") "" else it.customUniversity
        )
    }

    fun updateCustomUniversity(uni: String) = _state.update { it.copy(customUniversity = uni) }

    fun updateCourse(course: String) = _state.update { it.copy(course = course) }
    fun updateDegreeLevel(level: String) = _state.update { it.copy(degreeLevel = level) }
    fun updateYearOfStudy(year: String) = _state.update { it.copy(yearOfStudy = year) }
    fun updateExpectedGraduationYear(year: String) = _state.update { it.copy(expectedGraduationYear = year) }
    fun updateBio(bio: String) = _state.update { it.copy(bio = bio) }

    // --- Step 2 Updates ---

    fun updateProfileImage(uri: String?) = _state.update{
        // If a real photo is uploaded, clear any selected predefined avatar
        it.copy(profileImageUri = uri, selectedAvatar = null)
    }

    fun updateSelectedAvatar(avatarId: Int?) = _state.update {
        // If an avatar is selected, clear any uploaded real photo
        it.copy(selectedAvatar = avatarId, profileImageUri = null)
    }

    // --- Step 3 Updates ---

    fun toggleInterest(interest: String)
    {
        val currentInterests = _state.value.selectedInterests.toMutableSet()
        if (currentInterests.contains(interest)) {
            currentInterests.remove(interest)
        } else {
            currentInterests.add(interest)
        }
        _state.update { it.copy(selectedInterests = currentInterests) }
    }

    // --- Step 4 Updates ---

    fun toggleNeed(need: String)
    {
        val currentNeeds = _state.value.selectedNeeds.toMutableSet()
        if (currentNeeds.contains(need)) {
            currentNeeds.remove(need)
        } else {
            currentNeeds.add(need)
        }
        _state.update { it.copy(selectedNeeds = currentNeeds) }
    }

    // --- Final Submission ---

    fun completeProfile()
    {
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
                firestore.collection("users").document(uid).set(
                    mapOf(
                        "uid" to uid,
                        "name" to s.name,
                        "email" to s.email,
                        "photoUrl" to s.photoUrl,
                        "countryOfOrigin" to s.countryOfOrigin,
                        "currentCountry" to s.currentCountry,
                        "currentCity" to s.currentCity,
                        "university" to if (s.university == "Other") s.customUniversity else s.university,
                        "course" to s.course,
                        "degreeLevel" to s.degreeLevel,
                        "yearOfStudy" to s.yearOfStudy,
                        "expectedGraduationYear" to s.expectedGraduationYear,
                        "bio" to s.bio,
                        "languagesKnown" to s.languagesKnown,
                        "avatarIndex" to s.selectedAvatar,
                        "interests" to s.selectedInterests.toList(),
                        "needs" to s.selectedNeeds.toList(),
                        "profileCompleted" to true
                    )
                ).await()
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