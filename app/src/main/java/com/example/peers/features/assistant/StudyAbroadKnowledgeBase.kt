package com.example.peers.features.assistant

/**
 * Rich conversational PEERS app fallback advisor containing exact campus hospital,
 * helpline, hostel, venue, and accommodation directory details whenever live Gemini
 * calls are offline or unavailable.
 */
object StudyAbroadKnowledgeBase {

    fun generateResponse(userQuery: String): Pair<String, AssistantAction?> {
        val q = userQuery.lowercase()

        return when {
            q.contains("hospital") || q.contains("medical") || q.contains("doctor") ||
                q.contains("ambulance") || q.contains("chemist") || q.contains("pharmacy") ||
                q.contains("ward") -> {
                "For LPU University Hospital (24x7), call Reception at 01824-444079 or 01824-501227, or Hospital Enquiry at +91-9878426880 and +91-9815364977. For doctors on call, you can reach Dr. Anil Malhotra (9815364977 / 01824-444075) or Dr. Vijay Mohan Soni (9878426880 / 01824-444073); Chemist is 01824-444068, Lab is 01824-444069, Male Ward 01824-444066, Female Ward 01824-444067, and National Ambulance is 108. You can dial any of these in one tap from Helplines & Contacts." to
                    AssistantAction("Open Helplines & Contacts", AssistantActionType.OPEN_CONTACTS)
            }

            q.contains("women") || q.contains("safety") || q.contains("ragging") ||
                q.contains("fire") || q.contains("security") || q.contains("emergency") ||
                q.contains("police") || q.contains("helpline") || q.contains("number") ||
                q.contains("contact") -> {
                "For campus safety or emergencies, call Fire & Safety Cell (24x7) at 01824-444201, Women Safety & Anti-Ragging (Mr. Surinder Khurana) at 9876644331 / 01824-444097, or Women Help Center (Dr. Monica Gulati: 9915020408, Ms. Madhvi: 8288081091). DSW (Block 13) is 01824-444056, Student Relationship (Block 32) is 01824-520150, Counseling (Block 8) is 01824-444018, and national numbers are 112 (Emergency), 1091 (Women), and 1800-180-5522 (Anti-Ragging). Open Helplines & Contacts for all hostel and office numbers." to
                    AssistantAction("Open Helplines & Contacts", AssistantActionType.OPEN_CONTACTS)
            }

            q.contains("hostel") || q.contains("warden") || q.contains("bh-") || q.contains("gh-") -> {
                "You can reach Boys Hostels BH-1 (9915020442 / 01824-444521), BH-2 (9888598705), BH-3 (9876740080), BH-4 (9888598087), BH-5 (9780036434), BH-6 (9501110445), BH-7 (7508182814), BS-11 (9888598035), Campus Apartments (9876015107 / 9915020440), and Girls Hostels GH-1 (9915020443), GH-2 (9876644335), GH-3 (8264042170), GH-4 (9915020444), GH-5 (9876015106), GH-6 (9915020439), GH-7 (9780005963), GS-12 (7347000907) inside Helplines & Contacts." to
                    AssistantAction("Open Helplines & Contacts", AssistantActionType.OPEN_CONTACTS)
            }

            q.contains("rental") || q.contains("transport") || q.contains("bike") ||
                q.contains("scooty") || q.contains("cycle") || q.contains("shuttle") ||
                q.contains("cab") || q.contains("exchange") || q.contains("ticket") ||
                q.contains("digital") || q.contains("lost") || q.contains("found") -> {
                "Inside Rentals & Transport, you can browse or post Bike & Scooty rentals (e.g., Activa around ₹250/day), Geared Cycles (₹40/day), Airport/Railway Cab Pooling (₹350–₹600/seat), Student Exchange, spare Railway/Movie Tickets & Passes, Digital & Electronics items, and Lost & Found notices (where location is optional). You can also tap Interested or call/email the poster directly." to
                    AssistantAction("Open Rentals & Transport", AssistantActionType.OPEN_RENTALS)
            }

            q.contains("lease") || q.contains("house") || q.contains("housing") ||
                q.contains("flat") || q.contains("accommodation") || q.contains("pg") ||
                q.contains("room") || q.contains("green valley") || q.contains("sardar") ||
                q.contains("law gate") -> {
                "Curated LPU-area stays include Green Valley Apartments (₹9,500/mo), Sardar Apartments (₹8,000/mo), Five 50 Apartments (₹9,000/mo), Divjot PG at Simar Enclave (₹7,200/mo), GTK Studio Apartments (₹8,500/mo), Jagan Stay (₹8,200/mo), and Yours Space Boys Hostel (₹6,500/mo). Open Nearby Accommodation to filter, save preferences, check reviews, call or enquire with owners, or match with roommates." to
                    AssistantAction("Explore Accommodation", AssistantActionType.OPEN_ACCOMMODATION)
            }

            q.contains("budget") || q.contains("bank") || q.contains("forex") ||
                q.contains("expense") || q.contains("money") || q.contains("cost") -> {
                "Use the Budget & Cost of Living Planner to set your monthly spending cap, log rent, food, commute, and book expenses per month, and watch category totals stay on track offline via Room database storage." to
                    AssistantAction("Open Budget Planner", AssistantActionType.OPEN_BUDGET)
            }

            q.contains("event") || q.contains("meetup") || q.contains("workshop") ||
                q.contains("club") || q.contains("society") || q.contains("fest") ||
                q.contains("auditorium") || q.contains("unipolis") -> {
                "Campus & City Events tracks upcoming shows, talks, and trials across Shanti Devi Mittal Auditorium (Block 35), Baldev Raj Mittal UniPolis, Amphitheatre, Indoor Sports Complex, Unimall, Block 13 DSW, and Academic Blocks 25–38. You can RSVP for instant alerts, save bookmarks to view in Saved Events, or create your own event." to
                    AssistantAction("Browse Events", AssistantActionType.OPEN_EVENTS)
            }

            q.contains("discuss") || q.contains("forum") || q.contains("community") ||
                q.contains("peer") || q.contains("senior") || q.contains("placement") -> {
                "Head to the Discussion page to explore college-scoped threads across General, Events, Placements, PG & Rooms, Academics, and Campus Life. You can post questions, upvote or downvote replies, quote-reply to specific comments, attach photos or GIFs, and edit or delete your own posts." to
                    AssistantAction("Join Discussions", AssistantActionType.OPEN_COMMUNITY)
            }

            q.contains("visa") || q.contains("checklist") || q.contains("document") ||
                q.contains("arrival") || q.contains("pack") || q.contains("insurance") -> {
                "Your PEERS Student Checklist organizes visa, banking, hostel, health, and academic onboarding steps tailored to your profile and country, letting you check off tasks or add custom reminders offline." to
                    AssistantAction("View Student Checklist", AssistantActionType.OPEN_CHECKLIST)
            }

            else -> {
                "I know every section of PEERS! Ask me for LPU Hospital or hostel/safety helpline numbers, off-campus PGs around Law Gate/Meheru, bike/cycle/ticket/digital listings in Rentals & Transport, upcoming campus Events, Discussion topics, Student Checklist tasks, or your Budget Planner." to null
            }
        }
    }
}
