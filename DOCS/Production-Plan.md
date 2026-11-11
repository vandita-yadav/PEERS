# PEERS — Production Plan

> Roadmap for building the PEERS Android app per the Project Documentation, mapped to the
> CSE226 (Android App Deployment) syllabus. Free-tier services only.
> Stack: Kotlin · Jetpack Compose · Material 3 · MVVM · Firebase (Auth/Firestore) · Room ·
> Google Maps Compose · Gemini API (free tier) · Git/GitHub.

---

## Guiding rules

- We do **not** trim features. We build feature-set by feature-set, finishing each before
  moving to the next (unless we hit roadblocks or interconnected dependencies).
- Every feature lands through a **feature branch + PR on GitHub** — this doubles as the
  syllabus evidence for version control (branching, merging, PRs, conflict resolution).
- Free tiers only: **Firebase Spark**, **Google Maps free tier**, **Gemini API free tier**,
  **GitHub free**. No paid services anywhere.
- UI follows the documented design system exactly (brand palette, light/dark theme colors,
  logo concept, `features/<name>/` package convention).

---

## Phase 0 — Foundation & Setup

### Repo / Git workflow
1. Commit current work to `main`, push to GitHub (`origin` → github.com/vandita-yadav/PEERS).
2. Branch model: `main` → `feature/<name>` → PR → merge. Never commit to `main` directly.
3. Secrets hygiene: never commit `google-services.json` or API keys. Maps key lives in
   `local.properties` (already gitignored), surfaced via manifest placeholder.

### Linux migration (Windows → CachyOS)
- `local.properties` → `sdk.dir=/home/<user>/Android/Sdk` (gitignored, machine-specific).
- Build with Android Studio's bundled JBR (`/opt/android-studio/jbr`) or a local JDK 17+.
- `.idea` files are committed but harmless cross-platform; `.gitignore` already excludes
  caches/workspace. CRLF→LF churn on `.bat` files can be ignored.

### Firebase project (Spark, free)
1. Create project → add Android app `com.example.peers` → download `google-services.json`
   into `app/` (do NOT commit).
2. Enable: **Auth (Google provider)**, **Firestore**; later: **Storage** (images), **FCM**.
3. Gradle: Google services plugin + `firebase-auth` / `firebase-firestore` via Firebase BoM.
4. Spark free tier (50k reads/20k writes per day, 1 GiB storage) is far above our needs.

### Google Maps API key (free)
1. Google Cloud project → enable **Maps SDK for Android** (+ **Geocoding API**).
2. Key in `local.properties` → manifest placeholder. Free tier ≈ 10k map loads/month.

### Remove OTP legacy
- Delete phone/OTP auth code (`OtpVerificationScreen`, phone-based `AuthViewModel` logic).
- Google OAuth is the final, single auth method.

---

## Phase 1 — Auth & Onboarding
**Syllabus: CO1 (Compose UI), CO2 (Coroutines), CO3 (MVVM)**

- Splash → Onboarding carousel: LazyRow, auto-scroll via `LaunchedEffect`, page indicator,
  Compose animations.
- **Google Sign-In** (Credential Manager): new user → Profile Onboarding; returning →
  Dashboard; chooser cancelled → silent return; failure → snackbar. Profiles in Firestore.
- **4-step Profile Onboarding** (progress bar, back/continue, validation):
  1. Basic Information (countries, university, course, degree, year, bio, languages)
  2. Profile Picture (camera / gallery / 13 vector avatars, manual circular crop)
  3. Interests (categorized card grid, min 3)
  4. Needs ("What do you need from PEERS?", min 2) → Complete Profile

*Viva talking points:* Compose animations, side effects, StateFlow in
`ProfileSetupViewModel`, state hoisting in step components, `SavedStateHandle` for step
restoration, structured concurrency for sign-in.

---

## Phase 2 — Dashboard & Navigation
**Syllabus: CO1, CO3**

- Common reusable `Header` composable (hamburger, greeting, notification bell).
- Side drawer: overlay behavior, dim scrim, profile section, menu groups, rate-us, sign-out.
- Dashboard:
  - Time-based greeting + university name
  - Date calendar slider (LazyRow, event indicator dots, selection filters content)
  - **Top Events** (LazyRow cards, View All, empty state card)
  - **People You May Know** — rule-based weighted scoring:
    same university +30 · same course +20 · same year +10 · same city +10 ·
    +5 per common interest · +5 per common need · +15 per mutual connection.
    Card → ModalBottomSheet profile preview → Connect → Request Sent → Connected.
    Duplicate-request prevention, in-app notifications for sender/recipient.
  - **Discover What Interests You** — 2-column grid routing to feature pages.
  - Bottom navigation (Home, Community, Events, Profile) with selected-state styling.
