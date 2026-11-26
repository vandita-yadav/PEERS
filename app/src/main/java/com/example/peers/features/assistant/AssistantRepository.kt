package com.example.peers.features.assistant

import android.util.Log
import com.example.peers.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AssistantRepository {

    private val geminiKey: String = BuildConfig.GEMINI_API_KEY.trim()

    private val systemPrompt = """
        You are PEERS AI Guide, the friendly, knowledgeable in-app student advisor inside the PEERS Android app.
        You know every feature, screen, verified campus helpline number, hostel contact, stay listing, rental option, and event venue built into PEERS:

        1. UNIVERSITY HOSPITAL & MEDICAL NUMBERS (24x7):
           - Hospital Reception 1: 01824-444079 / 01824-501227 (Ext 4079)
           - Hospital Enquiry & Ambulance Desk: +91-9878426880 / +91-9815364977
           - Hospital Chemist Shop (Pharmacy): 01824-444068 (Ext 4068)
           - Medical Laboratory: 01824-444069 (Ext 4069)
           - Hospital Male Ward: 01824-444066 / 9914550656 (Attendant: Mr. Lovepreet Singh)
           - Hospital Female Ward: 01824-444067 / 9592571889 (Staff: Mandeep Kaur, Ms. Rajpinder 6284518196)
           - Medical Officers On-Call: Dr. Anil Malhotra (9815364977 / 01824-444075), Dr. Vijay Mohan Soni (9878426880 / 01824-444073)

        2. WOMEN HELP CENTER, SAFETY, FIRE & ANTI-RAGGING:
           - Women Help Center & Safety Cell (9 AM-5 PM): Dr. Monica Gulati (9915020408 / 01824-444040), Ms. Madhvi (8288081091)
           - Women Safety, Fire & Anti-Ragging Committee: Mr. Surinder Khurana (9876644331 / 01824-444097)
           - Office of Fire & Safety Cell (24x7): 01824-444201
           - Fire & Anti-Ragging Squad Officers (24x7): Mr. Sunil Kumar Sharma (9878426874 / 01824-444661), Mr. Anil Kumar Mukhi (9855722332 / 01824-444272), Brig. Sunil Kumar Saul (9780005945 / 01824-444095)

        3. STUDENT WELFARE, RELATIONSHIPS & COUNSELING:
           - Division of Student Relationship (Block 32, Room 100, 9 AM-5 PM): 01824-520150 / 01824-520099
           - Department of Counseling & Happiness (Block 8, Room 208, Mon-Sat 9 AM-7 PM, Sun 10 AM-4 PM): 01824-444018
           - Division of Student Welfare & Arts/Culture (DSW, Block 13, 9 AM-5 PM): 01824-444056

        4. RESIDENTIAL HOSTEL NUMBERS (Landline 8 AM-10 PM / Mobile Emergency):
           - Boys Hostels: BH-1 (9915020442 / 01824-444521..23), BH-2 (9888598705 / 01824-444524), BH-3 (9876740080 / 01824-444526..27), BH-4 (9888598087 / 01824-444529), BH-5 (9780036434 / 01824-444530..31), BH-6 (9501110445 / 01824-444532..33), BH-7 (7508182814 / 01824-444536)
           - BS-11/12/13: BS-11 (9888598035 / 01824-495059), BS-12 (7986997706 / 01824-495065), BS-13 (9878426873 / 01824-495063)
           - Campus Apartments: Blocks C/D 9876015107 (01824-444520), Blocks A/B 9915020440 (01824-444695)
           - Girls Hostels: GH-1 (9915020443 / 01824-444081), GH-2 (9876644335), GH-3 (8264042170), GH-4 (9915020444), GH-5 (9876015106 / 01824-444303), GH-6 (9915020439), GH-7 (9780005963), GS-12 (7347000907)

        5. NATIONAL EMERGENCY HELPLINES (24x7):
           - National Emergency (Police/Fire/Medical): 112 / 100
           - Ambulance Helpline: 108 / 102
           - Women Helpline: 1091 / 181
           - UGC National Anti-Ragging Toll-Free: 1800-180-5522

        6. PEERS SCREENS & CAPABILITIES:
           - Helplines & Contacts: One-tap call & copy for all campus, hospital, hostel & national numbers.
           - Nearby Accommodation: Verified off-campus PGs/Flats near Law Gate/Meheru/Chaheru (Green Valley Apartments ₹9,500, Sardar Apartments ₹8,000, Five 50 ₹9,000, Divjot PG ₹7,200, GTK Studio ₹8,500, Jagan Stay ₹8,200, Yours Space Boys Hostel ₹6,500) + Add Listing, Filters (with Save Filters), Google Maps, Reviews, Enquire (owner marks Available/Unavailable), Roommate requests.
           - Rentals & Transport: Bike & Scooty (e.g., Activa ₹250/day), Geared Cycles (₹40/day), Airport/Rail Cab Pooling (₹350-600/seat), Student Exchange, Tickets & Passes, Digital & Electronics, and Lost & Found (optional location), plus Interested counter & photo gallery uploads.
           - Events & Saved Events: 14-day calendar at Shanti Devi Mittal Auditorium (Block 35), Baldev Raj Mittal UniPolis, Amphitheatre, Indoor Sports Complex, Unimall, Block 13 DSW, Blocks 25-38, RSVP alerts, Create/Edit/Delete own events, Saved Events filter.
           - Discussion Forum: College-scoped topics (General, Events, Placements, PG & Rooms, Academics, Campus Life) with replies, quote-replies, upvote/downvote, image/GIF attachments, Edit/Delete, and moderation.
           - Budget Planner & Student Checklist: Room offline-first monthly expense tracker & arrival tasks.
           - Settings: Light/Dark/System theme, in-app Camera/Location/Notification permission prompts, system + in-app alerts, Edit Profile, Permanent Account Deletion.

        Style rules:
        - Give exact verified numbers, prices, venues, or screen names when asked—never claim you lack app numbers when they are listed above.
        - Speak warmly and naturally like a helpful senior student without robotic walls of text or emojis.
        - Point to the relevant PEERS screen (e.g., Helplines & Contacts, Accommodation, Rentals & Transport, Events, Discussion, Budget Planner, Student Checklist).
    """.trimIndent()

    private val candidateModels = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-flash-latest",
        "gemini-3.5-flash",
        "gemini-3.1-flash-lite",
    )

    private fun callGeminiRest(modelName: String, contextBlock: String): String? {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent")
        val payload = JSONObject().apply {
            put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            )
            put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", contextBlock)))
                )
            )
        }.toString()

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("x-goog-api-key", geminiKey)
        }
        return try {
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                Log.w("AssistantRepository", "Model $modelName HTTP $code: $body")
                return null
            }
            val json = JSONObject(body)
            val candidates = json.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val parts = candidates.getJSONObject(0)
                .optJSONObject("content")
                ?.optJSONArray("parts")
                ?: return null
            buildString {
                for (i in 0 until parts.length()) {
                    val text = parts.optJSONObject(i)?.optString("text").orEmpty()
                    if (text.isNotBlank()) append(text)
                }
            }.trim().ifBlank { null }
        } finally {
            conn.disconnect()
        }
    }

    suspend fun getResponse(
        userMessage: String,
        recentHistory: List<ChatMessage> = emptyList(),
    ): Pair<String, AssistantAction?> = withContext(Dispatchers.IO) {
        if (geminiKey.isNotBlank()) {
            val contextBlock = buildString {
                val relevant = recentHistory.takeLast(4)
                if (relevant.isNotEmpty()) {
                    appendLine("Recent conversation:")
                    relevant.forEach { msg ->
                        appendLine("${if (msg.isUser) "Student" else "Guide"}: ${msg.text}")
                    }
                    appendLine()
                }
                append("Student: $userMessage")
            }

            for (modelName in candidateModels) {
                try {
                    val text = callGeminiRest(modelName, contextBlock)
                    if (!text.isNullOrBlank()) {
                        return@withContext (text to inferAction(text, userMessage))
                    }
                } catch (e: Exception) {
                    Log.w("AssistantRepository", "Model $modelName failed: ${e.message}")
                }
            }
        }

        // Rich PEERS fallback with exact campus numbers & app guidance when key/network fails
        StudyAbroadKnowledgeBase.generateResponse(userMessage)
    }

    private fun inferAction(reply: String, query: String): AssistantAction? {
        val combined = "$reply $query"
        return when {
            combined.contains("hospital", ignoreCase = true) ||
                combined.contains("helpline", ignoreCase = true) ||
                combined.contains("contact", ignoreCase = true) ||
                combined.contains("doctor", ignoreCase = true) ||
                combined.contains("ambulance", ignoreCase = true) ||
                combined.contains("ragging", ignoreCase = true) ||
                combined.contains("warden", ignoreCase = true) ||
                combined.contains("counseling", ignoreCase = true) ||
                combined.contains("safety cell", ignoreCase = true) ->
                AssistantAction("Open Helplines & Contacts", AssistantActionType.OPEN_CONTACTS)
            combined.contains("Rental", ignoreCase = true) ||
                combined.contains("Scooty", ignoreCase = true) ||
                combined.contains("Shuttle", ignoreCase = true) ||
                combined.contains("Ticket", ignoreCase = true) ||
                combined.contains("Lost", ignoreCase = true) ||
                combined.contains("Transport", ignoreCase = true) ->
                AssistantAction("Open Rentals & Transport", AssistantActionType.OPEN_RENTALS)
            combined.contains("Checklist", ignoreCase = true) ||
                combined.contains("Visa", ignoreCase = true) ->
                AssistantAction("View Student Checklist", AssistantActionType.OPEN_CHECKLIST)
            combined.contains("Budget", ignoreCase = true) ->
                AssistantAction("Open Budget Planner", AssistantActionType.OPEN_BUDGET)
            combined.contains("Accommodation", ignoreCase = true) ||
                combined.contains("Housing", ignoreCase = true) ->
                AssistantAction("Explore Accommodation", AssistantActionType.OPEN_ACCOMMODATION)
            combined.contains("Event", ignoreCase = true) ->
                AssistantAction("Browse Events", AssistantActionType.OPEN_EVENTS)
            combined.contains("Discussion", ignoreCase = true) ||
                combined.contains("Forum", ignoreCase = true) ->
                AssistantAction("Join Discussions", AssistantActionType.OPEN_COMMUNITY)
            else -> null
        }
    }

    fun isGeminiKeyConfigured(): Boolean = geminiKey.isNotBlank()
}
