package com.example.peers.features.profile

data class ProfileSetupState(
    val currentStep: Int = 1,

    // Step 1
    val countryOfOrigin: String = "",
    val currentCountry: String = "",
    val currentCity: String = "",
    val university: String = "",
    val customUniversity: String = "",
    val course: String = "",
    val degreeLevel: String = "",
    val yearOfStudy: String = "",
    val expectedGraduationYear: String = "",
    val bio: String = "",
    val languagesKnown: List<String> = emptyList(),

    // Step 2
    val profileImageUri: String? = null,
    val selectedAvatar: Int? = null,

    // Steps 3 & 4
    val selectedInterests: Set<String> = emptySet(),
    val selectedNeeds: Set<String> = emptySet()
)