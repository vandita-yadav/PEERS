package com.example.peers.features.checklist

import androidx.compose.ui.graphics.Color
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import java.util.UUID

enum class ChecklistCategory(val label: String, val emoji: String, val color: Color) {
    ALL("All", "📋", PeersPrimaryIndigo),
    VISA_DOCS("Visa & Docs", "📄", PeersLavender),
    ACADEMIC("Academic", "🎓", PeersSky),
    FINANCE("Financial", "💰", PeersMint),
    HOUSING_PACKING("Housing & Packing", "🎒", Color(0xFFFFD9E2)),
    ARRIVAL("Upon Arrival", "🛬", Color(0xFFFFE3C7)),
    CUSTOM("Custom", "📌", Color(0xFFE4DDFF)),
}

enum class ChecklistPriority(val label: String, val color: Color) {
    HIGH("Urgent", Color(0xFFE53935)),
    MEDIUM("Important", Color(0xFFFB8C00)),
    LOW("Standard", Color(0xFF5E35B1)),
}

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: ChecklistCategory,
    val priority: ChecklistPriority = ChecklistPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

object ChecklistGenerator {

    /**
     * Rule-based intelligent checklist generator based on the student's profile:
     * destination, course, university, and degree level.
     */
    fun generateDefaults(user: DashboardUser?): List<ChecklistItem> {
        val uni = user?.university?.ifBlank { "University" } ?: "University"
        val course = user?.course?.ifBlank { "your program" } ?: "your program"
        val isAbroad = !user?.currentCountry.isNullOrBlank() && user?.currentCountry != user?.countryOfOrigin

        val items = mutableListOf<ChecklistItem>()

        // 1. Visa & Official Documents
        items += ChecklistItem(
            title = "Passport Validity Check",
            description = "Ensure your passport is valid for at least 6 months beyond your expected stay.",
            category = ChecklistCategory.VISA_DOCS,
            priority = ChecklistPriority.HIGH,
        )
        items += ChecklistItem(
            title = "Official Admission Letter & I-20 / CAS",
            description = "Download and print multiple physical copies of your $uni admission letter.",
            category = ChecklistCategory.VISA_DOCS,
            priority = ChecklistPriority.HIGH,
        )
        if (isAbroad) {
            items += ChecklistItem(
                title = "Student Visa Stamping & Biometrics",
                description = "Complete visa interview appointment, pay SEVIS/visa fee, and keep receipt safe.",
                category = ChecklistCategory.VISA_DOCS,
                priority = ChecklistPriority.HIGH,
            )
        }
        items += ChecklistItem(
            title = "Academic Transcripts & Certificates",
            description = "Gather notarized or attested originals and copies of your degree/diploma certificates.",
            category = ChecklistCategory.VISA_DOCS,
            priority = ChecklistPriority.MEDIUM,
        )
        items += ChecklistItem(
            title = "Student Health & Travel Insurance",
            description = "Purchase comprehensive student health insurance meeting university coverage standards.",
            category = ChecklistCategory.VISA_DOCS,
            priority = ChecklistPriority.HIGH,
        )

        // 2. Financial Preparation
        items += ChecklistItem(
            title = "International Multi-Currency Forex Card",
            description = "Get a student forex card loaded with funds for initial living and tuition expenses.",
            category = ChecklistCategory.FINANCE,
            priority = ChecklistPriority.HIGH,
        )
        items += ChecklistItem(
            title = "Emergency Foreign Currency Cash",
            description = "Keep $150–$300 equivalent in local cash for transit, taxis, and initial meals.",
            category = ChecklistCategory.FINANCE,
            priority = ChecklistPriority.MEDIUM,
        )
        items += ChecklistItem(
            title = "Bank Account Setup Documents",
            description = "Prepare address proof, passport copies, and student letter to open a student bank account.",
            category = ChecklistCategory.FINANCE,
            priority = ChecklistPriority.MEDIUM,
        )

        // 3. Academic & Tech Preparation
        items += ChecklistItem(
            title = "Course Syllabus & Laptop Setup",
            description = "Prepare your computer with required IDEs, tools, and textbooks for $course.",
            category = ChecklistCategory.ACADEMIC,
            priority = ChecklistPriority.MEDIUM,
        )
        items += ChecklistItem(
            title = "University Student Portal & Email Activation",
            description = "Log into $uni student portal, set up your institutional email, and upload ID photo.",
            category = ChecklistCategory.ACADEMIC,
            priority = ChecklistPriority.HIGH,
        )
        items += ChecklistItem(
            title = "Connect with Faculty / Academic Advisor",
            description = "Check department timetable, orientation schedules, and prerequisite reading lists.",
            category = ChecklistCategory.ACADEMIC,
            priority = ChecklistPriority.LOW,
        )

        // 4. Housing & Packing
        items += ChecklistItem(
            title = "Accommodation Confirmation / Lease",
            description = "Finalize lease agreement or confirm temporary student accommodation near campus.",
            category = ChecklistCategory.HOUSING_PACKING,
            priority = ChecklistPriority.HIGH,
        )
        items += ChecklistItem(
            title = "Universal Power Adapter & Extension Cord",
            description = "Pack 2–3 universal electrical plug adapters suited for destination sockets.",
            category = ChecklistCategory.HOUSING_PACKING,
            priority = ChecklistPriority.MEDIUM,
        )
        items += ChecklistItem(
            title = "Prescription Medications & Doctor's Letter",
            description = "Pack 3-month supply of essential medicines with doctor's formal prescription letter.",
            category = ChecklistCategory.HOUSING_PACKING,
            priority = ChecklistPriority.HIGH,
        )
        items += ChecklistItem(
            title = "Weather-Appropriate Clothing & Essentials",
            description = "Pack thermal layers, warm jacket, rainwear, and formal interview attire.",
            category = ChecklistCategory.HOUSING_PACKING,
            priority = ChecklistPriority.MEDIUM,
        )

        // 5. Arrival & Orientation
        items += ChecklistItem(
            title = "Airport Transit & Campus Pickup",
            description = "Book university airport shuttle or arrange cab transport to your housing.",
            category = ChecklistCategory.ARRIVAL,
            priority = ChecklistPriority.HIGH,
        )
        items += ChecklistItem(
            title = "Local SIM Card / eSIM Activation",
            description = "Set up a local phone plan or activate roaming eSIM before landing.",
            category = ChecklistCategory.ARRIVAL,
            priority = ChecklistPriority.HIGH,
        )
        items += ChecklistItem(
            title = "Attend International Student Orientation",
            description = "Participate in $uni campus welcome tours, meet student mentors, and get your physical ID card.",
            category = ChecklistCategory.ARRIVAL,
            priority = ChecklistPriority.HIGH,
        )
        items += ChecklistItem(
            title = "Join Campus Student Societies on PEERS",
            description = "Explore PEERS discussion forums, find flatmates, and RSVP for campus meetups.",
            category = ChecklistCategory.ARRIVAL,
            priority = ChecklistPriority.LOW,
        )

        return items
    }
}
