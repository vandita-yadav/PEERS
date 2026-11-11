# PEERS — Documentation Updates

*This section contains additions and revisions to the main Project Documentation. It is organized so each part can be pasted directly into the corresponding place in the document.*

---

## PART 1 — SIGNUP / LOGIN PAGE (UPDATED: GOOGLE OAUTH)

> **Change note:** The previously planned phone-number + password signup and the OTP verification flow have been **removed** from the current scope. PEERS now uses **Google OAuth** as the single authentication method. Phone/email OTP may be revisited in the future as a fallback, but it will not be a launch feature.

### a. Splash Screen

- Displays the PEERS logo when the application is launched.
- Automatically redirects the user to the Get Started / Onboarding screen after a short duration.

(No change from the existing documentation — retained as-is.)

### b. Get Started / Onboarding Screen

(No change from the existing documentation.)

- Displays "Get Started" at the top with a horizontally scrollable feature carousel (4–6 slides, page indicator, auto-scroll).
- Fixed bottom section contains a **Sign Up** button and a **Log In** button.

### c. Sign Up — Google OAuth (Updated)

The Sign Up page is replaced by a simplified Google Sign-In based registration flow:

- Displays the PEERS logo/name with a short tagline at the top.
- A short explanation text will be displayed:
  *"Sign in with your Google account to join PEERS. We use your account only to verify that you are a real student and to keep your sign-in simple — no passwords to remember."*
- A single primary button is provided:
  - **Continue with Google** — official Google branding (the multi-colour "G" logo, white background, outlined button).
- A short privacy statement will be displayed below the button:
  *"PEERS will only read your name, email address, and profile photo from your Google account. PEERS will never post or share anything to your Google account."*
- No form fields are present on this screen. All profile details are collected manually in the **Profile Onboarding flow** immediately after a successful first-time sign-in.

### Behaviour After Google Sign-Up

1. The user taps **Continue with Google**.
2. The device's native Google account chooser is displayed (for accounts already signed in on the device), or a Google Sign-In web sheet is shown.
3. If this Google account has **never** been used with PEERS (new user):
   - The account is registered.
   - The user is redirected to the **Student Profile Setup / Onboarding** flow (Step 1 – Basic Information).
   - The **Full Name**, **Email Address**, and **Profile Photo** are prefilled from the Google account.
   - The user reviews and corrects the prefilled values and manually enters all remaining details (university, course, etc.).
   - No OTP verification is required.
4. If this Google account is **already registered** (returning user):
   - A toast/snackbar is displayed: *"Welcome back! You already have a PEERS account."*
   - The user is redirected directly to the **Dashboard**.
5. If the user cancels the Google account chooser, the app silently returns to the Get Started screen — no error is shown and no state changes.
6. If the Google Sign-In fails (network error, provider issue), a snackbar is displayed:
   *"Sign-in failed. Please check your internet connection and try again."*

### d. Log In — Google OAuth (Updated)

The Log In page is also simplified to the Google authentication flow:

- Displays **"Welcome Back"** at the top.
- A short supporting line: *"Use the Google account you signed up with."*
- The same single **Continue with Google** button (identical branding and behaviour as Sign Up).
- Because Google OAuth identifies the user automatically, there are:
  - **No** email/phone entry fields.
  - **No** password field.
  - **No** "Forgot Password?" option required (a password no longer exists in the flow).
- The final statement is: **"New to PEERS? Sign Up"**
  - Both buttons lead to the same Google Sign-In sheet for their own Google account; the app distinguishes new vs. returning users automatically by whether the account already exists in Firestore.

### Incorrect credentials / failure handling (Updated)

- With OAuth, "incorrect credentials" cannot occur on the user's side. The only failure states are:
  - **Account chooser cancelled** → silent return to Get Started.
  - **Network/provider error** → error snackbar (see above).
  - **Account suspended/blocked by moderation** (future scope) → alert dialog: *"Your account has been restricted. Contact support@peersapp.com"* with **Contact Support** and **Close** buttons.

