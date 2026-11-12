package com.example.peers.features.accommodation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.peers.ui.theme.PeersPrimaryIndigo

data class ListingForm(
    val title: String,
    val type: String,
    val rentPerMonth: Int,
    val address: String,
    val distanceKm: Double,
    val gender: String,
    val furnishing: String,
    val amenities: List<String>,
    val availability: String,
    val photoUrls: List<String>,
    val emoji: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingScreen(
    onBack: () -> Unit,
    onSubmit: (ListingForm) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(accommodationTypes.first()) }
    var rent by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Any") }
    var furnishing by remember { mutableStateOf(furnishingOptions.first()) }
    var amenities by remember { mutableStateOf(setOf<String>()) }
    var availability by remember { mutableStateOf("Available now") }
    var photoUrls by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(listingEmojis.first()) }

    val canSubmit = title.isNotBlank() && address.isNotBlank() && (rent.toIntOrNull() ?: 0) > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Add accommodation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Help a peer find their next home",
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            FormSection("Basics")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Listing title") },
                placeholder = { Text("e.g. Sunrise Girls PG") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            FormLabel("Property type")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accommodationTypes) { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option) },
                        colors = addChipColors(),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 14.dp),
            ) {
                OutlinedTextField(
                    value = rent,
                    onValueChange = { rent = it.filter(Char::isDigit) },
                    label = { Text("Rent / month (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = distance,
                    onValueChange = { input ->
                        distance = input.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Distance (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address / area") },
                placeholder = { Text("Law Gate, Phagwara") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            )

            FormSection("Preferences")
            FormLabel("Gender preference")
            ChipRow(genderOptions, selected = gender) { gender = it }

            FormLabel("Furnishing")
            ChipRow(furnishingOptions, selected = furnishing) { furnishing = it }

            FormLabel("Amenities")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(amenityOptions) { amenity ->
                    FilterChip(
                        selected = amenity in amenities,
                        onClick = {
                            amenities = if (amenity in amenities) amenities - amenity else amenities + amenity
                        },
                        label = { Text(amenity) },
                        colors = addChipColors(),
                    )
                }
            }

            OutlinedTextField(
                value = availability,
                onValueChange = { availability = it },
                label = { Text("Availability") },
                placeholder = { Text("Available now / From 1 Dec") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            )

            FormSection("Presentation")
            FormLabel("Cover emoji")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listingEmojis) { option ->
                    FilterChip(
                        selected = emoji == option,
                        onClick = { emoji = option },
                        label = { Text(option, style = MaterialTheme.typography.titleMedium) },
                        colors = addChipColors(),
                    )
                }
            }

            OutlinedTextField(
                value = photoUrls,
                onValueChange = { photoUrls = it },
                label = { Text("Photo URLs (optional, one per line)") },
                placeholder = { Text("https://…") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            )

            Text(
                "Listings start as pending and become active after community review. " +
                    "Never share your exact unit number publicly.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 14.dp),
            )

            Button(
                onClick = {
                    onSubmit(
                        ListingForm(
                            title = title,
                            type = type,
                            rentPerMonth = rent.toIntOrNull() ?: 0,
                            address = address,
                            distanceKm = distance.toDoubleOrNull() ?: 0.0,
                            gender = gender,
                            furnishing = furnishing,
                            amenities = amenities.toList(),
                            availability = availability.ifBlank { "Available now" },
                            photoUrls = photoUrls.lineSequence()
                                .map(String::trim)
                                .filter { it.startsWith("http") }
                                .toList(),
                            emoji = emoji,
                        )
                    )
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .height(54.dp),
            ) {
                Text("Submit listing", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun addChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = PeersPrimaryIndigo,
    selectedLabelColor = Color.White,
)

@Composable
private fun FormSection(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
    )
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
    )
}

@Composable
private fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option) },
                colors = addChipColors(),
            )
        }
    }
}
