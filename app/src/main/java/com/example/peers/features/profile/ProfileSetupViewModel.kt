package com.example.peers.features.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileSetupViewModel : ViewModel()
{
    private val _state = MutableStateFlow(ProfileSetupState())
    val state: StateFlow<ProfileSetupState> = _state.asStateFlow()

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
                // Ensure required fields are not empty
                val isUniversityValid = if (currentState.university == "Other") {
                    currentState.customUniversity.isNotBlank()
                }
                else
                {
                    currentState.university.isNotBlank()
                }

                currentState.countryOfOrigin.isNotBlank() &&
                        currentState.currentCountry.isNotBlank() &&
                        currentState.currentCity.isNotBlank() &&
                        isUniversityValid &&
                        currentState.course.isNotBlank() &&
                        currentState.degreeLevel.isNotBlank() &&
                        currentState.yearOfStudy.isNotBlank()
            }
            2 -> true // Photo is technically optional based on docs, or has a default avatar
            3 -> currentState.selectedInterests.size >= 3 // Min 3 required
            4 -> currentState.selectedNeeds.size >= 2     // Min 2 required
            else -> false
        }
    }

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

    fun completeProfile(userId: String)
    {
        if (canProceedToNextStep()) {
            val finalData = _state.value
            // TODO: Dispatch finalData and userId to Firebase Firestore
            println("Profile Completed for User: $userId. Data: $finalData")
        }
    }
}