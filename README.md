# PEERS — Student Campus & Community Companion App

**PEERS** (*Peers Empower Each Other, Rise Together*) is an all-in-one Android application built with **Kotlin** and **Jetpack Compose (Material 3)** for university and college students. It unifies campus onboarding, accommodation discovery, transport & item exchange, college-independent discussion forums, event discovery & bookmarking, personalized arrival checklists, monthly budget planning, emergency helplines, and a conversational Gemini-powered AI Student Guide in one cohesive offline-first application.

---

## Key Features

### 1. Onboarding, Splash & Authentication
- **Custom Splash & Auto-Scrolling Carousel:** Branded mascot splash screen (1.5s) transitioning into an illustration carousel with adaptive Light & Dark theme switcher and edge-to-edge system bar insets.
- **Dual Authentication:** Supports **Google OAuth** (via Android Credential Manager + Google ID Services + Firebase Auth) as well as **Manual Email/Password Sign-Up & Log-In** with real-time field validation (domain format checks and password strength rules).
- **Duplicate & Missing Account Safeguards:** Distinguishes sign-up vs. sign-in flows cleanly across Google and manual providers.

### 2. Progressive Profile Onboarding & Academic Calculator
- **4-Step Onboarding Wizard & Edit Profile:**
  - **Cascading Location Pickers:** `Country -> State -> City -> University/College`, scoping universities to the chosen country and prioritizing institutions in the selected city.
  - **Degree & Graduation Calculator:** Maps degree types (`B.Tech`, `B.E.`, `B.A. LL.B`, `B.Sc`, `BBA`, `B.Com`, `B.Arch`, `MBBS`, `MBA`, `MCA`, etc.) to course duration and derives Expected Graduation Year and Current Year of Study from Commencing Month & Year.
  - **International Phone Code Picker** + **Domain/Specialization** selection.
  - **Flexible Profile Photo:** Camera capture (with runtime permission prompt and EXIF orientation handling), gallery picker with built-in cropper, or **13 custom PEERS vector avatars** synced consistently across Header, Navigation Drawer, Profile, and Settings.

### 3. Dashboard & Unified Sub-Screen Navigation
- **Personalized Home Dashboard:** Displays time-aware greeting, user profile & university, compact mini-calendar strip with event indicators, and elevated quick-access cards (*Discover What Interests You*).
- **Consistent Navigation Architecture:** Navigation drawer (`ModalNavigationDrawer`), 4-tab bottom navigation (`Home`, `Discussion`, `Events`, `Settings`), unified sticky `PeersSubScreenHeader` on all sub-screens, and double-back exit guard.

### 4. Accommodation Discovery & Roommate Support
- **Verified Stays Near Campus:** Curated PGs, studio flats, and hostels around campus (e.g., Phagwara, Law Gate, Meheru, Chaheru, LPU area) plus Firestore/local user-added listings (`+ Add Listing`).
- **Interactive Filter Bottom Sheet:** Filter by max monthly rent slider, distance from campus, property type, gender preference, furnishing, immediate availability, and amenities with an explicit **Save Filters** action.
- **Stay Details, Reviews & Roommate Matching:** Photo carousel, campus distance, Google Maps integration, peer verification count, student reviews with Edit/Delete support for your own reviews, wishlist bookmarking (Room + Firestore offline-first sync), and roommate finder requests.

### 5. Campus & City Events with Saved Events
- **Interactive 14-Day Calendar Ribbon & Search:** Filter by date, organizer category (`University`, `Student Clubs`, `Community`), search query, or **Saved** bookmarks.
- **Create Events & Real Campus Venues:** Quick-select genuine LPU venues (*Shanti Devi Mittal Auditorium (Block 35)*, *Baldev Raj Mittal UniPolis*, *Amphitheatre*, *Indoor Sports Complex*, *Unimall*, *Block 13 DSW*, Academic Blocks *25–38*) when publishing events.
- **RSVP & Notifications:** One-tap RSVP triggers Android system notifications and updates attendee lists in real time; Saved Events stay synced via Room + Firestore (`Profile -> Saved Events` jumps directly to bookmarked events).

### 6. College-Independent Discussion Forums
- **University-Scoped Topics:** Forums are isolated per enrolled university and organized by topic tabs (*General*, *Events*, *Placements*, *PG & Rooms*, *Academics*, *Campus Life*).
- **Real-Time Threads, Replies & Moderation:** Supports creating discussions, upvoting, commenting without hiding pre-existing peer replies, Edit/Delete for your own posts & comments, and moderation reporting.

### 7. Rentals, Student Exchange & Lost & Found
- **Unified Marketplace Categories:** *All*, *Bike & Scooty*, *Cycles*, *Cab & Shuttle*, *Student Exchange* (gadgets, appliances, textbooks, formal wear), and *Lost & Found*.
- **Post Listings & Optional Location for Lost & Found:** Students can publish new items via `+ Add Listing` (allowing location to be left blank for lost/found items anywhere), inspect poster info and item details in `RentalDetailView`, and contact posters via one-tap Dial, Copy, or Email actions.

