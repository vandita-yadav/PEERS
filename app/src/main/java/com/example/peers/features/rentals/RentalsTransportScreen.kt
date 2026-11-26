package com.example.peers.features.rentals

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FindInPage
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PedalBike
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.peers.R
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.dashboard.components.PeersSubScreenHeader
import com.example.peers.features.offline.OfflineCacheRepository
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import com.example.peers.ui.theme.PeersWarmYellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

enum class RentalCategory(val label: String) {
    ALL("All"),
    BIKE_SCOOTY("Bike & Scooty"),
    CYCLES("Cycles"),
    CAB_SHUTTLE("Cab & Shuttle"),
    EXCHANGE("Student Exchange"),
    TICKETS("Tickets & Passes"),
    DIGITAL_ITEMS("Digital & Electronics"),
    LOST_AND_FOUND("Lost & Found")
}

data class RentalListingItem(
    val id: String,
    val title: String,
    val provider: String,
    val category: RentalCategory,
    val rateLabel: String,
    val pickupLocation: String,
    val details: String,
    val highlights: List<String>,
    val postedBy: String,
    val contactPhone: String = "",
    val contactEmail: String = "",
    val contactNote: String = "",
    val photoUris: List<String> = emptyList(),
    val authorUid: String = "",
    val interestedCount: Int = 0,
)

private fun categoryIcon(cat: RentalCategory): ImageVector = when (cat) {
    RentalCategory.BIKE_SCOOTY -> Icons.Rounded.DirectionsBike
    RentalCategory.CYCLES -> Icons.Rounded.PedalBike
    RentalCategory.CAB_SHUTTLE -> Icons.Rounded.LocalTaxi
    RentalCategory.EXCHANGE -> Icons.Rounded.SwapHoriz
    RentalCategory.TICKETS -> Icons.Rounded.ConfirmationNumber
    RentalCategory.DIGITAL_ITEMS -> Icons.Rounded.Devices
    RentalCategory.LOST_AND_FOUND -> Icons.Rounded.FindInPage
    RentalCategory.ALL -> Icons.Rounded.DirectionsBus
}

private fun categoryAccent(cat: RentalCategory): Color = when (cat) {
    RentalCategory.BIKE_SCOOTY -> PeersWarmYellow
    RentalCategory.CYCLES -> PeersMint
    RentalCategory.CAB_SHUTTLE -> PeersSky
    RentalCategory.EXCHANGE -> PeersLavender
    RentalCategory.TICKETS -> Color(0xFFFFE0CC)
    RentalCategory.DIGITAL_ITEMS -> Color(0xFFCFF3FF)
    RentalCategory.LOST_AND_FOUND -> Color(0xFFFFD9E2)
    RentalCategory.ALL -> PeersPrimaryIndigo
}

