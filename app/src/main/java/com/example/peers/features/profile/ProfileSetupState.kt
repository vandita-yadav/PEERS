package com.example.peers.features.profile

data class ProfileSetupState(
    val currentStep: Int = 1,

    // Identity (prefilled from Google / Manual Sign-Up)
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,

    // Step 1 — Basic Information
    val countryOfOrigin: String = "",
    val currentCountry: String = "",
    val currentState: String = "",
    val currentCity: String = "",
    val university: String = "",
    val customUniversity: String = "",
    val degreeLevel: String = "",
    val degreeDurationYears: Int = 0,
    val course: String = "",
    val customCourse: String = "",
    val commencingMonth: String = "",
    val commencingYear: String = "",
    val yearOfStudy: String = "",
    val expectedGraduationYear: String = "",
    val phoneCountryCode: String = "+91",
    val phoneNumber: String = "",
    val bio: String = "",
    val languagesKnown: List<String> = emptyList(),

    // Step 2 — Profile Picture
    val profileImageUri: String? = null,
    val croppedPhotoDataUri: String? = null,
    val selectedAvatar: String? = null,

    // Steps 3 & 4 — Interests & PEERS Needs
    val selectedInterests: Set<String> = emptySet(),
    val selectedNeeds: Set<String> = emptySet(),

    // Completion screen ("You're All Set!")
    val isCompletedScreen: Boolean = false,
    val completedUserId: String? = null
)