### e. OTP Verification — REMOVED

- The 6-digit SMS OTP flow, email OTP fallback, 60-second resend countdown, and 15-minute OTP validity are removed from scope.
- Firebase-phone-number authentication code may remain in the repository for possible future use, but it is no longer part of the signup/login documentation.
- After successful Google authentication, the user goes straight to **Profile Onboarding** (new users) or the **Dashboard** (returning users).

### Updated Auth Flow Summary

| Step | Screen | New user outcome | Returning user outcome |
|---|---|---|---|
| 1 | Splash | — | — |
| 2 | Get Started / Onboarding | — | — |
| 3 | Google Sign-In (Sign Up or Log In button) | Account registered → Profile Onboarding Step 1 | Direct to Dashboard |

---

## PART 2 — PROFILE SETUP PAGE (UPDATE: 13 PREDEFINED AVATARS)

### 2nd Slider – Profile Picture Input (Updated)

The profile picture options remain as documented, with the avatar option now specified in detail:

- The user can:
  - **Take Photo** using the device camera
  - **Upload/Browse from gallery**
  - **Choose an Avatar** from **13 predefined PEERS avatars**

### Avatar Picker Screen/Section

- Selecting **Choose an Avatar** opens a dedicated section within Step 2 (or a modal bottom sheet over Step 2).
- A **3-column grid** displays all 13 predefined PEERS avatars.
- Each avatar is drawn in the same flat, white-line-figure style as the PEERS logo concept (dots forming a "P" with people-in-windows clipart), so the visual identity stays uniform across the app.
- Each avatar sits inside a circular background that alternates between the PEERS brand palette colours (PeersIndigo `#9FA1FF`, PeersLavender `#B5BAFF`, PeersSky `#AEE2FF`, PeersMint `#D9F9DF`).
- The figure inside each avatar is drawn in white (`0xFFFFFFFF`) on the coloured circle, matching the logo style.
- Tapping an avatar selects it: the circle receives a highlighted border (PeersIndigo) and a checkmark badge.
- Tapping a selected avatar again deselects it (user may revert to camera/gallery instead).
- A caption is displayed below the grid: *"Pick the avatar you relate to — you can always change it later in Edit Profile."*

### The 13 Predefined Avatars

| # | Avatar | Description (figure in white on coloured circle) |
|---|---|---|
| 1 | African Man | Short hair, ears visible, simple smiling face |
| 2 | African Woman | Afro hairstyle, smiling face |
| 3 | North Indian Boy | Short dark hair with a small tuft, smiling face |
| 4 | Northeast Indian Girl | Shoulder-length hair with fringe, smiling face |
| 5 | South Indian Girl | Long black hair, small bindi dot on forehead |
| 6 | Sikh Man | Dark turban and beard outline |
| 7 | Muslim Woman | Hijab silhouette framing the face |
| 8 | Middle Eastern Man | Short beard, thick eyebrows |
| 9 | East Asian Woman | Straight fringe (bob cut), smiling face |
| 10 | East Asian Man | Side-parted straight hair, smiling face |
| 11 | Southeast Asian Woman | Long wavy hair, minor freckles |
| 12 | Latina Woman | Curly shoulder-length hair, smiling face |
| 13 | Neutral Student | Gender-neutral short hair with round glasses |

- Avatars must have **no gender stereotypes in clothing** beyond the identifying features listed above; faces are simple, friendly, and identical in style.
- Avatars are implemented as **vector drawables** (resolution-independent, no image assets) stored under `res/drawable/`.
- The state stores the selected avatar as an integer index (already represented in `ProfileSetupState` as `selectedAvatar: Int`); `null` means "no avatar chosen", in which case a personal photo or the default placeholder is used.
- Edit Profile later reuses the same avatar grid so users can switch avatars at any time.

---

## PART 3 — NOTIFICATIONS SCREEN

### Entry Point

- Tapped from the **notification / bell icon** in the common header.
- The red notification dot appears on the header icon whenever the user has one or more **unread** notifications; it disappears once every notification has been opened or dismissed via "Mark all as read".