private val seededRentalListings = listOf(
    RentalListingItem(
        id = "r1",
        title = "Honda Activa 6G / Scooty Rental",
        provider = "Campus Wheels Co.",
        category = RentalCategory.BIKE_SCOOTY,
        rateLabel = "₹250/day · ₹2,400/mo",
        pickupLocation = "Main Gate & Law Gate Counter",
        details = "Ideal for daily grocery runs and off-campus commute. Helmet and RC papers included; valid DL + Student ID required.",
        highlights = listOf("1 Helmet Free", "Zero Hidden Deposit", "Student Discount"),
        postedBy = "Harpreet Singh (Campus Wheels)",
        contactPhone = "+91 98765 41200",
        contactEmail = "campuswheels.phagwara@example.com",
        contactNote = "Available 8 AM – 9 PM near Law Gate entrance.",
        interestedCount = 14,
    ),
    RentalListingItem(
        id = "r2",
        title = "Geared City & Hybrid Cycles",
        provider = "GreenCampus Pedals",
        category = RentalCategory.CYCLES,
        rateLabel = "₹40/day · ₹450/mo",
        pickupLocation = "Block B Cycle Stand",
        details = "Lightweight 7-speed cycles with lock and basket for quick hops across hostels, library, and academic blocks.",
        highlights = listOf("Free Lock", "Semester Pass", "On-Campus Maintenance"),
        postedBy = "Vikram Sharma",
        contactPhone = "+91 98140 22310",
        contactEmail = "greencampus.cycles@example.com",
        contactNote = "Collect from Block B shed with student ID card.",
        interestedCount = 9,
    ),
    RentalListingItem(
        id = "r3",
        title = "Royal Enfield / Commuter Bike",
        provider = "Highway Student Rentals",
        category = RentalCategory.BIKE_SCOOTY,
        rateLabel = "₹550/day · Weekend Plans",
        pickupLocation = "Phagwara Highway Hub (1.2 km)",
        details = "Well-serviced bikes for weekend trips or internships outside city limits. Dual helmets and roadside assistance covered.",
        highlights = listOf("Weekend Pack", "2 Helmets Included", "24/7 Roadside Help"),
        postedBy = "Arjun Gill",
        contactPhone = "+91 98550 77881",
        contactNote = "WhatsApp or call 2 hours before pickup.",
        interestedCount = 6,
    ),
    RentalListingItem(
        id = "r4",
        title = "Airport & Railway Pooling Cabs",
        provider = "PEERS Verified Cab Desk",
        category = RentalCategory.CAB_SHUTTLE,
        rateLabel = "Shared ₹350–₹600/seat",
        pickupLocation = "Campus Main Gate Pickup Zone",
        details = "Pre-scheduled shared sedans and SUVs for Delhi T3, Chandigarh Airport, and Jalandhar/Phagwara railway stations.",
        highlights = listOf("Split Fare", "Fixed Student Rates", "Night Pickup Safe"),
        postedBy = "Rohan Mehta (Transport Coord.)",
        contactPhone = "+91 97800 55412",
        contactEmail = "cabpool@peerscampus.in",
        contactNote = "Share train/flight time to match co-passengers.",
        interestedCount = 19,
    ),
    RentalListingItem(
        id = "ex1",
        title = "Electric Kettle, Induction & Study Lamp Set",
        provider = "Graduating Senior Pass-On",
        category = RentalCategory.EXCHANGE,
        rateLabel = "Exchange or ₹750 bundle",
        pickupLocation = "BH-4 Common Room / Law Gate",
        details = "1.5L Prestige electric kettle, Philips study lamp, and single cookware pan in clean working condition. Open to swapping for Data Structures books or ₹750.",
        highlights = listOf("Tested Working", "Hostel Friendly", "Open to Swap"),
        postedBy = "Aisha Verma (Final Year CSE)",
        contactPhone = "+91 98761 33490",
        contactEmail = "aisha.verma@peerscampus.in",
        contactNote = "Available after 5 PM on weekdays.",
        interestedCount = 11,
    ),
    RentalListingItem(
        id = "ex2",
        title = "Men's Formal Blazer & Interview Trousers (Size 40)",
        provider = "Campus Formal Closet",
        category = RentalCategory.EXCHANGE,
        rateLabel = "Swap / Borrow for Placements",
        pickupLocation = "Block 34 Outside Cafeteria",
        details = "Navy blue slim-fit blazer and crisp trousers worn only twice for placement drives. Happy to lend for interviews or exchange.",
        highlights = listOf("Dry Cleaned", "Size 40 Regular", "Placement Ready"),
        postedBy = "Kabir Singh (MBA)",
        contactPhone = "+91 99142 88019",
        contactNote = "Ping 1 day before your interview date.",
        interestedCount = 7,
    ),
    RentalListingItem(
        id = "ex3",
        title = "GATE CSE & Engineering Maths Textbooks + Lab Coat",
        provider = "Senior Book Bank",
        category = RentalCategory.EXCHANGE,
        rateLabel = "Free Swap / ₹300",
        pickupLocation = "LPU Unimall Upper Deck",
        details = "Made Easy CSE notes, BS Grewal Higher Engineering Mathematics, and White Lab Coat (Size M) ready for juniors.",
        highlights = listOf("Zero Markings", "Full Set", "Exam Prep"),
        postedBy = "Meera Nair",
        contactEmail = "meera.nair@peerscampus.in",
        contactNote = "Meet near Unimall book corner between 1–2 PM.",
        interestedCount = 8,
    ),
    RentalListingItem(
        id = "t1",
        title = "2 Confirmed Phagwara -> New Delhi Shatabdi E-Tickets",
        provider = "Student Travel Share",
        category = RentalCategory.TICKETS,
        rateLabel = "₹720 / ticket (Cost Price)",
        pickupLocation = "Block 32 / Law Gate",
        details = "Plans shifted for home visit this Saturday morning (07:50 AM Shatabdi CC). Offering 2 adjacent seats at original IRCTC fare.",
        highlights = listOf("Saturday 7:50 AM", "Chair Car", "Original Price"),
        postedBy = "Kartik Verma",
        contactPhone = "+91 98722 44109",
        contactNote = "Call to coordinate transfer before chart prep.",
        interestedCount = 5,
    ),
    RentalListingItem(
        id = "t2",
        title = "2 IMAX Movie Passes — Saturday Night Show (Jalandhar)",
        provider = "Campus Entertainment Share",
        category = RentalCategory.TICKETS,
        rateLabel = "₹450 for pair",
        pickupLocation = "LPU Unimall",
        details = "2 recliner-row e-passes for Saturday 8:15 PM show at Viva Collage Mall. Can share QR/booking code immediately.",
        highlights = listOf("2 Passes", "Saturday Evening", "Instant Transfer"),
        postedBy = "Neha Kulkarni",
        contactEmail = "neha.k@peerscampus.in",
        contactPhone = "+91 98159 22104",
        interestedCount = 4,
    ),
    RentalListingItem(
        id = "d1",
        title = "Casio fx-991EX Scientific Calculator & USB-C Hub",
        provider = "Tech Peer Swap",
        category = RentalCategory.DIGITAL_ITEMS,
        rateLabel = "₹850 / Exam Lend",
        pickupLocation = "Block 27 Lobby",
        details = "Mint-condition non-programmable Casio scientific calculator approved for university exams, bundled with 5-in-1 Type-C HDMI hub.",
        highlights = listOf("Exam Approved", "HDMI + USB 3.0", "With Pouch"),
        postedBy = "Siddharth Rao",
        contactPhone = "+91 98760 88213",
        contactNote = "Meet between lectures at Block 27.",
        interestedCount = 12,
    ),
    RentalListingItem(
        id = "d2",
        title = "TP-Link Dual-Band Wi-Fi Router + Mechanical Keyboard",
        provider = "Hostel Room Tech Upgrade",
        category = RentalCategory.DIGITAL_ITEMS,
        rateLabel = "₹1,400 combo or separate",
        pickupLocation = "Green Valley Apartments / BH-5",
        details = "Archer C6 AC1200 dual-band router (₹800) and compact Redragon wired mechanical keyboard (₹700). Ideal for PG rooms and coding.",
        highlights = listOf("Tested Working", "Low Latency", "Bill Available"),
        postedBy = "Amanjot Gill",
        contactPhone = "+91 99152 66401",
        contactEmail = "amanjot.g@peerscampus.in",
        interestedCount = 10,
    ),
    RentalListingItem(
        id = "lf1",
        title = "Found: Black Boat Airdopes Case near Block 34 Room 201",
        provider = "Lost & Found Board",
        category = RentalCategory.LOST_AND_FOUND,
        rateLabel = "Found Item",
        pickupLocation = "Block 34 Ground Floor Reception",
        details = "Found a matte black earbuds charging case on the desk after the 3 PM lecture. Verify sticker inside to claim.",
        highlights = listOf("Found Today", "Block 34"),
        postedBy = "Dev Patel",
        contactPhone = "+91 98155 60123",
        contactNote = "Message with the sticker color inside the lid.",
        interestedCount = 2,
    ),
    RentalListingItem(
        id = "lf2",
        title = "Lost: Student ID Card & Hostel Keychain (Blue Lanyard)",
        provider = "Lost & Found Board",
        category = RentalCategory.LOST_AND_FOUND,
        rateLabel = "Lost Item",
        pickupLocation = "",
        details = "Lost my blue lanyard student ID + 2 silver hostel keys somewhere on campus or Law Gate road yesterday evening.",
        highlights = listOf("Urgent", "Reward Coffee"),
        postedBy = "Ananya Rao",
        contactPhone = "+91 98780 19284",
        contactEmail = "ananya.rao@peerscampus.in",
        contactNote = "Please call or drop at Block 13 DSW reception.",
        interestedCount = 3,
    ),
)

