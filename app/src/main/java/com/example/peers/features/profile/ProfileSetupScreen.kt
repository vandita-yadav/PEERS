package com.example.peers.features.profile


data class ProfileSetupState (
    val currentStep: Int = 1,

    // Basic Information
    val countryOfOrigin: String = "",
    val currentCountry: String = "",
    val currentCity: String = "",
    val course: String = "",
    val degreeLevel: String = "",
    val yearOfStudy: String = "",
    val graduationYear: String = "",
    val bio: String = "",
    val languagesKnown: List<String> = emptyList(),

    //Profile Picture
    val profileImageUri: String? = null,

    // Interests
    val selectedInterests: Set<String> = emptySet(),

    // PEERS Preferences
    val selectedNeeds: Set<String> = emptySet()
)


class ProfileSetupScreen {
}