- **Notifications screen** (Part 3 spec): grouped LazyColumn with sticky date headers,
  type icons, unread styling, inline Accept/Decline, mark-all-read, empty state.
  In-app only initially (Firestore); FCM later if needed.

---

## Phase 3 — Nearby Accommodation
**Syllabus: CO4 (Room), CO5 (Location/Maps) — flagship for both**

- Listings page (cards: image, name, type, distance, wishlist icon, View Details),
  search + filters (rent, distance, type, gender, furnishing, amenities, availability).
- Add Accommodation form: details, multiple images, location via search or Google Maps
  picker (store lat/lng + formatted address), contributor recording, pending → active status.
- Details screen: image carousel, persistent bottom sheet, share/deep-link message,
  wishlist sync, "open in Google Maps", Contact/Enquiry (no booking/payments).
- **Google Maps Compose:** map view of listings, markers, customization.
  **Fused Location Provider:** user location, distance calculation, runtime permissions.
- Community verification: "Mark as Genuine", one per student, threshold → badge, removable.
- Reviews dialog: 1–5 stars, one per student, report reviews.
- Roommate matching: Looking for a Roommate (contact-sharing permissions + custom message),
  Find a Roommate (list seekers), Roommate Request → Pending/Accepted/Declined →
  "Here are ways you can connect" dialog.

---

## Phase 4 — Room / Offline-First
**Syllabus: CO4**

- **Budget & Cost-of-Living Planner:** full Room CRUD (entities, DAOs, relationships,
  type converters, migrations), Flow-based queries, monthly/estimated-vs-actual tracking.
  Local-only = clean offline-first demo.
- **Offline-first cache:** wishlist, saved events, notifications in Room; Firestore as
  remote source of truth; read offline → hydrate → sync on reconnect.
  Explains "Room with Flow + Data Synchronization / Offline-First" directly.

---

## Phase 5 — Forum, Communities & Events
**Syllabus: CO1, CO2**

- Discussion forum: posts, comments, upvotes, university/city communities, reporting.
  `SharedFlow` for one-off UI events (toasts/errors) — CO2 talking point.
- Events: browse, details, create (student/university/community), attendee lists;
  integrated with dashboard date slider.

---

## Phase 6 — Remaining Features

- **Personalized Checklist** — rule-based pre-departure/arrival generator by
  destination/course/intake; stored in Room (offline-first).
- **AI-Powered Student Assistant** *(required)* — Gemini API **free tier**
  (AI Studio key, per-minute/day quotas). Chat UI, streaming responses, prompts grounded
  in study-abroad guidance. Graceful quota-exceeded fallback message.
- **Jobs, Internships & Scholarships** — Firestore-backed discovery page with filters.
- **Help & Emergency Support** — structured "Need Help?" flow, official emergency contacts.
- **Language & Cultural Assistance** — phrases/tips content + translation via Gemini free tier.
- **View Similar Profiles** page — full recommendation list behind "View Similar Profiles →".
- **Edit Profile** — same 4 onboarding steps as editable sections, discard-changes dialog.
- **Settings** — theme (Light/Dark/System persisted via DataStore, using the exact
  documented color values), notification toggles, account section, permissions, support.
- **Invite a Friend** bottom sheet, **Rate Us** (Play Store intent with web fallback),
  **Sign Out** confirmation flow, **About PEERS**.

---

## Phase 7 — Quality & Deployment
**Syllabus: CO6**

- **JUnit unit tests:** recommendation scoring (pure function — ideal test target),
  budget calculations, ViewModel state transitions, checklist generator.
- **Compose UI tests:** onboarding flow, dashboard rendering, notification actions.
- **Debugging:** Logcat walkthrough, Android Studio debugger, exception/crash analysis
  (documented for viva).
- **Git evidence:** PR history, tags, one deliberate conflict-resolution exercise documented.
- **Deployment:** signed release APK/AAB; Play Console publishing ($25, decide later) or
  distribute signed APK directly.

---

## Syllabus coverage map (for viva)

| Syllabus unit | Where in PEERS |
|---|---|
| I — Compose M3 / animations | Onboarding, dashboard, bottom sheets, dialogs, drawer |
| II — Coroutines / Flow | Auth flow, Firestore streams, StateFlow/SharedFlow, notifications |
| III — MVVM / Architecture | Every feature (ViewModel + Repository + single source of truth) |
| IV — Room / offline-first | Budget planner, wishlist/notifications cache, checklist, sync |
| V — Location / Maps | Accommodation map, geocoding, distances, Fused Location |
| VI — Git / testing / debug | Branch+PR workflow, JUnit + Compose tests, docs |

~30% beyond syllabus (all explainable): Firebase Auth OAuth, Firestore data modeling,
notifications system, weighted recommendation engine, deep linking, Gemini AI assistant.