### 8. Student Productivity, Safety, AI Tools & Admin Console
- **PEERS AI Guide:** Gemini-powered conversational assistant (`generativelanguage.googleapis.com` REST with `gemini-2.5-flash` / `gemini-3.5-flash` fallback chain + complete campus directory knowledge base) offering deep-link action chips directly into relevant app screens.
- **Student Checklist:** Offline Room-backed arrival/visa/banking/academic checklist personalized to student metadata.
- **Monthly Budget & Cost of Living Planner:** Offline Room-backed expense logger with monthly caps, category breakdowns, and spending summaries.
- **Helplines & Important Contacts:** Searchable directory of University Hospital, Women Safety & Anti-Ragging Cells, Fire/Security, DSW Counseling, Campus Hostels (`BH-1..7`, `BS-11..13`, `GH-1..7`, `GS-12`), and National Emergency helplines (`112`, `108`, `1091`, `1800-180-5522`) with one-tap Dial and Copy.
- **Role-Gated Admin Console (`AdminScreen`):** Dedicated administrative panel accessible exclusively to authorized admins (`van22pop576@gmail.com`, `tanyayadav2552005@gmail.com`, and dynamically added co-admins) for campus-wide alert broadcasts, moderation of reported discussions, accommodation/event/rental verification & cleanup, and user directory management.
- **Settings, In-App Permissions & Account Deletion:** Light/Dark/System theme modes, notification channel preferences, in-app runtime permission prompts (Camera, Location, Phone Notifications), Sign Out, and **Delete Account Permanently** (removing Firebase Auth account, Firestore document, and local caches).

---

## Tech Stack & Architecture

- **Language & UI:** Kotlin `2.2.10`, Jetpack Compose (`composeBom 2026.02.01`), Material 3, Google Sans & Lato typography.
- **Architecture:** Feature-modular **MVVM** using `ViewModel` / `AndroidViewModel`, Kotlin Coroutines, and reactive `StateFlow` / `SharedFlow`.
- **Local Persistence (Offline-First):** **Room Database `2.8.5`** (with KSP) for Budget expenses, Student Checklist items, Saved Events cache, Accommodations Wishlist, and In-App Notifications, paired with `SharedPreferences` fallbacks and `ConnectivityManager` background sync.
- **Backend & Auth:** **Firebase BoM `32.8.0`** (Firebase Authentication + Cloud Firestore real-time snapshot listeners & transactions) + AndroidX Credentials & Google ID (`1.3.0` / `1.1.1`).
- **AI & Maps:** Google Gemini REST API (free-tier Flash models via `BuildConfig.GEMINI_API_KEY`) & Google Play Services Maps (`19.0.0`).
- **Media & Images:** Coil (`2.7.0` + `coil-svg`), AndroidX ExifInterface (`1.3.7`), and `FileProvider` camera URI capture.
- **SDK Targets:** `minSdk = 26`, `targetSdk = 34`, `compileSdk = 37`, Java 11 compatibility.

---

## Project Structure

```text
app/src/main/java/com/example/peers/
├── MainActivity.kt                      # Auth-state root routing, splash flow & account deletion wiring
├── features/
│   ├── onboarding/                      # Carousel, Welcome, Manual SignUp/LogIn & Google Auth sheets
│   ├── auth/                            # AuthViewModel & Google Credential / Firebase Auth handlers
│   ├── profile/                         # 4-step Setup Wizard, cascading options, avatar/cropper & Edit Profile
│   ├── dashboard/                       # Home feed, PeersHeader/PeersSubScreenHeader, Drawer & Notifications
│   ├── accommodation/                   # Stay listings, FilterSheet, AccommodationDetails & AddListingScreen
│   ├── events/                          # Calendar ribbon, Saved Events filter, EventDetails & CreateEventSheet
│   ├── forum/                           # College-scoped discussion feed, replies & Edit/Delete/Report actions
│   ├── rentals/                         # Rentals, Cab Pools, Student Exchange, Lost & Found & Detail view
│   ├── contacts/                        # ImportantContactsScreen (Hospital, Hostels, Safety, National Helplines)
│   ├── assistant/                       # Gemini-backed PEERS AI Guide + deep-link action suggestions
│   ├── checklist/                       # Room-backed personalized Student Checklist
│   ├── budget/                          # Room-backed Budget & Cost of Living Planner
│   ├── offline/                         # Room OfflineCacheRepository (Wishlist, Notifications, Saved Events)
│   ├── notifications/                   # NotificationChannel & runtime permission helpers
│   └── settings/                        # Appearance, in-app permission dialogs, preferences & account deletion
└── ui/theme/                            # Adaptive light/dark ColorScheme, Typography & custom icon helpers
```

---

## Setup & Configuration (Security First)

API keys and OAuth client identifiers are **never hardcoded or committed to Git**. They are read at build time from `local.properties` inside `app/build.gradle.kts`:

1. Clone the repository and open it in **Android Studio**.
2. Add your local configuration to `local.properties` in the project root (ignored by `.gitignore`):
   ```properties
   sdk.dir=YOUR_ANDROID_SDK_PATH
   WEB_CLIENT_ID=YOUR_FIREBASE_WEB_CLIENT_ID
   MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
   GEMINI_API_KEY=YOUR_GEMINI_API_KEY
   ```
3. Place your Firebase project's `google-services.json` inside `app/` (`google-services.json` is listed in `.gitignore`).
4. Build and run on an emulator or physical Android device (`minSdk 26+`):
   ```bash
   ./gradlew assembleDebug
   ```
