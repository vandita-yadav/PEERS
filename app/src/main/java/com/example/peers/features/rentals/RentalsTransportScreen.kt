package com.example.peers.features.rentals

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.FindInPage
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.PedalBike
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.R
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.dashboard.components.PeersSubScreenHeader
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
import org.json.JSONArray
import org.json.JSONObject

enum class RentalCategory(val label: String) {
    ALL("All"),
    BIKE_SCOOTY("Bike & Scooty"),
    CYCLES("Cycles"),
    CAB_SHUTTLE("Cab & Shuttle"),
    EXCHANGE("Student Exchange"),
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
)

private fun categoryIcon(cat: RentalCategory): ImageVector = when (cat) {
    RentalCategory.BIKE_SCOOTY -> Icons.Rounded.DirectionsBike
    RentalCategory.CYCLES -> Icons.Rounded.PedalBike
    RentalCategory.CAB_SHUTTLE -> Icons.Rounded.LocalTaxi
    RentalCategory.EXCHANGE -> Icons.Rounded.SwapHoriz
    RentalCategory.LOST_AND_FOUND -> Icons.Rounded.FindInPage
    RentalCategory.ALL -> Icons.Rounded.DirectionsBus
}

private fun categoryAccent(cat: RentalCategory): Color = when (cat) {
    RentalCategory.BIKE_SCOOTY -> PeersWarmYellow
    RentalCategory.CYCLES -> PeersMint
    RentalCategory.CAB_SHUTTLE -> PeersSky
    RentalCategory.EXCHANGE -> PeersLavender
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
    ),
    RentalListingItem(
        id = "lf1",
        title = "Found: Black Boat Airdopes Case near Block 34 Room 201",
        provider = "Lost & Found Board",
        category = RentalCategory.LOST_AND_FOUND,
        rateLabel = "Found Item",
        pickupLocation = "Block 34 Ground Floor Reception",
        details = "Found a matte black earbuds charging case on the desk after the 3 PM lecture. Verify serial/sticker inside to claim.",
        highlights = listOf("Found Today", "Block 34"),
        postedBy = "Dev Patel",
        contactPhone = "+91 98155 60123",
        contactNote = "Message with the sticker color inside the lid.",
    ),
    RentalListingItem(
        id = "lf2",
        title = "Lost: Student ID Card & Hostel Keychain (Blue Lanyard)",
        provider = "Lost & Found Board",
        category = RentalCategory.LOST_AND_FOUND,
        rateLabel = "Lost Item",
        pickupLocation = "Between Mittal Auditorium (Block 35) & Unimall",
        details = "Dropped around 5:30 PM yesterday near Unimall stairs. Has 2 silver keys and a library barcode attached.",
        highlights = listOf("Urgent", "Reward Coffee"),
        postedBy = "Ananya Rao",
        contactPhone = "+91 98780 19284",
        contactEmail = "ananya.rao@peerscampus.in",
        contactNote = "Please call or drop at Block 13 DSW reception.",
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
            RentalListingItem(
                id = obj.getString("id"),
                title = obj.getString("title"),
                provider = obj.optString("provider", "Student Listing"),
                category = cat,
                rateLabel = obj.optString("rateLabel", "Contact for details"),
                pickupLocation = obj.optString("pickupLocation", "Campus"),
                details = obj.optString("details", ""),
                highlights = highlights,
                postedBy = obj.optString("postedBy", "Peer"),
                contactPhone = obj.optString("contactPhone", ""),
                contactEmail = obj.optString("contactEmail", ""),
                contactNote = obj.optString("contactNote", ""),
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
                }
            )
        }
        context.getSharedPreferences("peers_rentals_local", Context.MODE_PRIVATE)
            .edit().putString("user_rentals", arr.toString()).apply()
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
    var selectedListing by remember { mutableStateOf<RentalListingItem?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }

    BackHandler {
        if (selectedListing != null) selectedListing = null else onBack()
    }

    var selectedCategory by remember { mutableStateOf(RentalCategory.ALL) }
    var userListings by remember { mutableStateOf(loadLocalRentalListings(context)) }
    val allListings = remember(userListings) { userListings + seededRentalListings }

    val visibleListings = remember(selectedCategory, allListings) {
        if (selectedCategory == RentalCategory.ALL) allListings
        else allListings.filter { it.category == selectedCategory }
    }

    if (selectedListing != null) {
        RentalDetailView(
            item = selectedListing!!,
            user = user,
            unreadCount = unreadCount,
            onBack = { selectedListing = null },
            onProfileClick = onProfileClick,
            onBellClick = onBellClick,
            modifier = modifier,
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PeersSubScreenHeader(
                title = "Rentals & Transport",
                subtitle = "Bikes, shuttles, exchange & lost & found",
                user = user,
                unreadCount = unreadCount,
                onBack = onBack,
                onProfileClick = onProfileClick,
                onBellClick = onBellClick,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
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
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            // ── Pre-loaded PEERS AI Guide Card ────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable {
                            onAskAiRentals(
                                "What are the best bike/scooty rentals, campus shuttles, student item exchange tips, and lost & found procedures near campus?"
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
                                text = "Get recommendations on rentals, shuttles, swaps, or reporting lost items.",
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

            // ── Category Filter Chips ─────────────────────────────────────────
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

            // ── Cards for ALL Categories (including Exchange & Lost & Found) ──
            items(visibleListings, key = { it.id }) { item ->
                val accent = categoryAccent(item.category)
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
                                    text = "${item.provider} · ${item.pickupLocation}",
                                    fontFamily = Lato,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Posted by ${item.postedBy}",
                                fontFamily = Lato,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { selectedListing = item }) {
                                Text(
                                    text = "View Details",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Bold,
                                    color = PeersPrimaryIndigo,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = PeersPrimaryIndigo,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddRentalListingSheet(
            defaultCategory = selectedCategory.takeIf { it != RentalCategory.ALL } ?: RentalCategory.EXCHANGE,
            defaultPoster = user?.name.orEmpty().ifBlank { "Student Peer" },
            defaultEmail = user?.email.orEmpty(),
            defaultPhone = listOfNotNull(
                user?.phoneCountryCode?.takeIf { it.isNotBlank() },
                user?.phoneNumber?.takeIf { it.isNotBlank() },
            ).joinToString(" "),
            onDismiss = { showAddSheet = false },
            onAdd = { newItem ->
                val updated = listOf(newItem) + userListings
                userListings = updated
                saveLocalRentalListings(context, updated)
                showAddSheet = false
                Toast.makeText(context, "Listing added to ${newItem.category.label}.", Toast.LENGTH_SHORT).show()

                // Best-effort Firestore backup
                runCatching {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                    FirebaseFirestore.getInstance().collection("rentalsListings").document(newItem.id).set(
                        mapOf(
                            "title" to newItem.title,
                            "provider" to newItem.provider,
                            "category" to newItem.category.name,
                            "rateLabel" to newItem.rateLabel,
                            "pickupLocation" to newItem.pickupLocation,
                            "details" to newItem.details,
                            "postedBy" to newItem.postedBy,
                            "contactPhone" to newItem.contactPhone,
                            "contactEmail" to newItem.contactEmail,
                            "contactNote" to newItem.contactNote,
                            "uid" to uid,
                            "createdAt" to FieldValue.serverTimestamp(),
                        )
                    )
                }
            },
        )
    }
}

@Composable
private fun RentalDetailView(
    item: RentalListingItem,
    user: DashboardUser?,
    unreadCount: Int,
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
            // ── Hero Card with Full Listing Info ──────────────────────────────
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
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }

                    Text(
                        text = item.title,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 14.dp),
                    )

                    Text(
                        text = item.provider,
                        fontFamily = Lato,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )

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
                }
            }

            // ── Posted By & Contact Details Card ──────────────────────────────
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
private fun AddRentalListingSheet(
    defaultCategory: RentalCategory,
    defaultPoster: String,
    defaultEmail: String,
    defaultPhone: String,
    onDismiss: () -> Unit,
    onAdd: (RentalListingItem) -> Unit,
) {
    val categories = RentalCategory.entries.filter { it != RentalCategory.ALL }
    var category by remember { mutableStateOf(defaultCategory) }
    var title by remember { mutableStateOf("") }
    var rateLabel by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var postedBy by remember { mutableStateOf(defaultPoster) }
    var phone by remember { mutableStateOf(defaultPhone) }
    var email by remember { mutableStateOf(defaultEmail) }
    var contactNote by remember { mutableStateOf("") }

    val canSubmit = title.isNotBlank() && details.isNotBlank() && (phone.isNotBlank() || email.isNotBlank())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Post in Rentals, Exchange or Lost & Found",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            Text("Category *", fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PeersPrimaryIndigo)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
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
                placeholder = { Text("e.g. Study Table Lamp / Found Hostel Keys / Activa") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = rateLabel,
                onValueChange = { rateLabel = it },
                label = { Text("Rate / Exchange / Status Tag") },
                placeholder = { Text("e.g. ₹250/day, Swap / ₹400, Found at Unimall") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Pickup / Campus Location") },
                placeholder = { Text("e.g. Law Gate / BH-4 / Block 34") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("Description & Details *") },
                placeholder = { Text("Condition, timing, identification details, or terms...") },
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
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
                    onAdd(
                        RentalListingItem(
                            id = "user-${System.currentTimeMillis()}",
                            title = title.trim(),
                            provider = when (category) {
                                RentalCategory.EXCHANGE -> "Peer Exchange"
                                RentalCategory.LOST_AND_FOUND -> "Lost & Found"
                                else -> "Student / Campus Listing"
                            },
                            category = category,
                            rateLabel = rateLabel.trim().ifBlank { category.label },
                            pickupLocation = location.trim().ifBlank { "Campus" },
                            details = details.trim(),
                            highlights = listOf(category.label),
                            postedBy = postedBy.trim().ifBlank { "Student Peer" },
                            contactPhone = phone.trim(),
                            contactEmail = email.trim(),
                            contactNote = contactNote.trim(),
                        )
                    )
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Text("Publish Listing", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