private fun loadLocalRentalListings(context: Context): List<RentalListingItem> {
    val prefs = context.getSharedPreferences("peers_rentals_local", Context.MODE_PRIVATE)
    val raw = prefs.getString("user_rentals", null) ?: return emptyList()
    return runCatching {
        val arr = JSONArray(raw)
        (0 until arr.length()).mapNotNull { idx ->
            val obj = arr.getJSONObject(idx)
            val cat = runCatching { RentalCategory.valueOf(obj.getString("category")) }
                .getOrDefault(RentalCategory.EXCHANGE)
            val hlArr = obj.optJSONArray("highlights") ?: JSONArray()
            val highlights = (0 until hlArr.length()).map { hlArr.getString(it) }
            val phArr = obj.optJSONArray("photoUris") ?: JSONArray()
            val photos = (0 until phArr.length()).map { phArr.getString(it) }
            RentalListingItem(
                id = obj.getString("id"),
                title = obj.getString("title"),
                provider = obj.optString("provider", "Student Listing"),
                category = cat,
                rateLabel = obj.optString("rateLabel", "Contact for details"),
                pickupLocation = obj.optString("pickupLocation", ""),
                details = obj.optString("details", ""),
                highlights = highlights,
                postedBy = obj.optString("postedBy", "Peer"),
                contactPhone = obj.optString("contactPhone", ""),
                contactEmail = obj.optString("contactEmail", ""),
                contactNote = obj.optString("contactNote", ""),
                photoUris = photos,
                authorUid = obj.optString("authorUid", ""),
                interestedCount = obj.optInt("interestedCount", 0),
            )
        }
    }.getOrDefault(emptyList())
}

