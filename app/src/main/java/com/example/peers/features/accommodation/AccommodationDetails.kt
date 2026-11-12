package com.example.peers.features.accommodation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky

@Composable
fun AccommodationDetails(
    listing: AccommodationListing,
    wishlisted: Boolean,
    isVerifiedByMe: Boolean,
    isReviewed: Boolean,
    reviews: List<AccommodationReview>,
    onWishlist: () -> Unit,
    onMarkGenuine: () -> Unit,
    onRemoveGenuine: () -> Unit,
    onAddReview: (Int, String) -> Unit,
    onReportReview: (String) -> Unit,
    onRoommateRequest: (ContactInfo, String) -> Unit,
) {
    val context = LocalContext.current
    var showEnquiry by remember { mutableStateOf(false) }
    var enquirySent by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showRoommateDialog by remember { mutableStateOf(false) }
    var roommateSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
    ) {
        PhotoCarousel(listing)

        Column(Modifier.padding(horizontal = 22.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        listing.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        listing.rentLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PeersPrimaryIndigo,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                IconButton(onClick = onWishlist) {
                    Icon(
                        if (wishlisted) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (wishlisted) Color(0xFFE0556B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Icon(
                    Icons.Rounded.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    " ${listing.address} · ${listing.distanceLabel} from campus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                if (listing.reviewCount > 0) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFF2B33D),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        " ${listing.rating} · ${listing.reviewCount} reviews",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        "New listing · no reviews yet",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        listing.availability,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            if (listing.isVerified) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PeersMint,
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                ) {
                    Text(
                        "✓ Marked genuine by ${listing.verifiedCount} students",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F6B3B),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                if (isVerifiedByMe) {
                    Text(
                        "✓ You marked this stay genuine",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1F6B3B),
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onRemoveGenuine) {
                        Text("Remove", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    FilledTonalButton(onClick = onMarkGenuine) {
                        Text("Mark as Genuine")
                    }
                    Text(
                        "  One per student",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            DetailSection("About this stay")
            Text(
                "${listing.type} · ${listing.furnishing} · ${listing.genderPreference.lowercase()} friendly. " +
                    "Contact the contributor to arrange a visit — no booking or payment happens in PEERS.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            DetailSection("Amenities")
            FlowRowChips(listing.amenities)

            DetailSection("Reviews")
            if (reviews.isEmpty()) {
                Text(
                    "No reviews yet — be the first to share your experience.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                reviews.take(3).forEach { review ->
                    ReviewRow(review = review, onReport = { onReportReview(review.id) })
                }
            }
            if (isReviewed) {
                Text(
                    "You've reviewed this stay. Thanks!",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF1F6B3B),
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                OutlinedButton(
                    onClick = { showReviewDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    Icon(Icons.Rounded.StarBorder, null, Modifier.size(18.dp))
                    Text("  Write a review")
                }
            }

            DetailSection("Roommate")
            Text(
                "Looking for someone to share this stay? Send a request with only the contact details you choose to share.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { showRoommateDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                modifier = Modifier.padding(top = 10.dp),
            ) {
                Text("Looking for a roommate", fontWeight = FontWeight.SemiBold)
            }
            if (roommateSent) {
                Text(
                    "Request sent! You'll be notified when they respond.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1F6B3B),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            DetailSection("Location")
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().height(110.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Rounded.Map,
                        contentDescription = null,
                        tint = PeersPrimaryIndigo,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        "Map preview",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "Add a Maps API key to enable the interactive map",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            DetailSection("Contributor")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PeersLavender),
                ) {
                    Text("🧑🏽", fontSize = 20.sp)
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text(
                        listing.contributorName.ifBlank { "PEERS student" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Listing contributor",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 20.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        val query = Uri.encode("${listing.title}, ${listing.address}")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query"))
                        runCatching { context.startActivity(intent) }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                ) {
                    Icon(Icons.Rounded.Map, null, Modifier.size(18.dp))
                    Text("  Open in Maps")
                }
                Button(
                    onClick = { showEnquiry = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                    modifier = Modifier.weight(1f).height(50.dp),
                ) {
                    Text("Enquire", fontWeight = FontWeight.Bold)
                }
            }

            if (enquirySent) {
                Text(
                    "Enquiry sent! ${listing.contributorName.ifBlank { "The contributor" }} will see your message.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1F6B3B),
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }

    if (showEnquiry) {
        var message by remember {
            mutableStateOf("Hi! I'm interested in ${listing.title}. Is it still available?")
        }
        AlertDialog(
            onDismissRequest = { showEnquiry = false },
            title = { Text("Send an enquiry") },
            text = {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showEnquiry = false
                    enquirySent = true
                }) { Text("Send", color = PeersPrimaryIndigo) }
            },
            dismissButton = {
                TextButton(onClick = { showEnquiry = false }) { Text("Cancel") }
            },
        )
    }

    if (showReviewDialog) {
        var rating by remember { mutableIntStateOf(0) }
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Write a review") },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    if (star <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) Color(0xFFF2B33D)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("How was your stay?") },
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddReview(rating, text)
                        showReviewDialog = false
                    },
                    enabled = rating > 0 && text.isNotBlank(),
                ) { Text("Post review", color = PeersPrimaryIndigo) }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showRoommateDialog) {
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var instagram by remember { mutableStateOf("") }
        var snapchat by remember { mutableStateOf("") }
        var message by remember {
            mutableStateOf("Hi! I'm interested in sharing ${listing.title}. Is it still available?")
        }
        val contact = ContactInfo(phone, email, instagram, snapchat)
        AlertDialog(
            onDismissRequest = { showRoommateDialog = false },
            title = { Text("Looking for a roommate") },
            text = {
                Column {
                    Text(
                        "Share only what you're comfortable with. This is sent to the listing contributor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone (optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    OutlinedTextField(
                        value = instagram,
                        onValueChange = { instagram = it },
                        label = { Text("Instagram (optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    OutlinedTextField(
                        value = snapchat,
                        onValueChange = { snapchat = it },
                        label = { Text("Snapchat (optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRoommateRequest(contact, message)
                        roommateSent = true
                        showRoommateDialog = false
                    },
                    enabled = !contact.isEmpty && message.isNotBlank(),
                ) { Text("Send request", color = PeersPrimaryIndigo) }
            },
            dismissButton = {
                TextButton(onClick = { showRoommateDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ReviewRow(review: AccommodationReview, onReport: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    review.authorName.ifBlank { "Student" },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Row {
                    repeat(5) { index ->
                        Icon(
                            if (index < review.rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFF2B33D),
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
            }
            if (review.text.isNotBlank()) {
                Text(
                    review.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (review.reported) {
                Text(
                    "Reported",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else if (!review.isMine) {
                TextButton(
                    onClick = onReport,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Report", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun PhotoCarousel(listing: AccommodationListing) {
    if (listing.photoUrls.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(Brush.linearGradient(listOf(listing.accent, PeersSky))),
        ) {
            Text(listing.emoji, fontSize = 72.sp)
        }
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 8.dp),
    ) {
        itemsIndexed(listing.photoUrls) { _, url ->
            AsyncImage(
                model = url,
                contentDescription = listing.title,
                modifier = Modifier
                    .size(width = 300.dp, height = 190.dp)
                    .clip(RoundedCornerShape(20.dp)),
            )
        }
    }
}

@Composable
private fun DetailSection(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun FlowRowChips(items: List<String>) {
    if (items.isEmpty()) {
        Text(
            "No amenities listed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    // Simple wrapping rows keep the sheet compact without an experimental FlowRow API.
    items.chunked(3).forEach { row ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            row.forEach { amenity ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        amenity,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}