### Screen Layout

1. **Top App Bar**
   - Back button (←) on the left, returning to the previous screen (usually the Dashboard).
   - Title **"Notifications"**.
   - A **"Mark all as read"** text button on the right (visible only when at least one unread notification exists).
2. **Notification List**
   - Vertically scrollable list (`LazyColumn`).
   - Notifications are grouped under sticky date headers: **Today**, **Yesterday**, and then specific dates (dd MMM yyyy).
   - Within each group, notifications are sorted newest-first.
3. **Notification Card (each item)**
   - Leading **type icon** inside a small tinted circle:
     - 👥 Connection request / connection accepted
     - 🏠 Roommate request / roommate request accepted
     - 📅 Event related (reminder, event created in your community)
     - 💬 Forum reply / upvote on your post or answer
     - ⭐ Review on an accommodation you contributed
     - 🏷️ System/moderation notices
   - **Sender avatar** (overlapping or beside the type icon) where a specific person triggered the notification.
   - **Title** (bold): a short summary, e.g. *"Aarav Sharma sent you a connection request."*
   - **Message body** (regular): supporting detail, e.g. *"Computer Science • LPU. You have 5 mutual connections."*
   - **Timestamp**: relative time ("2 min ago", "3 h ago", yesterday, date).
   - **Unread state**: unread cards use the `LightSurfaceVariant` background (or Lavender tint in light mode) plus a small dot on the right; read cards use the plain surface.
4. **Inline Actions (pending requests only)**
   - **Connection request** cards show **Accept** and **Decline** buttons directly on the card.
     - Accept → status changes to Accepted; a connection is created; the sender receives an "…accepted your request" notification; the card's buttons are replaced by the text *"✓ Connected"*.
     - Decline → status changes to Declined; the sender receives a "…declined your request" notification; the card's buttons are replaced by *"Declined"*.
   - **Roommate request** cards (e.g., *"[Name] has requested you to join as a roommate for [Accommodation Name]."*) show the same **Accept** / **Decline** controls.
     - On Accept, a dialog titled **"Here are ways you can connect"** is shown, containing only the contact information the sender chose to share (phone, Gmail, Instagram, Snapchat, permitted details) plus the sender's custom roommate message.
   - Tap-through: tapping a non-request notification navigates to its related context (Event Details, the forum post/discussion, the accommodation listing, or the profile preview bottom sheet).
5. **Empty State**
   - When no notifications exist, a friendly illustration with the text *"No notifications yet — when someone wants to connect with you or an event is coming up, you'll see it here."*

### Data Model (Firestore)

```text
notifications/{autoId}
  userId:        recipient user ID
  senderId:      acting user ID (nullable for system notices)
  type:          CONNECTION_REQUEST | CONNECTION_ACCEPTED | CONNECTION_DECLINED |
                 ROOMMATE_REQUEST | ROOMMATE_ACCEPTED | ROOMMATE_DECLINED |
                 EVENT | FORUM | REVIEW | SYSTEM
  title:         short bold summary
  message:       supporting body text
  relatedType:   user | post | event | accommodation
  relatedId:     ID of the related document
  read:          Boolean (false = unread)
  createdAt:     Timestamp
```

- Notification writes are performed by the same operations that change request states (connect, accept/decline, roommate request acceptance, forum replies, etc.), so no separate worker is required initially.
- **Firebase Cloud Messaging (FCM)** can be added later to deliver real push notifications when the app is closed; the in-app notification list remains the single source of truth either way.

---

## PART 4 — EDIT PROFILE SCREEN

### Entry Point

- Side Navigation Menu → **Profile → Edit Profile**.
- Also accessible from a small **"Edit"** pencil icon on the user's own Profile tab (bottom navigation "Profile").

### Purpose

- Allows the user to **view and modify** the profile information originally entered during Profile Onboarding, including details that are no longer part of the simplified Google sign-up (for example, the previously planned signup fields now live here/onboarding only).
- The screen is effectively the same four onboarding steps, presented as editable sections, so the user never has to re-learn a different UI.

