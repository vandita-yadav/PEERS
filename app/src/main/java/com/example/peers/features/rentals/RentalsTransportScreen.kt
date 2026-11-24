package com.example.peers.features.rentals

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.PedalBike
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.R
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import com.example.peers.ui.theme.PeersWarmYellow

enum class RentalCategory(val label: String) {
    ALL("All"),
    BIKE_SCOOTY("Bike & Scooty"),
    CYCLES("Cycles"),
    CAB_SHUTTLE("Cab & Shuttle"),
    EXCHANGE("Exchange (Soon)")
}

private data class TransportOption(
    val id: String,
    val title: String,
    val provider: String,
    val category: RentalCategory,
    val rateLabel: String,
    val pickupLocation: String,
    val details: String,
    val highlights: List<String>,
    val icon: ImageVector,
    val accent: Color,
)

private val transportListings = listOf(
    TransportOption(
        id = "r1",
        title = "Honda Activa 6G / Scooty Rental",
        provider = "Campus Wheels Co.",
        category = RentalCategory.BIKE_SCOOTY,
        rateLabel = "₹250/day · ₹2,400/mo",
        pickupLocation = "Main Gate & Law Gate Counter",
        details = "Ideal for daily grocery runs and off-campus commute. Helmet and RC papers included; valid DL + Student ID required.",
        highlights = listOf("1 Helmet Free", "Zero Hidden Deposit", "Student Discount"),
        icon = Icons.Rounded.DirectionsBike,
        accent = PeersWarmYellow,
    ),
    TransportOption(
        id = "r2",
        title = "Geared City & Hybrid Cycles",
        provider = "GreenCampus Pedals",
        category = RentalCategory.CYCLES,
        rateLabel = "₹40/day · ₹450/mo",
        pickupLocation = "Block B Cycle Stand",
        details = "Lightweight 7-speed cycles with lock and basket for quick hops across hostels, library, and academic blocks.",
        highlights = listOf("Free Lock", "Semester Pass Available", "On-Campus Maintenance"),
        icon = Icons.Rounded.PedalBike,
        accent = PeersMint,
    ),
    TransportOption(
        id = "r3",
        title = "Royal Enfield / Commuter Bike",
        provider = "Highway Student Rentals",
        category = RentalCategory.BIKE_SCOOTY,
        rateLabel = "₹550/day · Weekend Plans",
        pickupLocation = "Phagwara Highway Hub (1.2 km)",
        details = "Well-serviced bikes for weekend trips or internships outside city limits. Dual helmets and roadside assistance covered.",
        highlights = listOf("Weekend Pack", "2 Helmets Included", "24/7 Roadside Help"),
        icon = Icons.Rounded.DirectionsBike,
        accent = PeersLavender,
    ),
    TransportOption(
        id = "r4",
        title = "Morning & Evening Campus Shuttle",
        provider = "University Transit Loop",
        category = RentalCategory.CAB_SHUTTLE,
        rateLabel = "₹20/ride · ₹400 Monthly Pass",
        pickupLocation = "All Hostel Gates -> Academic Blocks",
        details = "Runs every 15 minutes from 7:30 AM to 8:30 PM covering Law Gate PG lanes, bus stand, and main campus.",
        highlights = listOf("Every 15 Mins", "PG Route Covered", "AC Electric Buses"),
        icon = Icons.Rounded.DirectionsBus,
        accent = PeersSky,
    ),
    TransportOption(
        id = "r5",
        title = "Airport & Railway Pooling Cabs",
        provider = "PEERS Verified Cab Desk",
        category = RentalCategory.CAB_SHUTTLE,
        rateLabel = "Shared ₹350–₹600/seat",
        pickupLocation = "Campus Main Gate Pickup Zone",
        details = "Pre-scheduled shared sedans and SUVs for Delhi T3, Chandigarh Airport, and Jalandhar/Phagwara railway stations.",
        highlights = listOf("Split Fare", "Fixed Student Rates", "Night Pickup Safe"),
        icon = Icons.Rounded.LocalTaxi,
        accent = PeersPrimaryIndigo,
    ),
)

private val exchangePreviewItems = listOf(
    "Electronic Gadgets (Laptops, Monitors, Lamps, Kettles)",
    "Kitchen Essentials (Induction, Cookware, Utensils)",
    "Textbooks, Reference Guides & Lab Coats",
    "Formal Wear (Blazers, Interview Attire)",
    "Seasonal Clothing & Dorm Storage",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RentalsTransportScreen(
    onBack: () -> Unit,
    onAskAiRentals: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { onBack() }

    var selectedCategory by remember { mutableStateOf(RentalCategory.ALL) }

    val visibleListings = remember(selectedCategory) {
        when (selectedCategory) {
            RentalCategory.ALL -> transportListings
            RentalCategory.EXCHANGE -> emptyList()
            else -> transportListings.filter { it.category == selectedCategory }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Rentals & Transport",
                            fontFamily = GoogleSans,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Bikes, cycles, campus shuttles & student deals",
                            fontFamily = Lato,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = 28.dp,
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
                                "What are the best budget bike, scooty, or cycle rental options and cab/shuttle tips for students on PEERS?"
                            )
                        },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PeersPrimaryIndigo, PeersLavender)
                                )
                            )
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
                                text = "Ask PEERS AI for Local Travel Tips",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = Color.White,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Get instant recommendations on scooty rentals, shuttle routes, and shared airport cabs.",
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

            // ── Transport & Rental Listings ───────────────────────────────────
            items(visibleListings, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(item.accent.copy(alpha = 0.25f)),
                            ) {
                                Icon(
                                    imageVector = item.icon,
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

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PeersPrimaryIndigo.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = item.rateLabel,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = PeersPrimaryIndigo,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = item.details,
                            fontFamily = Lato,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

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
                }
            }

            // ── Student Item Exchange Preview Card (Coming in future update) ───
            if (selectedCategory == RentalCategory.ALL || selectedCategory == RentalCategory.EXCHANGE) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PeersSky.copy(alpha = 0.14f)
                        ),
                        border = BorderStroke(1.dp, PeersSky.copy(alpha = 0.65f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PeersSky.copy(alpha = 0.4f)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SwapHoriz,
                                        contentDescription = "Student Exchange",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Student Exchange Hub",
                                            fontFamily = GoogleSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = PeersPrimaryIndigo,
                                        ) {
                                            Text(
                                                text = "Coming Soon",
                                                fontFamily = GoogleSans,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Pass on or swap essentials with graduating seniors & campus peers",
                                        fontFamily = Lato,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "In an upcoming update, you'll be able to exchange or borrow campus items directly from trusted students:",
                                fontFamily = Lato,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            Spacer(Modifier.height(8.dp))

                            exchangePreviewItems.forEach { categoryText ->
                                Text(
                                    text = "- $categoryText",
                                    fontFamily = Lato,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp),
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    onAskAiRentals(
                                        "Where can new students find affordable second-hand study gear, kitchen items, bikes, or local transport near campus?"
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Ask AI Guide for Student Transport & Gear Tips",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