private fun saveLocalRentalListings(context: Context, list: List<RentalListingItem>) {
    runCatching {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("provider", item.provider)
                    put("category", item.category.name)
                    put("rateLabel", item.rateLabel)
                    put("pickupLocation", item.pickupLocation)
                    put("details", item.details)
                    put("highlights", JSONArray(item.highlights))
                    put("postedBy", item.postedBy)
                    put("contactPhone", item.contactPhone)
                    put("contactEmail", item.contactEmail)
                    put("contactNote", item.contactNote)
                    put("photoUris", JSONArray(item.photoUris))
                    put("authorUid", item.authorUid)
                    put("interestedCount", item.interestedCount)
                }
            )
        }
        context.getSharedPreferences("peers_rentals_local", Context.MODE_PRIVATE)
            .edit().putString("user_rentals", arr.toString()).apply()
    }
}

private suspend fun copyGalleryImagesLocally(context: Context, uris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
    val dir = File(context.filesDir, "rental_media").apply { mkdirs() }
    uris.take(4).mapNotNull { uri ->
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@runCatching null
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@runCatching null
            val maxDim = 960
            val scaled = if (bmp.width > maxDim || bmp.height > maxDim) {
                val scale = minOf(maxDim.toFloat() / bmp.width, maxDim.toFloat() / bmp.height)
                Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt().coerceAtLeast(1), (bmp.height * scale).toInt().coerceAtLeast(1), true)
            } else bmp
            val out = File(dir, "rental-${System.currentTimeMillis()}-${(100..999).random()}.jpg")
            ByteArrayOutputStream().use { bos ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 82, bos)
                out.writeBytes(bos.toByteArray())
            }
            Uri.fromFile(out).toString()
        }.getOrNull()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RentalsTransportScreen(
    onBack: () -> Unit,
    onAskAiRentals: (String) -> Unit,
    user: DashboardUser? = null,
    unreadCount: Int = 0,
    onProfileClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val cache = remember { OfflineCacheRepository.get(context) }
    val prefs = remember { context.getSharedPreferences("peers_rentals_local", Context.MODE_PRIVATE) }
    val currentUid = auth.currentUser?.uid.orEmpty()

    var selectedListing by remember { mutableStateOf<RentalListingItem?>(null) }
    var showAddForm by remember { mutableStateOf(false) }
    var editingListing by remember { mutableStateOf<RentalListingItem?>(null) }
    var deletingListing by remember { mutableStateOf<RentalListingItem?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf(RentalCategory.ALL) }
    var userListings by remember { mutableStateOf(loadLocalRentalListings(context)) }
    var remoteListings by remember { mutableStateOf<List<RentalListingItem>>(emptyList()) }
    var deletedIds by remember { mutableStateOf(prefs.getStringSet("deleted_rental_ids", emptySet()) ?: emptySet()) }
    var interestedIds by remember { mutableStateOf(prefs.getStringSet("interested_rental_ids", emptySet()) ?: emptySet()) }
    var deltaCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    DisposableEffect(Unit) {
        val reg = runCatching {
            firestore.collection("rentalListings").addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                remoteListings = snap?.documents?.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val cat = runCatching { RentalCategory.valueOf(doc.getString("category").orEmpty()) }
                        .getOrDefault(RentalCategory.EXCHANGE)
                    RentalListingItem(
                        id = doc.id,
                        title = title,
                        provider = doc.getString("provider").orEmpty().ifBlank { "Student Listing" },
                        category = cat,
                        rateLabel = doc.getString("rateLabel").orEmpty(),
                        pickupLocation = doc.getString("pickupLocation").orEmpty(),
                        details = doc.getString("details").orEmpty(),
                        highlights = (doc.get("highlights") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                        postedBy = doc.getString("postedBy").orEmpty().ifBlank { "Student Peer" },
                        contactPhone = doc.getString("contactPhone").orEmpty(),
                        contactEmail = doc.getString("contactEmail").orEmpty(),
                        contactNote = doc.getString("contactNote").orEmpty(),
                        photoUris = (doc.get("photoUris") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                        authorUid = doc.getString("authorUid").orEmpty(),
                        interestedCount = (doc.getLong("interestedCount") ?: 0L).toInt(),
                    )
                }.orEmpty()
            }
        }.getOrNull()
        onDispose { reg?.remove() }
    }

    fun isMine(item: RentalListingItem): Boolean =
        (currentUid.isNotBlank() && item.authorUid == currentUid) || item.id.startsWith("user-")

    fun effectiveInterestedCount(item: RentalListingItem): Int =
        (item.interestedCount + (deltaCounts[item.id] ?: 0)).coerceAtLeast(0)

    fun toggleInterested(item: RentalListingItem) {
        val nowInterested = item.id !in interestedIds
        val next = if (nowInterested) interestedIds + item.id else interestedIds - item.id
        interestedIds = next
        prefs.edit().putStringSet("interested_rental_ids", next).apply()
        val delta = if (nowInterested) 1 else -1
        deltaCounts = deltaCounts + (item.id to ((deltaCounts[item.id] ?: 0) + delta))
        if (nowInterested) {
            scope.launch {
                val meName = user?.name?.ifBlank { null } ?: auth.currentUser?.displayName ?: "A peer"
                cache.emitUnifiedNotification(
                    title = "Interest Noted: ${item.title}",
                    body = "$meName marked interest in \"${item.title}\" (${item.category.label}). Contact: ${item.contactPhone.ifBlank { item.contactEmail }}",
                    type = "info",
                    prefKey = "notif_events",
                    targetUid = item.authorUid.takeIf { it.isNotBlank() && it != currentUid },
                )
            }
            Toast.makeText(context, "Marked interested!", Toast.LENGTH_SHORT).show()
        }
        runCatching {
            firestore.collection("rentalListings").document(item.id)
                .update("interestedCount", FieldValue.increment(delta.toLong()))
        }
    }

    fun deleteListingItem(item: RentalListingItem) {
        deletedIds = deletedIds + item.id
        prefs.edit().putStringSet("deleted_rental_ids", deletedIds).apply()
        userListings = userListings.filterNot { it.id == item.id }
        saveLocalRentalListings(context, userListings)
        if (selectedListing?.id == item.id) selectedListing = null
        scope.launch {
            runCatching { firestore.collection("rentalListings").document(item.id).delete().await() }
        }
        Toast.makeText(context, "Listing removed.", Toast.LENGTH_SHORT).show()
    }

    val allListings = remember(userListings, remoteListings, deletedIds) {
        (userListings + remoteListings + seededRentalListings)
            .filter { it.id !in deletedIds }
            .distinctBy { it.id }
    }

    val visibleListings = remember(selectedCategory, allListings) {
        if (selectedCategory == RentalCategory.ALL) allListings
        else allListings.filter { it.category == selectedCategory }
    }

    BackHandler {
        when {
            editingListing != null -> editingListing = null
            showAddForm -> showAddForm = false
            selectedListing != null -> selectedListing = null
            else -> onBack()
        }
    }

    if (showAddForm || editingListing != null) {
        val editing = editingListing
        RentalListingFormScreen(
            initial = editing,
            defaultCategory = editing?.category ?: if (selectedCategory == RentalCategory.ALL) RentalCategory.EXCHANGE else selectedCategory,
            defaultPoster = editing?.postedBy ?: user?.name.orEmpty().ifBlank { auth.currentUser?.displayName.orEmpty() },
            defaultEmail = editing?.contactEmail ?: user?.email.orEmpty().ifBlank { auth.currentUser?.email.orEmpty() },
            defaultPhone = editing?.contactPhone ?: listOfNotNull(
                user?.phoneCountryCode?.takeIf { it.isNotBlank() },
                user?.phoneNumber?.takeIf { it.isNotBlank() },
            ).joinToString(" ").trim(),
            authorUid = currentUid,
            onBack = {
                showAddForm = false
                editingListing = null
            },
            onSave = { savedItem ->
                val updated = listOf(savedItem) + userListings.filterNot { it.id == savedItem.id }
                userListings = updated
                saveLocalRentalListings(context, updated)
                selectedListing = if (editing != null) savedItem else null
                showAddForm = false
                editingListing = null
                runCatching {
                    firestore.collection("rentalListings").document(savedItem.id).set(
                        mapOf(
                            "title" to savedItem.title,
                            "provider" to savedItem.provider,
                            "category" to savedItem.category.name,
                            "rateLabel" to savedItem.rateLabel,
                            "pickupLocation" to savedItem.pickupLocation,
                            "details" to savedItem.details,
                            "highlights" to savedItem.highlights,
                            "postedBy" to savedItem.postedBy,
                            "contactPhone" to savedItem.contactPhone,
                            "contactEmail" to savedItem.contactEmail,
                            "contactNote" to savedItem.contactNote,
                            "photoUris" to savedItem.photoUris.filter { it.startsWith("http") },
                            "authorUid" to savedItem.authorUid,
                            "interestedCount" to savedItem.interestedCount,
                            "createdAt" to FieldValue.serverTimestamp(),
                        )
                    )
                }
                Toast.makeText(context, if (editing != null) "Listing updated!" else "Listing published!", Toast.LENGTH_SHORT).show()
            },
        )
        return
    }

    if (selectedListing != null) {
        val liveItem = allListings.firstOrNull { it.id == selectedListing!!.id } ?: selectedListing!!
        RentalDetailView(
            item = liveItem,
            isMine = isMine(liveItem),
            isInterested = liveItem.id in interestedIds,
            interestedCount = effectiveInterestedCount(liveItem),
            user = user,
            unreadCount = unreadCount,
            onToggleInterested = { toggleInterested(liveItem) },
            onEdit = { editingListing = liveItem },
            onDelete = { deletingListing = liveItem },
            onBack = { selectedListing = null },
            onProfileClick = onProfileClick,
            onBellClick = onBellClick,
            modifier = modifier,
        )
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                PeersSubScreenHeader(
                    title = "Rentals & Transport",
                    subtitle = "Bikes, tickets, digital items, exchange & lost & found",
                    user = user,
                    unreadCount = unreadCount,
                    onBack = onBack,
                    onProfileClick = onProfileClick,
                    onBellClick = onBellClick,
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddForm = true },
                    containerColor = PeersPrimaryIndigo,
                    contentColor = Color.White,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Listing")
                        Spacer(Modifier.width(6.dp))
                        Text("Add Listing", fontFamily = GoogleSans, fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier.fillMaxSize(),
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    scope.launch {
                        userListings = loadLocalRentalListings(context)
                        runCatching {
                            val snap = firestore.collection("rentalListings").get().await()
                            remoteListings = snap.documents.mapNotNull { doc ->
                                val title = doc.getString("title") ?: return@mapNotNull null
                                val cat = runCatching { RentalCategory.valueOf(doc.getString("category").orEmpty()) }
                                    .getOrDefault(RentalCategory.EXCHANGE)
                                RentalListingItem(
                                    id = doc.id,
                                    title = title,
                                    provider = doc.getString("provider").orEmpty().ifBlank { "Student Listing" },
                                    category = cat,
                                    rateLabel = doc.getString("rateLabel").orEmpty(),
                                    pickupLocation = doc.getString("pickupLocation").orEmpty(),
                                    details = doc.getString("details").orEmpty(),
                                    highlights = (doc.get("highlights") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                                    postedBy = doc.getString("postedBy").orEmpty().ifBlank { "Student Peer" },
                                    contactPhone = doc.getString("contactPhone").orEmpty(),
                                    contactEmail = doc.getString("contactEmail").orEmpty(),
                                    contactNote = doc.getString("contactNote").orEmpty(),
                                    photoUris = (doc.get("photoUris") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                                    authorUid = doc.getString("authorUid").orEmpty(),
                                    interestedCount = (doc.getLong("interestedCount") ?: 0L).toInt(),
                                )
                            }
                        }
                        isRefreshing = false
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable {
                                    onAskAiRentals(
                                        "What are the best bike/scooty rentals, campus shuttles, tickets, digital items, student exchange tips, and lost & found procedures near campus?"
                                    )
                                },
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.horizontalGradient(listOf(PeersPrimaryIndigo, PeersLavender)))
                                    .padding(18.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White.copy(alpha = 0.22f),
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        PeersDrawableIcon(
                                            lightRes = R.drawable.aibot_white,
                                            darkRes = R.drawable.aibot_white,
                                            contentDescription = "AI Guide",
                                            size = 26.dp,
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Ask PEERS AI for Travel & Exchange Tips",
                                        fontFamily = GoogleSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.5.sp,
                                        color = Color.White,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Get recommendations on rentals, tickets, swaps, or lost items.",
                                        fontFamily = Lato,
                                        fontSize = 12.5.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = "Open AI Guide",
                                    tint = Color.White,
                                )
                            }
                        }
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(RentalCategory.entries) { category ->
                                val selected = selectedCategory == category
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text(
                                            text = category.label,
                                            fontFamily = GoogleSans,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 13.sp,
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PeersPrimaryIndigo,
                                        selectedLabelColor = Color.White,
                                    ),
                                )
                            }
                        }
                    }

                    items(visibleListings, key = { it.id }) { item ->
                        val accent = categoryAccent(item.category)
                        val mine = isMine(item)
                        val interested = item.id in interestedIds
                        val count = effectiveInterestedCount(item)
                        var menuOpen by remember { mutableStateOf(false) }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedListing = item },
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(accent.copy(alpha = 0.28f)),
                                    ) {
                                        Icon(
                                            imageVector = categoryIcon(item.category),
                                            contentDescription = item.title,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = if (item.pickupLocation.isNotBlank()) "${item.provider} · ${item.pickupLocation}" else item.provider,
                                            fontFamily = Lato,
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    if (mine) {
                                        Box {
                                            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(30.dp)) {
                                                Icon(Icons.Rounded.MoreVert, contentDescription = "Options", modifier = Modifier.size(18.dp))
                                            }
                                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                                DropdownMenuItem(
                                                    text = { Text("Edit listing") },
                                                    onClick = { menuOpen = false; editingListing = item },
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Mark as Sold / Delete", color = MaterialTheme.colorScheme.error) },
                                                    onClick = { menuOpen = false; deletingListing = item },
                                                )
                                            }
                                        }
                                    }
                                }

                                if (item.photoUris.isNotEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(item.photoUris) { uriStr ->
                                            AsyncImage(
                                                model = uriStr,
                                                contentDescription = item.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(width = 140.dp, height = 94.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = PeersPrimaryIndigo.copy(alpha = 0.12f),
                                    ) {
                                        Text(
                                            text = item.rateLabel,
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = PeersPrimaryIndigo,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = accent.copy(alpha = 0.35f),
                                    ) {
                                        Text(
                                            text = item.category.label,
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = item.details,
                                    fontFamily = Lato,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                if (item.highlights.isNotEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        item.highlights.forEach { tag ->
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                            ) {
                                                Text(
                                                    text = tag,
                                                    fontFamily = Lato,
                                                    fontSize = 11.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = "Posted by ${item.postedBy}",
                                        fontFamily = Lato,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (interested) PeersPrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { toggleInterested(item) },
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        ) {
                                            Icon(
                                                if (interested) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                                contentDescription = "Interested",
                                                tint = if (interested) Color.White else PeersPrimaryIndigo,
                                                modifier = Modifier.size(15.dp),
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Interested ($count)",
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = if (interested) Color.White else MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { selectedListing = item },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, PeersPrimaryIndigo),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    ) {
                                        Text(
                                            "View Details",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = PeersPrimaryIndigo,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deletingListing?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingListing = null },
            title = { Text("Remove / Mark as Sold?", fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${target.title}\" permanently from Rentals & Exchange listings?") },
            confirmButton = {
                TextButton(onClick = {
                    deleteListingItem(target)
                    deletingListing = null
                }) { Text("Delete Listing", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { deletingListing = null }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RentalDetailView(
    item: RentalListingItem,
    isMine: Boolean,
    isInterested: Boolean,
    interestedCount: Int,
    user: DashboardUser?,
    unreadCount: Int,
    onToggleInterested: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val accent = categoryAccent(item.category)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PeersSubScreenHeader(
                title = item.category.label,
                subtitle = "Listing Details & Contact",
                user = user,
                unreadCount = unreadCount,
                onBack = onBack,
                onProfileClick = onProfileClick,
                onBellClick = onBellClick,
                trailingAction = if (isMine) {
                    {
                        Row {
                            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit Listing", tint = PeersPrimaryIndigo, modifier = Modifier.size(19.dp))
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete Listing", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                } else null,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (item.photoUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(item.photoUris) { uriStr ->
                        AsyncImage(
                            model = uriStr,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 270.dp, height = 180.dp)
                                .clip(RoundedCornerShape(18.dp)),
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(accent.copy(alpha = 0.35f)),
                        ) {
                            Icon(
                                imageVector = categoryIcon(item.category),
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = accent.copy(alpha = 0.4f),
                            ) {
                                Text(
                                    text = item.category.label,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                )
                            }
                            Text(
                                text = item.rateLabel,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PeersPrimaryIndigo,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = item.title,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = item.provider,
                        fontFamily = Lato,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )

                    if (item.pickupLocation.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 10.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Place,
                                contentDescription = null,
                                tint = PeersPrimaryIndigo,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = item.pickupLocation,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 14.dp))

                    Text(
                        text = "Article & Listing Information",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = item.details,
                        fontFamily = Lato,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )

                    if (item.highlights.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item.highlights.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    Text(
                                        text = tag,
                                        fontFamily = Lato,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onToggleInterested,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInterested) MaterialTheme.colorScheme.surfaceVariant else PeersPrimaryIndigo,
                            contentColor = if (isInterested) MaterialTheme.colorScheme.onSurface else Color.White,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                    ) {
                        Icon(
                            if (isInterested) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isInterested) "Interested ($interestedCount) · Tap to Remove" else "I'm Interested ($interestedCount)",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "Posted By",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(PeersPrimaryIndigo.copy(alpha = 0.15f)),
                        ) {
                            Text(
                                text = item.postedBy.firstOrNull()?.uppercase() ?: "P",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = PeersPrimaryIndigo,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.postedBy,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = item.provider,
                                fontFamily = Lato,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 14.dp))

                    Text(
                        text = "How to Reach",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (item.contactNote.isNotBlank()) {
                        Text(
                            text = item.contactNote,
                            fontFamily = Lato,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }

                    if (item.contactPhone.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    val clean = item.contactPhone.filter { it.isDigit() || it == '+' }
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")))
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Call ${item.contactPhone}", fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText(item.postedBy, item.contactPhone))
                                    Toast.makeText(context, "Phone copied", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (item.contactEmail.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${item.contactEmail}")).apply {
                                    putExtra(Intent.EXTRA_SUBJECT, "PEERS Enquiry: ${item.title}")
                                }
                                runCatching { context.startActivity(intent) }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Email ${item.contactEmail}", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RentalListingFormScreen(
    initial: RentalListingItem?,
    defaultCategory: RentalCategory,
    defaultPoster: String,
    defaultEmail: String,
    defaultPhone: String,
    authorUid: String,
    onBack: () -> Unit,
    onSave: (RentalListingItem) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val categories = RentalCategory.entries.filter { it != RentalCategory.ALL }
    val formKey = initial?.id ?: "new"

    var categoryName by rememberSaveable(formKey) { mutableStateOf(defaultCategory.name) }
    val category = runCatching { RentalCategory.valueOf(categoryName) }.getOrDefault(defaultCategory)
    var title by rememberSaveable(formKey) { mutableStateOf(initial?.title.orEmpty()) }
    var rateLabel by rememberSaveable(formKey) { mutableStateOf(initial?.rateLabel.orEmpty()) }
    var location by rememberSaveable(formKey) { mutableStateOf(initial?.pickupLocation.orEmpty()) }
    var details by rememberSaveable(formKey) { mutableStateOf(initial?.details.orEmpty()) }
    var postedBy by rememberSaveable(formKey) { mutableStateOf(defaultPoster) }
    var phone by rememberSaveable(formKey) { mutableStateOf(defaultPhone) }
    var email by rememberSaveable(formKey) { mutableStateOf(defaultEmail) }
    var contactNote by rememberSaveable(formKey) { mutableStateOf(initial?.contactNote.orEmpty()) }
    var photoUris by rememberSaveable(formKey) { mutableStateOf(initial?.photoUris.orEmpty()) }

    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val saved = copyGalleryImagesLocally(context, uris)
                photoUris = (photoUris + saved).distinct().take(4)
            }
        }
    }

    val canSubmit = title.isNotBlank() && details.isNotBlank() && (phone.isNotBlank() || email.isNotBlank())

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (initial != null) "Edit Listing" else "Post Rental, Item, Ticket or Lost & Found",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Category *", fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PeersPrimaryIndigo)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { categoryName = cat.name },
                        label = { Text(cat.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PeersPrimaryIndigo,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title / Article Name *") },
                placeholder = { Text("e.g. Shatabdi Ticket / Casio Calculator / Lost Keys") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = rateLabel,
                onValueChange = { rateLabel = it },
                label = { Text("Rate / Price / Status Tag") },
                placeholder = { Text("e.g. ₹250/day, ₹600, Swap, Found Item") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = {
                    Text(
                        if (category == RentalCategory.LOST_AND_FOUND) "Location (optional)"
                        else "Pickup / Campus Location (optional)"
                    )
                },
                placeholder = {
                    Text(
                        if (category == RentalCategory.LOST_AND_FOUND) "e.g. Unimall, Law Gate — or leave blank"
                        else "e.g. Law Gate / BH-4 / Block 34"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("Description & Details *") },
                placeholder = { Text("Condition, seat/show info, model specs, timing, or identification notes...") },
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            // Photos from Gallery
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Photos from Gallery (optional, up to 4)", fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = { galleryPicker.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Photos", fontSize = 12.sp)
                }
            }
            if (photoUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(photoUris) { uriStr ->
                        Box {
                            AsyncImage(
                                model = uriStr,
                                contentDescription = "Selected photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(22.dp)
                                    .clickable { photoUris = photoUris.filterNot { it == uriStr } },
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Remove photo", tint = Color.White, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = postedBy,
                onValueChange = { postedBy = it },
                label = { Text("Your Name") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Contact Phone (phone or email required) *") },
                placeholder = { Text("+91 98765 43210") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Contact Email") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = contactNote,
                onValueChange = { contactNote = it },
                label = { Text("Preferred Meetup / Contact Note (optional)") },
                placeholder = { Text("e.g. Call after 5 PM or meet at Unimall") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSave(
                        RentalListingItem(
                            id = initial?.id ?: "user-${System.currentTimeMillis()}",
                            title = title.trim(),
                            provider = when (category) {
                                RentalCategory.EXCHANGE -> "Peer Exchange"
                                RentalCategory.TICKETS -> "Tickets & Passes"
                                RentalCategory.DIGITAL_ITEMS -> "Digital & Electronics"
                                RentalCategory.LOST_AND_FOUND -> "Lost & Found"
                                else -> "Student / Campus Listing"
                            },
                            category = category,
                            rateLabel = rateLabel.trim().ifBlank { category.label },
                            pickupLocation = location.trim(),
                            details = details.trim(),
                            highlights = listOf(category.label),
                            postedBy = postedBy.trim().ifBlank { "Student Peer" },
                            contactPhone = phone.trim(),
                            contactEmail = email.trim(),
                            contactNote = contactNote.trim(),
                            photoUris = photoUris,
                            authorUid = initial?.authorUid?.ifBlank { authorUid } ?: authorUid,
                            interestedCount = initial?.interestedCount ?: 0,
                        )
                    )
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).height(50.dp),
            ) {
                Text(if (initial != null) "Save Changes" else "Publish Listing", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