### Screen Layout

1. **Top App Bar**
   - Back button (←), title **"Edit Profile"**, and a **Save / Update** text button on the right (enabled only when at least one change exists).
2. **Profile Picture Section**
   - Shows the current profile photo or avatar in a large circle.
   - **Edit** options below it (identical to onboarding Step 2): Take Photo / Upload from Gallery / Choose Avatar (opens the same avatar grid with all 13 predefined avatars).
3. **Basic Information Section** (card)
   - Rows for: Country of Origin, Current Country, Current City, University/College, Course/Program, Degree Level, Year of Study, Expected Graduation Year, Bio/About Me, Languages Known, and **Gender** (selectable options including *Prefer not to say*).
   - Tapping a row opens the same picker/dropdown used during onboarding; Bio is a multiline editor.
4. **Interests Section** (card)
   - Displays the user's selected interest chips.
   - An **Edit** button opens the same interest card grid from onboarding Step 3; at least 3 interests must remain selected to save.
5. **Needs Section** (card)
   - Same pattern for the "What do you need from PEERS?" selections; at least 2 selections must remain to save.
6. **Save Behaviour**
   - **Save/Update** writes all changed fields to the user's profile in Firestore and shows a snackbar: *"Profile updated."*
   - If the user leaves with unsaved changes (back button/gesture), a confirmation dialog appears:
     - *"Discard changes?"* — **Discard** (exit without saving) / **Keep Editing**.
7. **Rules**
   - Optional fields may still be left empty; required fields (country, university, course, degree, year) cannot be cleared.
   - After this flow, users never see onboarding again — Edit Profile is the only way to change these details.

---

## PART 5 — SETTINGS SCREEN

### Entry Point

- Side Navigation Menu → **Application → Settings**.
- Optionally a gear icon on the user's own Profile tab.

### Screen Layout

1. **Top App Bar** — back button (←) and title **"Settings"**.
2. **Appearance Section** (card)
   - **Theme Mode** — three radio options:
     - ☀️ **Light** — soft warm/off-white background (`0xFFFAF8F4`)
     - 🌙 **Dark** — soft grayish dark background (`0xFF1E1F24`)
     - ⚙️ **System Default** — follows the device setting
   - The choice is **persisted locally** (DataStore/SharedPreferences) and applied instantly; all UI components adapt without a restart.
   - A small preview strip inside the card shows how cards/buttons look in the chosen mode.
3. **Notifications Section** (card)
   - Master toggle: **In-app notifications** (on/off).
   - Individual toggles: Connection requests, Roommate requests, Events, Forum replies & upvotes, Reviews on my contributions.
4. **Account Section** (card)
   - Shows the signed-in Google account: profile photo, full name, email address (read-only).
   - **Sign out** button — see the Sign Out confirmation dialog spec in this document.
   - **Delete Account** (optional/future scope): warning dialog before permanently deleting the account and profile data.
5. **Privacy & Permissions Section** (card)
   - Status rows for Camera, Location, and Storage permissions, each opening the device's app-permission settings.
   - Snippet of the profile-photo privacy statement from onboarding.
6. **Support Section** (card)
   - **Rate us on Google Play** — same behaviour as the side-menu "Love the app? Rate us" item.
   - **About PEERS** — navigates to the About screen.
   - **Contact support** — opens the mail client at `support@peersapp.com` (address placeholder).
7. **Version Row** (footer)
   - App name, version number and build: e.g. **PEERS 1.0 (1)**.

---

## PART 6 — SIDE MENU: "LOVE THE APP? RATE US" (FORMAL SPEC)

- Hyperlink/button text: **"❤️ Love the app? Rate us"** (bottom portion of the side navigation drawer, above Sign Out).
- Tapping it attempts to open the PEERS listing directly in the **Google Play Store app** (`market://details?id=com.example.peers`).
- If the Play Store app is not available on the device, it falls back to opening the equivalent **Play Store web URL** in the browser (`https://play.google.com/store/apps/details?id=com.example.peers`).
- If neither is possible, a snackbar is shown: *"Couldn't open the Play Store. Please try again later."*
- No intermediate dialog is used — the redirect happens directly, as specified in the main documentation.

