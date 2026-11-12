package com.example.peers.features.accommodation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccommodationViewModel = viewModel(),
) {
    val listings by viewModel.listings.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val wishlist by viewModel.wishlist.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val verifiedIds by viewModel.verifiedIds.collectAsStateWithLifecycle()
    val reviewedIds by viewModel.reviewedIds.collectAsStateWithLifecycle()
    val roommateRequests by viewModel.roommateRequests.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showFilters by remember { mutableStateOf(false) }
    var showAddListing by remember { mutableStateOf(false) }
    var showRoommates by remember { mutableStateOf(false) }
    var selectedListing by remember { mutableStateOf<AccommodationListing?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val visible = listings.filter(filters::matches)

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Nearby Accommodation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${visible.size} stays around your campus",
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
                actions = {
                    IconButton(onClick = { showRoommates = true }) {
                        BadgedBox(
                            badge = {
                                val pending = roommateRequests.count {
                                    it.incoming && it.status == RoommateRequestStatus.PENDING
                                }
                                if (pending > 0) {
                                    Badge { Text("$pending") }
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.People, contentDescription = "Roommate matching")
                        }
                    }
                    IconButton(onClick = { showFilters = true }) {
                        BadgedBox(
                            badge = {
                                if (filters.activeCount > 0) {
                                    Badge { Text("${filters.activeCount}") }
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.Tune, contentDescription = "Filters")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddListing = true },
                containerColor = PeersPrimaryIndigo,
                contentColor = Color.White,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text(
                        "Add listing",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            OutlinedTextField(
                value = filters.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Search area, PG, flat…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp),
            ) {
                item {
                    FilterChip(
                        selected = filters.types.isEmpty(),
                        onClick = { viewModel.applyFilters(filters.copy(types = emptySet())) },
                        label = { Text("All") },
                        colors = filterChipColors(),
                    )
                }
                items(accommodationTypes) { type ->
                    FilterChip(
                        selected = type in filters.types,
                        onClick = {
                            val types = if (type in filters.types) filters.types - type else filters.types + type
                            viewModel.applyFilters(filters.copy(types = types))
                        },
                        label = { Text(type) },
                        colors = filterChipColors(),
                    )
                }
            }

            if (filters.activeCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                ) {
                    AssistChip(
                        onClick = { showFilters = true },
                        label = { Text("${filters.activeCount} filter${if (filters.activeCount == 1) "" else "s"} active") },
                        leadingIcon = { Icon(Icons.Rounded.Tune, null, Modifier.size(14.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = PeersPrimaryIndigo,
                            leadingIconContentColor = PeersPrimaryIndigo,
                        ),
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = viewModel::clearFilters) {
                        Text("Clear", color = PeersPrimaryIndigo)
                    }
                }
            }

            if (visible.isEmpty()) {
                EmptyListings(onReset = viewModel::clearFilters)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(visible, key = { it.id }) { listing ->
                        AccommodationCard(
                            listing = listing,
                            wishlisted = listing.id in wishlist,
                            onWishlist = { viewModel.toggleWishlist(listing.id) },
                            onOpen = { selectedListing = listing },
                        )
                    }
                }
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            FilterSheet(
                current = filters,
                onApply = {
                    viewModel.applyFilters(it)
                    showFilters = false
                },
                onReset = viewModel::clearFilters,
            )
        }
    }

    selectedListing?.let { selected ->
        val listing = listings.firstOrNull { it.id == selected.id } ?: selected
        ModalBottomSheet(
            onDismissRequest = { selectedListing = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            AccommodationDetails(
                listing = listing,
                wishlisted = listing.id in wishlist,
                isVerifiedByMe = listing.id in verifiedIds,
                isReviewed = listing.id in reviewedIds,
                reviews = reviews[listing.id].orEmpty(),
                onWishlist = { viewModel.toggleWishlist(listing.id) },
                onMarkGenuine = { viewModel.markGenuine(listing) },
                onRemoveGenuine = { viewModel.unmarkGenuine(listing) },
                onAddReview = { rating, text -> viewModel.addReview(listing, rating, text) },
                onReportReview = { reviewId -> viewModel.reportReview(listing.id, reviewId) },
                onRoommateRequest = { contact, text ->
                    viewModel.sendRoommateRequest(listing, contact, text)
                },
            )
        }
    }

    if (showRoommates) {
        RoommateScreen(
            onBack = { showRoommates = false },
            viewModel = viewModel,
        )
    }

    if (showAddListing) {
        AddListingScreen(
            onBack = { showAddListing = false },
            onSubmit = { form ->
                viewModel.addListing(
                    title = form.title,
                    type = form.type,
                    rentPerMonth = form.rentPerMonth,
                    address = form.address,
                    distanceKm = form.distanceKm,
                    genderPreference = form.gender,
                    furnishing = form.furnishing,
                    amenities = form.amenities,
                    availability = form.availability,
                    photoUrls = form.photoUrls,
                    emoji = form.emoji,
                )
                showAddListing = false
            },
        )
    }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = PeersPrimaryIndigo,
    selectedLabelColor = Color.White,
)

@Composable
private fun AccommodationCard(
    listing: AccommodationListing,
    wishlisted: Boolean,
    onWishlist: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .background(
                    Brush.linearGradient(
                        listOf(listing.accent, MaterialTheme.colorScheme.surface)
                    )
                ),
        ) {
            if (listing.photoUrls.isNotEmpty()) {
                AsyncImage(
                    model = listing.photoUrls.first(),
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    listing.emoji,
                    fontSize = 56.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            if (listing.isVerified) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PeersMint,
                    modifier = Modifier.padding(12.dp),
                ) {
                    Text(
                        "✓ Verified by ${listing.verifiedCount} students",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F6B3B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            ) {
                IconButton(onClick = onWishlist, modifier = Modifier.size(38.dp)) {
                    Icon(
                        if (wishlisted) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (wishlisted) "Remove from wishlist" else "Save to wishlist",
                        tint = if (wishlisted) Color(0xFFE0556B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Icon(
                        Icons.Rounded.Place,
                        contentDescription = null,
                        tint = PeersPrimaryIndigo,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        " ${listing.distanceLabel} from campus",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    listing.rentLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PeersPrimaryIndigo,
                )
            }
            Text(
                listing.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp),
            ) {
                MetaPill(listing.type)
                MetaPill(listing.furnishing)
                MetaPill(listing.genderPreference)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp),
            ) {
                if (listing.reviewCount > 0) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFF2B33D),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        " ${listing.rating} (${listing.reviewCount})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        "New · no reviews yet",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "View details",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PeersPrimaryIndigo,
                )
            }
        }
    }
}

@Composable
private fun MetaPill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun EmptyListings(onReset: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(32.dp),
    ) {
        Text("🏠", fontSize = 44.sp)
        Text(
            "No stays match those filters yet.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp),
        )
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
            modifier = Modifier.padding(top = 14.dp),
        ) {
            Text("Clear filters")
        }
    }
}

@Composable
private fun FilterSheet(
    current: AccommodationFilters,
    onApply: (AccommodationFilters) -> Unit,
    onReset: () -> Unit,
) {
    var draft by remember(current) { mutableStateOf(current) }
    var maxRent by remember(current) { mutableStateOf((current.maxRent ?: 30000).toFloat()) }
    var maxDistance by remember(current) { mutableStateOf((current.maxDistanceKm ?: 10.0).toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Filters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {
                onReset()
                draft = AccommodationFilters(query = current.query)
                maxRent = 30000f
                maxDistance = 10f
            }) { Text("Reset", color = PeersPrimaryIndigo) }
        }

        FilterLabel("Maximum rent")
        Text(
            "₹${maxRent.roundToInt()} / month",
            style = MaterialTheme.typography.bodyMedium,
            color = PeersPrimaryIndigo,
            fontWeight = FontWeight.SemiBold,
        )
        Slider(
            value = maxRent,
            onValueChange = { maxRent = it },
            valueRange = 2000f..30000f,
            steps = 27,
        )

        FilterLabel("Distance from campus")
        Text(
            "Within ${"%.1f".format(maxDistance)} km",
            style = MaterialTheme.typography.bodyMedium,
            color = PeersPrimaryIndigo,
            fontWeight = FontWeight.SemiBold,
        )
        Slider(
            value = maxDistance,
            onValueChange = { maxDistance = it },
            valueRange = 0.5f..10f,
        )

        FilterLabel("Property type")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(accommodationTypes) { type ->
                FilterChip(
                    selected = type in draft.types,
                    onClick = {
                        draft = draft.copy(
                            types = if (type in draft.types) draft.types - type else draft.types + type
                        )
                    },
                    label = { Text(type) },
                    colors = filterChipColors(),
                )
            }
        }

        FilterLabel("Gender preference")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(genderOptions) { gender ->
                FilterChip(
                    selected = draft.gender == gender,
                    onClick = { draft = draft.copy(gender = if (draft.gender == gender) null else gender) },
                    label = { Text(gender) },
                    colors = filterChipColors(),
                )
            }
        }

        FilterLabel("Furnishing")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(furnishingOptions) { option ->
                FilterChip(
                    selected = draft.furnishing == option,
                    onClick = {
                        draft = draft.copy(furnishing = if (draft.furnishing == option) null else option)
                    },
                    label = { Text(option) },
                    colors = filterChipColors(),
                )
            }
        }

        FilterLabel("Availability")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = draft.availableNow,
                    onClick = { draft = draft.copy(availableNow = !draft.availableNow) },
                    label = { Text("Available now") },
                    colors = filterChipColors(),
                )
            }
        }

        FilterLabel("Amenities")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(amenityOptions) { amenity ->
                FilterChip(
                    selected = amenity in draft.amenities,
                    onClick = {
                        draft = draft.copy(
                            amenities = if (amenity in draft.amenities) draft.amenities - amenity
                            else draft.amenities + amenity
                        )
                    },
                    label = { Text(amenity) },
                    colors = filterChipColors(),
                )
            }
        }

        Button(
            onClick = {
                onApply(
                    draft.copy(
                        maxRent = if (maxRent >= 30000f) null else maxRent.roundToInt(),
                        maxDistanceKm = if (maxDistance >= 10f) null else maxDistance.toDouble(),
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(52.dp),
        ) {
            Text("Show results", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilterLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp),
    )
}
