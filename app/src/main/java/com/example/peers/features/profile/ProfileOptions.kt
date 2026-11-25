package com.example.peers.features.profile

import java.time.LocalDate

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

    val statesByCountry: Map<String, List<String>> = mapOf(
        "India" to listOf(
            "Punjab", "Uttar Pradesh", "Delhi NCR", "Maharashtra", "Karnataka",
            "Tamil Nadu", "Telangana", "Rajasthan", "Gujarat", "Haryana",
            "West Bengal", "Kerala", "Madhya Pradesh", "Bihar", "Odisha",
            "Assam", "Chandigarh", "Uttarakhand", "Himachal Pradesh"
        ),
        "United States" to listOf(
            "California", "New York", "Massachusetts", "Texas", "Illinois",
            "Washington", "Georgia", "Pennsylvania", "Michigan", "Florida",
            "District of Columbia", "New Jersey"
        ),
        "United Kingdom" to listOf(
            "England", "Scotland", "Wales", "Northern Ireland"
        ),
        "Canada" to listOf(
            "Ontario", "British Columbia", "Quebec", "Alberta", "Manitoba", "Nova Scotia"
        ),
        "Australia" to listOf(
            "Victoria", "New South Wales", "Queensland", "Western Australia",
            "South Australia", "Australian Capital Territory"
        ),
        "Germany" to listOf(
            "Berlin", "Bavaria", "Baden-Wurttemberg", "North Rhine-Westphalia", "Hesse", "Hamburg"
        ),
        "United Arab Emirates" to listOf("Dubai", "Abu Dhabi", "Sharjah"),
        "Singapore" to listOf("Singapore"),
        "Ireland" to listOf("Leinster", "Munster", "Connacht"),
        "France" to listOf("Ile-de-France", "Auvergne-Rhone-Alpes", "Occitanie", "Provence-Alpes-Cote d'Azur")
    )

    val citiesByState: Map<String, List<String>> = mapOf(
        // India
        "Punjab" to listOf("Phagwara", "Jalandhar", "Ludhiana", "Amritsar", "Patiala", "Mohali", "Bathinda"),
        "Uttar Pradesh" to listOf("Lucknow", "Noida", "Greater Noida", "Ghaziabad", "Kanpur", "Varanasi", "Agra", "Prayagraj", "Meerut"),
        "Delhi NCR" to listOf("New Delhi", "Gurugram", "Noida", "Faridabad"),
        "Maharashtra" to listOf("Mumbai", "Pune", "Nagpur", "Nashik", "Aurangabad"),
        "Karnataka" to listOf("Bengaluru", "Manipal", "Mysuru", "Mangaluru", "Hubballi"),
        "Tamil Nadu" to listOf("Chennai", "Vellore", "Coimbatore", "Tiruchirappalli", "Madurai"),
        "Telangana" to listOf("Hyderabad", "Warangal"),
        "Rajasthan" to listOf("Jaipur", "Pilani", "Udaipur", "Jodhpur", "Kota"),
        "Gujarat" to listOf("Ahmedabad", "Vadodara", "Surat", "Gandhinagar", "Rajkot"),
        "Haryana" to listOf("Gurugram", "Faridabad", "Sonipat", "Kurukshetra", "Rohtak"),
        "West Bengal" to listOf("Kolkata", "Kharagpur", "Durgapur", "Siliguri"),
        "Kerala" to listOf("Kochi", "Thiruvananthapuram", "Kozhikode", "Thrissur"),
        "Madhya Pradesh" to listOf("Indore", "Bhopal", "Jabalpur", "Gwalior"),
        "Bihar" to listOf("Patna", "Gaya", "Muzaffarpur"),
        "Odisha" to listOf("Bhubaneswar", "Cuttack", "Rourkela"),
        "Assam" to listOf("Guwahati", "Silchar", "Dibrugarh"),
        "Chandigarh" to listOf("Chandigarh", "Mohali"),
        "Uttarakhand" to listOf("Dehradun", "Roorkee", "Haridwar"),
        "Himachal Pradesh" to listOf("Shimla", "Mandi", "Solan", "Hamirpur"),
        // United States
        "California" to listOf("San Francisco", "Los Angeles", "Berkeley", "Palo Alto", "San Diego", "San Jose"),
        "New York" to listOf("New York City", "Buffalo", "Ithaca", "Rochester", "Syracuse"),
        "Massachusetts" to listOf("Boston", "Cambridge", "Amherst", "Worcester"),
        "Texas" to listOf("Austin", "Dallas", "Houston", "College Station", "San Antonio"),
        "Illinois" to listOf("Chicago", "Evanston", "Urbana-Champaign"),
        "Washington" to listOf("Seattle", "Pullman", "Tacoma"),
        "Georgia" to listOf("Atlanta", "Athens"),
        "Pennsylvania" to listOf("Philadelphia", "Pittsburgh", "State College"),
        "Michigan" to listOf("Ann Arbor", "East Lansing", "Detroit"),
        "Florida" to listOf("Gainesville", "Miami", "Tampa", "Orlando"),
        "District of Columbia" to listOf("Washington D.C."),
        "New Jersey" to listOf("Princeton", "New Brunswick", "Newark"),
        // UK
        "England" to listOf("London", "Manchester", "Oxford", "Cambridge", "Birmingham", "Leeds", "Bristol", "Sheffield"),
        "Scotland" to listOf("Edinburgh", "Glasgow", "St Andrews", "Aberdeen"),
        "Wales" to listOf("Cardiff", "Swansea"),
        "Northern Ireland" to listOf("Belfast"),
        // Canada
        "Ontario" to listOf("Toronto", "Waterloo", "Ottawa", "Kingston", "London (ON)"),
        "British Columbia" to listOf("Vancouver", "Victoria", "Burnaby"),
        "Quebec" to listOf("Montreal", "Quebec City"),
        "Alberta" to listOf("Calgary", "Edmonton"),
        "Manitoba" to listOf("Winnipeg"),
        "Nova Scotia" to listOf("Halifax"),
        // Australia
        "Victoria" to listOf("Melbourne", "Geelong"),
        "New South Wales" to listOf("Sydney", "Wollongong", "Newcastle"),
        "Queensland" to listOf("Brisbane", "Gold Coast"),
        "Western Australia" to listOf("Perth"),
        "South Australia" to listOf("Adelaide"),
        "Australian Capital Territory" to listOf("Canberra"),
        // Germany & others
        "Berlin" to listOf("Berlin"),
        "Bavaria" to listOf("Munich", "Erlangen", "Regensburg"),
        "Baden-Wurttemberg" to listOf("Stuttgart", "Heidelberg", "Karlsruhe", "Freiburg"),
        "North Rhine-Westphalia" to listOf("Aachen", "Cologne", "Bonn", "Dortmund"),
        "Hesse" to listOf("Frankfurt", "Darmstadt"),
        "Hamburg" to listOf("Hamburg"),
        "Dubai" to listOf("Dubai"),
        "Abu Dhabi" to listOf("Abu Dhabi"),
        "Sharjah" to listOf("Sharjah"),
        "Singapore" to listOf("Singapore"),
        "Leinster" to listOf("Dublin"),
        "Munster" to listOf("Cork", "Limerick"),
        "Connacht" to listOf("Galway"),
        "Ile-de-France" to listOf("Paris", "Palaiseau"),
        "Auvergne-Rhone-Alpes" to listOf("Lyon", "Grenoble"),
        "Occitanie" to listOf("Toulouse", "Montpellier"),
        "Provence-Alpes-Cote d'Azur" to listOf("Nice", "Marseille")
    )

    val citiesByCountry: Map<String, List<String>> = mapOf(
        "India" to listOf(
            "Phagwara", "Jalandhar", "New Delhi", "Mumbai", "Bengaluru", "Hyderabad",
            "Chennai", "Pune", "Kolkata", "Ahmedabad", "Jaipur", "Chandigarh", "Vellore",
            "Pilani", "Manipal", "Lucknow", "Indore", "Bhopal", "Kochi", "Coimbatore",
            "Guwahati", "Bhubaneswar", "Noida", "Greater Noida", "Gurugram"
        ),
        "United States" to listOf(
            "New York City", "Boston", "Cambridge", "San Francisco", "Los Angeles", "Chicago",
            "Seattle", "Austin", "Atlanta", "Philadelphia", "San Diego", "Dallas", "Houston",
            "Washington D.C.", "Ann Arbor", "Berkeley", "Palo Alto"
        ),
        "United Kingdom" to listOf(
            "London", "Manchester", "Edinburgh", "Birmingham", "Leeds", "Glasgow",
            "Oxford", "Cambridge", "Bristol", "Liverpool", "Sheffield", "Cardiff"
        ),
        "Canada" to listOf(
            "Toronto", "Vancouver", "Montreal", "Ottawa", "Waterloo", "Calgary",
            "Edmonton", "Halifax", "Winnipeg", "Quebec City"
        ),
        "Australia" to listOf("Melbourne", "Sydney", "Brisbane", "Perth", "Adelaide", "Canberra"),
        "Germany" to listOf("Berlin", "Munich", "Frankfurt", "Hamburg", "Aachen", "Stuttgart", "Heidelberg"),
        "Singapore" to listOf("Singapore"),
        "United Arab Emirates" to listOf("Dubai", "Abu Dhabi", "Sharjah"),
        "Ireland" to listOf("Dublin", "Cork", "Galway"),
        "France" to listOf("Paris", "Lyon", "Toulouse", "Nice")
    )

    fun statesForCountry(country: String): List<String> =
        statesByCountry[country].orEmpty()

    fun citiesForState(country: String, state: String): List<String> {
        val stateCities = citiesByState[state].orEmpty()
        if (stateCities.isNotEmpty()) return stateCities
        val countryCities = citiesByCountry[country].orEmpty()
        if (countryCities.isNotEmpty()) return countryCities
        return emptyList()
    }

    val universitiesByCountry: Map<String, List<String>> = mapOf(
        "India" to listOf(
            "Lovely Professional University",
            "NIT Jalandhar (Dr. B. R. Ambedkar NIT)",
            "DAV University Jalandhar",
            "I.K. Gujral Punjab Technical University",
            "Guru Nanak Dev University, Amritsar",
            "Thapar Institute of Engineering & Technology",
            "Panjab University, Chandigarh",
            "Chandigarh University",
            "Delhi University",
            "IIT Delhi",
            "Jawaharlal Nehru University",
            "Delhi Technological University (DTU)",
            "IIT Kanpur",
            "IIT BHU Varanasi",
            "University of Lucknow",
            "Amity University Noida",
            "Galgotias University",
            "Shiv Nadar University",
            "IIT Bombay",
            "University of Mumbai",
            "Savitribai Phule Pune University",
            "Symbiosis International University",
            "IISc Bengaluru",
            "Christ University Bengaluru",
            "Manipal Academy of Higher Education",
            "IIT Madras",
            "Anna University",
            "VIT Vellore",
            "SRM Institute of Science & Technology",
            "IIT Hyderabad",
            "BITS Pilani",
            "IIT Kharagpur",
            "IIT Roorkee",
            "IIT Guwahati",
            "IIM Ahmedabad",
            "IIM Bangalore"
        ),
        "United States" to listOf(
            "Massachusetts Institute of Technology (MIT)",
            "Harvard University",
            "Stanford University",
            "University of California, Berkeley",
            "California Institute of Technology (Caltech)",
            "New York University (NYU)",
            "Columbia University",
            "Cornell University",
            "Carnegie Mellon University",
            "University of Michigan",
            "University of Texas at Austin",
            "Georgia Institute of Technology",
            "University of Washington",
            "University of Illinois Urbana-Champaign",
            "University of Pennsylvania",
            "UCLA"
        ),
        "United Kingdom" to listOf(
            "University of Oxford",
            "University of Cambridge",
            "Imperial College London",
            "University College London (UCL)",
            "London School of Economics (LSE)",
            "King's College London",
            "University of Edinburgh",
            "University of Manchester",
            "University of Bristol",
            "University of Warwick",
            "University of Leeds",
            "University of Glasgow"
        ),
        "Canada" to listOf(
            "University of Toronto",
            "University of British Columbia",
            "McGill University",
            "University of Waterloo",
            "University of Alberta",
            "University of Ottawa",
            "Queen's University",
            "Simon Fraser University",
            "Dalhousie University"
        ),
        "Australia" to listOf(
            "University of Melbourne",
            "University of Sydney",
            "Australian National University (ANU)",
            "University of New South Wales (UNSW)",
            "University of Queensland",
            "Monash University",
            "University of Western Australia",
            "University of Adelaide"
        ),
        "Germany" to listOf(
            "Technical University of Munich",
            "Ludwig Maximilian University of Munich",
            "RWTH Aachen University",
            "Heidelberg University",
            "Humboldt University of Berlin",
            "Technical University of Berlin",
            "Karlsruhe Institute of Technology (KIT)",
            "University of Stuttgart"
        ),
        "Singapore" to listOf(
            "National University of Singapore (NUS)",
            "Nanyang Technological University (NTU)",
            "Singapore Management University (SMU)"
        ),
        "United Arab Emirates" to listOf(
            "Khalifa University",
            "United Arab Emirates University",
            "American University of Sharjah",
            "Heriot-Watt University Dubai",
            "NYU Abu Dhabi"
        ),
        "Ireland" to listOf(
            "Trinity College Dublin",
            "University College Dublin",
            "University College Cork",
            "University of Galway"
        ),
        "France" to listOf(
            "Sorbonne University",
            "Ecole Polytechnique",
            "PSL University",
            "Sciences Po Paris",
            "HEC Paris"
        )
    )

    private val cityUniversityPriority: Map<String, List<String>> = mapOf(
        "Phagwara" to listOf(
            "Lovely Professional University",
            "I.K. Gujral Punjab Technical University",
            "NIT Jalandhar (Dr. B. R. Ambedkar NIT)",
            "DAV University Jalandhar"
        ),
        "Jalandhar" to listOf(
            "Lovely Professional University",
            "NIT Jalandhar (Dr. B. R. Ambedkar NIT)",
            "DAV University Jalandhar",
            "I.K. Gujral Punjab Technical University"
        ),
        "Chandigarh" to listOf("Panjab University, Chandigarh", "Chandigarh University"),
        "Mohali" to listOf("Chandigarh University", "Panjab University, Chandigarh"),
        "New Delhi" to listOf("Delhi University", "IIT Delhi", "Jawaharlal Nehru University", "Delhi Technological University (DTU)"),
        "Noida" to listOf("Amity University Noida", "Shiv Nadar University", "Galgotias University"),
        "Greater Noida" to listOf("Shiv Nadar University", "Galgotias University", "Amity University Noida"),
        "Lucknow" to listOf("University of Lucknow"),
        "Kanpur" to listOf("IIT Kanpur"),
        "Varanasi" to listOf("IIT BHU Varanasi"),
        "Mumbai" to listOf("IIT Bombay", "University of Mumbai"),
        "Pune" to listOf("Savitribai Phule Pune University", "Symbiosis International University"),
        "Bengaluru" to listOf("IISc Bengaluru", "Christ University Bengaluru", "IIM Bangalore"),
        "Manipal" to listOf("Manipal Academy of Higher Education"),
        "Chennai" to listOf("IIT Madras", "Anna University", "SRM Institute of Science & Technology"),
        "Vellore" to listOf("VIT Vellore"),
        "Pilani" to listOf("BITS Pilani"),
        "Hyderabad" to listOf("IIT Hyderabad")
    )

    fun universitiesForLocation(country: String, city: String): List<String> {
        val countryUnis = if (country.isBlank()) {
            universitiesByCountry.values.flatten().distinct()
        } else {
            universitiesByCountry[country].orEmpty()
        }
        val cityTop = cityUniversityPriority[city].orEmpty().filter { it in countryUnis }
        val combined = (cityTop + countryUnis).distinct()
        return combined + "Other"
    }

    /** Flat list retained for backward compatibility across callers. */
    val universities: List<String> by lazy {
        universitiesByCountry.values.flatten().distinct() + "Other"
    }

    /** Degree -> default program duration in years. */
    val degreeDurationMap: Map<String, Int> = linkedMapOf(
        "B.Tech" to 4,
        "B.E." to 4,
        "B.A. LL.B" to 5,
        "B.Arch" to 5,
        "MBBS" to 5,
        "B.Sc" to 3,
        "BBA" to 3,
        "B.Com" to 3,
        "BCA" to 3,
        "B.A." to 3,
        "MBA" to 2,
        "MCA" to 2,
        "M.Tech" to 2,
        "M.Sc" to 2,
        "LL.M" to 2,
        "Diploma" to 3,
        "PhD" to 4,
        "Other" to 3
    )

    val degreeLevels: List<String> = degreeDurationMap.keys.toList()

    fun durationForDegree(degree: String): Int = degreeDurationMap[degree] ?: 3

    val courses = listOf(
        "Computer Science",
        "Information Technology (IT)",
        "Computer Science & Engineering",
        "Artificial Intelligence & Data Science",
        "Software Engineering",
        "Cybersecurity",
        "Food Technology",
        "Performing Arts",
        "Electronics & Communication Engineering",
        "Electrical Engineering",
        "Mechanical Engineering",
        "Civil Engineering",
        "Aerospace Engineering",
        "Biotechnology",
        "Business Administration & Management",
        "Finance & Accounting",
        "Commerce",
        "Economics",
        "Marketing & Digital Media",
        "Law & Legal Studies",
        "Medicine & Healthcare",
        "Pharmacy",
        "Psychology",
        "Architecture & Urban Design",
        "Design & Fine Arts",
        "Journalism & Mass Communication",
        "Hospitality & Tourism Management",
        "Mathematics & Statistics",
        "Physics",
        "Chemistry",
        "English & Literature",
        "Other"
    )

    val commencingMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val commencingYears = listOf(
        "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027"
    )

    fun computeExpectedGraduationYear(commencingYear: String, durationYears: Int): String {
        val start = commencingYear.toIntOrNull() ?: return ""
        val dur = durationYears.coerceIn(1, 7)
        return (start + dur).toString()
    }

    fun computeCurrentYearLabel(
        commencingMonth: String,
        commencingYear: String,
        durationYears: Int,
    ): String {
        val startYear = commencingYear.toIntOrNull() ?: return ""
        val dur = durationYears.coerceIn(1, 7)
        val startMonthIdx = commencingMonths.indexOf(commencingMonth).let { if (it >= 0) it + 1 else 8 }
        val now = LocalDate.now()
        val elapsedYears = ((now.year - startYear) * 12 + (now.monthValue - startMonthIdx)) / 12
        val yearNum = (elapsedYears + 1).coerceIn(1, dur)
        return when (yearNum) {
            1 -> "1st Year"
            2 -> "2nd Year"
            3 -> "3rd Year"
            4 -> "4th Year"
            5 -> "5th Year"
            else -> "${yearNum}th Year"
        }
    }

    val studyYears = listOf(
        "1st Year", "2nd Year", "3rd Year", "4th Year", "5th Year",
        "Postgraduate Year 1", "Postgraduate Year 2", "Research / PhD"
    )

    val graduationYears = listOf(
        "2025", "2026", "2027", "2028", "2029", "2030", "2031", "2032"
    )

    data class DialCodeOption(val code: String, val country: String) {
        val displayLabel: String get() = "$code ($country)"
    }

    val dialCodes = listOf(
        DialCodeOption("+91", "India"),
        DialCodeOption("+1", "USA / Canada"),
        DialCodeOption("+44", "United Kingdom"),
        DialCodeOption("+61", "Australia"),
        DialCodeOption("+971", "UAE"),
        DialCodeOption("+65", "Singapore"),
        DialCodeOption("+49", "Germany"),
        DialCodeOption("+33", "France"),
        DialCodeOption("+353", "Ireland"),
        DialCodeOption("+977", "Nepal"),
        DialCodeOption("+880", "Bangladesh"),
        DialCodeOption("+94", "Sri Lanka"),
        DialCodeOption("+234", "Nigeria"),
        DialCodeOption("+254", "Kenya"),
        DialCodeOption("+233", "Ghana"),
        DialCodeOption("+27", "South Africa"),
        DialCodeOption("+60", "Malaysia"),
        DialCodeOption("+81", "Japan"),
        DialCodeOption("+82", "South Korea")
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
        "Helplines & Contacts",
        "Healthcare",
        "Daily Life",
        "Events & Activities",
        "Language & Culture"
    )
}