---

## PART 7 — INVITE A FRIEND (BOTTOM SHEET — FORMAL SPEC)

### Trigger

- Side Navigation Menu → **Community → Invite a Friend**.
- The side navigation drawer closes first, then a **modal bottom sheet** slides up over the Dashboard.

### Bottom Sheet Layout

1. **Header**
   - Title: **"Invite Friends"**
   - A drag handle at the top center; swiping down or tapping the dimmed background dismisses the sheet.
2. **Description**
   - Short text: *"PEERS is better with your people. Invite classmates and friends who are studying abroad or planning to."*
3. **Invite Link Block**
   - A highlighted (PeersLavender tinted) box containing the app invite/download link and a **Copy Link** icon.
4. **Primary Action**
   - **Share via WhatsApp** button (green WhatsApp branding) — most common first choice.
5. **Secondary Actions Row**
   - Small circular icons for **Instagram**, **Snapchat**, **Messages**, and **More…**
6. **Copy Link** — copies the invite link to the device clipboard and shows a snackbar: *"Link copied."*

### Share Message

- PEERS generates a predefined invitation message:

  *"Check out PEERS — the student community app for people, communities & opportunities that matter to you! 🎒 Download: [Play Store Link]"*

- Selecting any sharing target launches the **system's native sharing mechanism** (Android `Intent`/share sheet) with this message, matching the behaviour described in the main documentation.
- The recipient who already has PEERS installed deep-links into the app; anyone without it lands on the Play Store listing.

---

## PART 8 — SIGN OUT / LOGOUT CONFIRMATION DIALOG (FORMAL SPEC)

### Trigger

- Side Navigation Menu → **Sign Out / Logout** (bottom portion of the drawer).

### Dialog

- Title: **"Sign out of PEERS?"**
- Body: *"You will need to sign in again with your Google account to use PEERS."*
- Buttons:
  - **Cancel** — closes the dialog and keeps the user signed in.
  - **Sign Out** (destructive/red) — confirms the action.

### Behaviour After Confirmation

1. The drawer and dialog close.
2. The session is cleared (Firebase Auth sign-out + local cached profile/UI state).
3. The user is returned to the **Get Started / Onboarding** screen.
4. A snackbar confirms: *"Signed out."*
5. The user re-enters the app through the normal Google Sign-In flow; their account and profile data remain intact.

---

## PART 9 — REMAINING FEATURE PAGES (PLANNED — TO BE DETAILED)

These sections are acknowledged in the documentation but their screen-by-screen layouts will be written in the next documentation pass:

1. **Discussion Forum & Communities** — posts, comments, upvotes, university/city communities, moderation and reporting.
2. **Events & Meetups** — Events page (already linked from the dashboard's "View All Events"), event details, event creation by students/universities/communities, attendee lists.
3. **Personalized Checklist** — pre-departure and arrival checklist generator based on destination/university/course/intake.
4. **AI-Powered Student Assistant** — chatbot screen (currently a side-menu item under Help & Support).
5. **Student Jobs, Internships & Scholarships** — centralized opportunity discovery page.
6. **Budget & Cost-of-Living Planner** — monthly expense estimation and tracking.
7. **Help & Emergency Support** — the structured "Need Help?" flow and official emergency contacts.
8. **Language & Cultural Assistance** — translation, local phrases, cultural tips.
9. **View Similar Profiles page** — the full recommendation page behind the dashboard's "View Similar Profiles →" button (visual form of the documented recommendation algorithm).

### Structure Note

When implemented, the new features follow the existing package convention:

```text
features/
├── notifications/   (NotificationsScreen.kt, NotificationsViewModel.kt, components/)
├── settings/        (SettingsScreen.kt, EditProfileScreen.kt)
├── ...              (forum, events, checklist, chatbot, jobs, budget, help)
```
