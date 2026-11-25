package com.example.peers.features.assistant

/**
 * Natural, conversational PEERS app fallback advisor used when live Gemini API key
 * is not configured or the device is offline.
 */
object StudyAbroadKnowledgeBase {

    fun generateResponse(userQuery: String): Pair<String, AssistantAction?> {
        val q = userQuery.lowercase()

        return when {
            q.contains("rental") || q.contains("transport") || q.contains("bike") ||
                q.contains("scooty") || q.contains("cycle") || q.contains("shuttle") ||
                q.contains("cab") || q.contains("exchange") -> {
                "Around campus, the most budget-friendly way to commute is picking up a geared cycle pass for short hostel-to-class trips or renting a scooty by the month if you stay in Law Gate or off-campus PGs. For airport and railway runs, you can save a lot by splitting a cab through the PEERS Verified Cab Desk or hopping on the 15-minute campus shuttle. Check out the Rentals & Transport page in PEERS to compare daily and monthly rates side by side." to
                    AssistantAction("Open Rentals & Transport", AssistantActionType.OPEN_RENTALS)
            }

            q.contains("lease") || q.contains("house") || q.contains("housing") ||
                q.contains("flat") || q.contains("accommodation") || q.contains("pg") ||
                q.contains("room") -> {
                "Before paying any booking deposit for a PG or flat, make sure you verify whether electricity, Wi-Fi, and laundry are included in the monthly rent and inspect water pressure and mobile signal inside the room. Always get the agreement in writing and check student reviews inside Nearby Accommodation so you pick stays that fellow peers have verified." to
                    AssistantAction("Explore Accommodation", AssistantActionType.OPEN_ACCOMMODATION)
            }

            q.contains("budget") || q.contains("bank") || q.contains("forex") ||
                q.contains("expense") || q.contains("money") || q.contains("cost") -> {
                "A great rule of thumb is setting your monthly rent ceiling first, then logging realistic estimates for groceries, local commute, and dining out so surprises don't eat into your savings. If you're opening a new student bank account, keep your passport, admission letter, and address proof handy, and track your day-to-day spending right inside the PEERS Budget Planner." to
                    AssistantAction("Open Budget Planner", AssistantActionType.OPEN_BUDGET)
            }

            q.contains("event") || q.contains("meetup") || q.contains("workshop") ||
                q.contains("club") || q.contains("society") || q.contains("fest") -> {
                "Campus meetups, speaking clubs, and society fairs are one of the quickest ways to settle in during your first few weeks. You can browse upcoming dates, RSVP, or bookmark activities in the Events tab so they sync straight to your personal schedule." to
                    AssistantAction("Browse Events", AssistantActionType.OPEN_EVENTS)
            }

            q.contains("discuss") || q.contains("forum") || q.contains("community") ||
                q.contains("peer") || q.contains("senior") -> {
                "If you're looking for honest local tips—like airport cab pooling, textbook recommendations, or professor advice—head over to your university or city channel in the Discussion Forum. Seniors and fellow students post practical threads there every day, and you can start your own question anytime." to
                    AssistantAction("Join Discussions", AssistantActionType.OPEN_COMMUNITY)
            }

            q.contains("visa") || q.contains("checklist") || q.contains("document") ||
                q.contains("arrival") || q.contains("pack") || q.contains("health") ||
                q.contains("insurance") -> {
                "To keep your arrival stress-free, make sure your passport, admission letter, insurance papers, and emergency cash are easy to reach before your trip, then activate your university portal and local SIM right when you land. Your PEERS Student Checklist organizes every urgent document and arrival step for your campus automatically." to
                    AssistantAction("View Student Checklist", AssistantActionType.OPEN_CHECKLIST)
            }

            else -> {
                "I'm here to help you get the most out of PEERS! You can ask me about finding verified PGs in Accommodation, comparing bike or shuttle rates in Rentals & Transport, tracking expenses in the Budget Planner, preparing documents in your Student Checklist, or joining campus Events and Discussions." to null
            }
        }
    }
}
