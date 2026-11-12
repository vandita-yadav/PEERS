package com.example.peers.features.assistant

object StudyAbroadKnowledgeBase {

    fun generateResponse(userQuery: String): Pair<String, AssistantAction?> {
        val q = userQuery.lowercase()

        return when {
            // Visa & Work Hours
            q.contains("visa") || q.contains("work hour") || q.contains("working") || q.contains("f-1") || q.contains("tier 4") || q.contains("20 hour") -> {
                """
                🛂 **International Student Work Regulations**
                
                Most major study-abroad destinations (US, UK, Canada, Australia, Ireland) have strict legal guidelines for international student employment:
                
                1. **Term-Time Limits:**
                   • Typically capped at **20 hours per week** while school is in active session.
                   • Exceeding 20 hours is a serious visa violation that can jeopardize your status.
                
                2. **Vacation / Semester Breaks:**
                   • Students can usually work full-time (**up to 40 hours/week**) during scheduled academic holidays, summer, and winter breaks.
                
                3. **On-Campus vs. Off-Campus:**
                   • In the US (F-1 visa), off-campus work without CPT/OPT authorization is generally prohibited. Stick strictly to verified on-campus roles (library, dining hall, research assistantships).
                   • In the UK and Canada, off-campus part-time work is allowed within your 20-hour weekly allowance.
                
                💡 *Pro Tip:* Always track your clocked hours carefully each week to ensure your employer does not accidentally schedule you over the 20-hour limit.
                """.trimIndent() to AssistantAction("View Student Checklist", AssistantActionType.OPEN_CHECKLIST)
            }

            // Bank Account & Forex
            q.contains("bank") || q.contains("forex") || q.contains("account") || q.contains("money") || q.contains("currency") || q.contains("wire") -> {
                """
                💰 **Student Banking & Money Guide**
                
                Setting up your finances smoothly in your host country:
                
                1. **Initial Days (Forex & Multi-Currency Cards):**
                   • Carry a multi-currency student forex card loaded in the host currency to avoid 3.5%+ foreign markup fees.
                   • Keep roughly $300–$500 in physical cash for immediate transit emergencies.
                
                2. **Opening a Local Student Bank Account:**
                   • What you need: **Passport**, valid **Student Visa**, University **Enrollment Letter / Bank Letter**, and proof of address (dorm lease or tenancy agreement).
                   • Digital-first options (like Monzo, Revolut, Chase College Checking, TD Student) allow fast setup with minimal paper bureaucracy.
                
                3. **International Tuition Transfers:**
                   • Never use standard credit cards for full tuition; bank wire or services like Wise/Flywire have significantly better exchange rates.
                
                📊 Track your monthly rent, groceries, and leisure spending easily using the PEERS Budget Planner!
                """.trimIndent() to AssistantAction("Open Budget Planner", AssistantActionType.OPEN_BUDGET)
            }

            // Housing, Lease, Rent
            q.contains("lease") || q.contains("house") || q.contains("housing") || q.contains("flat") || q.contains("rent") || q.contains("accommodation") || q.contains("room") -> {
                """
                🏠 **Student Housing & Lease Inspection Checklist**
                
                Before signing any tenancy or flat lease agreement:
                
                1. **Verify What Rent Covers:**
                   • Check whether utilities (water, electricity, gas, heating, high-speed Wi-Fi) are included in the monthly rent or billed separately.
                
                2. **Physical / Virtual Inspection:**
                   • Check for water pressure, radiator/heater performance, window seals, and mold in bathroom corners.
                   • Test power sockets and check mobile reception strength inside the apartment.
                
                3. **Scam Prevention:**
                   • **Never transfer a security deposit** before viewing the property or signing a written contract.
                   • Ensure security deposits are deposited into a legally protected government tenancy deposit scheme (e.g. DPS in the UK).
                
                4. **Council Tax Exemption (UK):**
                   • Full-time registered international students are exempt from council tax. Obtain your Council Tax Exemption letter from the student registry.
                """.trimIndent() to AssistantAction("Explore Nearby Accommodation", AssistantActionType.OPEN_ACCOMMODATION)
            }

            // Jobs & Internships
            q.contains("job") || q.contains("internship") || q.contains("career") || q.contains("resume") || q.contains("cv") || q.contains("assistantship") -> {
                """
                🎓 **Landing On-Campus Jobs & Student Roles**
                
                On-campus positions are competitive, so starting early is critical:
                
                1. **Target Departments:**
                   • University Libraries (desk assistant, cataloging)
                   • Campus Recreation & Sports Centers
                   • IT Helpdesk & Computer Labs
                   • Campus Cafes & Dining Halls
                   • Teaching Assistant (TA) or Research Assistant (RA) — approach professors directly after excelling in class.
                
                2. **Format Your CV to Local Standards:**
                   • US/UK student resumes should be strictly 1 page with no photo, date of birth, or marital status.
                   • Highlight coursework, technical projects, and bilingual skills.
                
                3. **Apply Early:**
                   • Positions usually open 2 to 4 weeks before the semester commences on the university student employment portal (Handshake, CareerHub, etc.).
                """.trimIndent() to AssistantAction("View Student Checklist", AssistantActionType.OPEN_CHECKLIST)
            }

            // Health Insurance & Doctors
            q.contains("health") || q.contains("insurance") || q.contains("doctor") || q.contains("clinic") || q.contains("hospital") || q.contains("medical") -> {
                """
                🏥 **Health Insurance & Medical Services Abroad**
                
                1. **Registration is Crucial:**
                   • In the UK: Register with a local **NHS GP (General Practitioner)** surgery near your student residence within your first two weeks.
                   • In the US/Canada: Understand your university's mandatory health insurance plan (deductible, co-pay, in-network vs. out-of-network clinics).
                
                2. **Prescription Medications:**
                   • Bring a 30–90 day supply of essential medications in their original packaging, accompanied by an official doctor's prescription in English with the generic chemical names.
                
                3. **Emergency Medical Numbers:**
                   • US/Canada: **911**
                   • UK: **999** (Emergency) or **111** (Non-emergency NHS advice)
                   • Europe: **112**
                """.trimIndent() to null
            }

            // Packing & Luggage
            q.contains("pack") || q.contains("luggage") || q.contains("bag") || q.contains("bring") || q.contains("flight") -> {
                """
                ✈️ **Pre-Departure Packing Essentials**
                
                1. **Carry-On Backpack (Never Check These In!):**
                   • Passport with Student Visa
                   • University Acceptance Letter (CAS / I-20)
                   • Financial proof & sponsor letters
                   • Multi-currency Forex card & emergency cash
                   • Laptop, chargers, power bank, universal travel adapter
                   • Essential medicines for 1 week
                
                2. **Checked Baggage:**
                   • Weather-appropriate layers: Thermal innerwear, warm waterproof jacket, sturdy walking boots.
                   • Basic starter utensil kit or spices you love from home.
                   • Important original academic degree certificates & transcripts.
                
                💡 Mark your packing tasks off as you pack in the PEERS Student Checklist!
                """.trimIndent() to AssistantAction("Open Student Checklist", AssistantActionType.OPEN_CHECKLIST)
            }

            // Budgeting & Expenses
            q.contains("budget") || q.contains("cost") || q.contains("expense") || q.contains("save money") -> {
                """
                📊 **Smart International Student Budgeting**
                
                Average monthly breakdown (excluding university tuition):
                
                • **Accommodation:** 45% - 60% of budget
                • **Groceries & Cooking:** 15% - 20% (cooking with flatmates saves hundreds each month!)
                • **Public Transit:** 8% - 12% (apply for student discount transit passes like 18+ Student Oyster in London or Ventra U-Pass in Chicago)
                • **SIM & Mobile Plan:** $15–$30/month (look for student SIM deals from VOXI, Giffgaff, Mint Mobile)
                • **Recreation & Discretionary:** 10%
                
                💡 Use the PEERS Budget Planner to log daily expenses and compare estimated vs actual costs!
                """.trimIndent() to AssistantAction("Open Budget Planner", AssistantActionType.OPEN_BUDGET)
            }

            // Making friends & Campus life
            q.contains("friend") || q.contains("lonely") || q.contains("homesick") || q.contains("community") || q.contains("meet") || q.contains("culture") -> {
                """
                🤝 **Connecting & Adjusting to Campus Life**
                
                Adjusting to a new country can feel overwhelming in the first few weeks, but you are not alone:
                
                1. **Attend Orientation & Freshers Week:**
                   • Go to department welcome mixers, campus society fairs, and international student events.
                
                2. **Join Student Societies:**
                   • Join clubs based on your passions (cultural societies, sports clubs, coding hackathons, hiking groups).
                
                3. **Use the PEERS Community:**
                   • Tap the **Community** tab right here in PEERS to discover students from your home country, course mates, and nearby campus peers!
                """.trimIndent() to AssistantAction("Browse Community Peers", AssistantActionType.OPEN_COMMUNITY)
            }

            // Default fallback
            else -> {
                """
                👋 **PEERS Study Abroad AI Guide**
                
                I'm here to help you navigate studying abroad! Here are some key topics I can provide immediate, detailed guidance on:
                
                • 🛂 **Visa Rules & Work Permits:** 20-hour work week limits, CPT/OPT, and post-study work visas.
                • 💰 **Banking & Forex:** Multi-currency cards, zero-fee student accounts, and tuition wire transfers.
                • 🏠 **Accommodation & Leases:** Dorms, off-campus flats, tenancy agreements, and deposit safety.
                • 🎓 **On-Campus Jobs:** Finding student assistant roles, tailoring your CV, and interview tips.
                • 🏥 **Healthcare & Safety:** Health insurance, registering with local doctors, and emergency contacts.
                • ✈️ **Pre-Departure Checklist:** Document verification and essential packing tips.
                
                Feel free to ask a question or tap one of the suggestion chips above!
                """.trimIndent() to null
            }
        }
    }
}
