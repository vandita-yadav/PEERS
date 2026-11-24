package com.example.peers.features.profile

/**
 * Standardized option catalogs shared across Profile Onboarding and Edit Profile
 * to keep student data clean and consistent for matching and personalization.
 */
object ProfileOptions {

    val countries = listOf(
        "India", "United States", "United Kingdom", "Canada", "Australia", "Germany",
        "France", "Singapore", "United Arab Emirates", "Ireland", "Netherlands", "Japan",
        "South Korea", "Italy", "Spain", "New Zealand", "Switzerland", "Sweden", "Norway",
        "Finland", "Denmark", "Poland", "Malaysia", "Indonesia", "Thailand", "Vietnam",
        "Philippines", "Bangladesh", "Nepal", "Sri Lanka", "Pakistan", "Nigeria", "Kenya",
        "Ghana", "South Africa", "Egypt", "Brazil", "Mexico", "Argentina", "Colombia",
        "China", "Taiwan", "Hong Kong", "Turkey", "Saudi Arabia", "Qatar", "Russia",
        "Ukraine", "Portugal", "Austria", "Belgium", "Czech Republic", "Greece", "Hungary"
    ).sorted()

    val citiesByCountry: Map<String, List<String>> = mapOf(
        "India" to listOf(
            "Jalandhar / Phagwara", "New Delhi", "Mumbai", "Bengaluru", "Hyderabad",
            "Chennai", "Pune", "Kolkata", "Ahmedabad", "Jaipur", "Chandigarh", "Vellore",
            "Pilani", "Manipal", "Lucknow", "Indore", "Bhopal", "Kochi", "Coimbatore",
            "Guwahati", "Bhubaneswar", "Noida", "Gurugram"
        ),
        "United States" to listOf(
            "New York", "Boston", "San Francisco", "Los Angeles", "Chicago", "Seattle",
            "Austin", "Atlanta", "Philadelphia", "San Diego", "Dallas", "Houston",
            "Washington D.C.", "Ann Arbor", "Berkeley", "Palo Alto"
        ),
        "United Kingdom" to listOf(
            "London", "Manchester", "Edinburgh", "Birmingham", "Leeds", "Glasgow",
            "Oxford", "Cambridge", "Bristol", "Liverpool", "Sheffield", "Nottingham"
        ),
        "Canada" to listOf(
            "Toronto", "Vancouver", "Montreal", "Ottawa", "Waterloo", "Calgary",
            "Edmonton", "Halifax", "Winnipeg", "Quebec City"
        ),
        "Australia" to listOf(
            "Melbourne", "Sydney", "Brisbane", "Perth", "Adelaide", "Canberra"
        ),
        "Germany" to listOf(
            "Berlin", "Munich", "Frankfurt", "Hamburg", "Aachen", "Stuttgart", "Heidelberg"
        ),
        "Singapore" to listOf("Singapore"),
        "United Arab Emirates" to listOf("Dubai", "Abu Dhabi", "Sharjah"),
        "Ireland" to listOf("Dublin", "Cork", "Galway"),
        "France" to listOf("Paris", "Lyon", "Toulouse", "Nice")
    )

    val allPopularCities: List<String> by lazy {
        citiesByCountry.values.flatten().distinct().sorted()
    }

    fun citiesForCountry(country: String): List<String> {
        val primary = citiesByCountry[country].orEmpty()
        return (primary + allPopularCities).distinct()
    }

    val universities = listOf(
        "Lovely Professional University",
        "Delhi University",
        "IIT Delhi",
        "IIT Bombay",
        "IIT Madras",
        "IIT Kanpur",
        "BITS Pilani",
        "VIT Vellore",
        "SRM Institute of Science & Technology",
        "Manipal Academy of Higher Education",
        "Chandigarh University",
        "Amity University",
        "Symbiosis International University",
        "Christ University",
        "Anna University",
        "Jawaharlal Nehru University",
        "University of Toronto",
        "University of British Columbia",
        "McGill University",
        "University of Waterloo",
        "New York University (NYU)",
        "Stanford University",
        "Massachusetts Institute of Technology (MIT)",
        "Harvard University",
        "University of California, Berkeley",
        "University of Oxford",
        "University of Cambridge",
        "Imperial College London",
        "University College London (UCL)",
        "Technical University of Munich",
        "University of Melbourne",
        "University of Sydney",
        "National University of Singapore (NUS)",
        "Other"
    )

    val courses = listOf(
        "Computer Science & Engineering",
        "Information Technology",
        "Data Science & AI",
        "Software Engineering",
        "Cybersecurity",
        "Electronics & Communication Engineering",
        "Electrical Engineering",
        "Mechanical Engineering",
        "Civil Engineering",
        "Aerospace Engineering",
        "Chemical Engineering",
        "Biotechnology",
        "Business Administration (BBA / MBA)",
        "Finance & Accounting",
        "Commerce (B.Com / M.Com)",
        "Economics",
        "Marketing & Digital Media",
        "Medicine & Surgery (MBBS)",
        "Pharmacy",
        "Nursing & Healthcare",
        "Public Health",
        "Psychology",
        "Law (LLB / LLM)",
        "Architecture & Urban Design",
        "Design & Fine Arts",
        "Mass Communication & Journalism",
        "Hospitality & Tourism Management",
        "Mathematics & Statistics",
        "Physics",
        "Chemistry",
        "English & Literature",
        "Political Science & International Relations",
        "Other"
    )

    /** Exact degree levels requested in documentation (no typo: Undergraduate). */
    val degreeLevels = listOf(
        "Undergraduate",
        "Postgraduate",
        "Diploma",
        "PhD",
        "Exchange Program",
        "Other"
    )

    val studyYears = listOf(
        "1st Year",
        "2nd Year",
        "3rd Year",
        "4th Year",
        "5th Year",
        "Final Year",
        "Postgraduate Year 1",
        "Postgraduate Year 2",
        "Research / PhD",
        "Exchange / Visiting"
    )

    val graduationYears = listOf(
        "2026", "2027", "2028", "2029", "2030", "2031", "2032"
    )

    val languages = listOf(
        "English", "Hindi", "Punjabi", "Spanish", "French", "German",
        "Mandarin", "Arabic", "Japanese", "Korean", "Bengali", "Tamil",
        "Telugu", "Marathi", "Gujarati", "Urdu", "Portuguese", "Russian",
        "Italian", "Dutch"
    )

    val categorizedInterests: Map<String, List<String>> = linkedMapOf(
        "Learning & Career" to listOf(
            "Academics & Study", "Technology", "Career & Jobs",
            "Entrepreneurship", "Freelancing", "Content Writing", "Content Creation"
        ),
        "Creative" to listOf(
            "Music", "Arts & Creativity", "Photography",
            "Filmography", "Dance", "Vlogging"
        ),
        "Lifestyle & Sports" to listOf(
            "Sports", "Gym & Fitness", "Martial Arts",
            "Travel", "Food & Cooking"
        ),
        "Entertainment" to listOf(
            "Gaming", "Books", "Movies & Entertainment"
        ),
        "Social & Community" to listOf(
            "Culture & Languages", "Volunteering", "Social Events"
        )
    )

    val allInterests: List<String> by lazy {
        categorizedInterests.values.flatten()
    }

    val needs = listOf(
        "Accommodation",
        "Discussion",
        "University Help",
        "Rentals & Transport",
        "Visa & Documents",
        "Budget & Finance",
        "Transportation",
        "Healthcare",
        "Daily Life",
        "Events & Activities",
        "Language & Culture",
        "Community Support"
    )
}
