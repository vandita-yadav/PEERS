package com.example.peers.features.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.profile.ProfileOptions
import com.example.peers.features.profile.ProfileSetupState
import com.example.peers.features.profile.ProfileSetupViewModel
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersPrimaryIndigo

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BasicInformationStep(state: ProfileSetupState, viewModel: ProfileSetupViewModel) {
    var activePicker by remember { mutableStateOf<String?>(null) }
    var showLanguagesSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Tell Us About You",
            fontFamily = GoogleSans,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Help peers get to know you and personalize your campus feed.",
            fontFamily = Lato,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // 1. Country of Origin (Required - Searchable Dropdown)
        SearchableSelectorField(
            label = "Country of Origin *",
            value = state.countryOfOrigin,
            placeholder = "Select your home country",
            onClick = { activePicker = "origin_country" }
        )

        // 2. Current Country (Required - Searchable Dropdown)
        SearchableSelectorField(
            label = "Current Country *",
            value = state.currentCountry,
            placeholder = "Select country you currently study in",
            onClick = { activePicker = "current_country" }
        )

        // 3. Current State / Province (Required - Cascading by Country)
        SearchableSelectorField(
            label = "Current State / Province *",
            value = state.currentState,
            placeholder = if (state.currentCountry.isBlank()) "Select Current Country first"
            else "Select or enter your state (e.g. Punjab, Uttar Pradesh)",
            onClick = { activePicker = "current_state" }
        )

        // 4. Current City (Required - Cascading by State)
        SearchableSelectorField(
            label = "Current City *",
            value = state.currentCity,
            placeholder = if (state.currentState.isBlank()) "Select Current State first"
            else "Select or enter your current city",
            onClick = { activePicker = "current_city" }
        )

        // 5. University / College (Strictly scoped to Current Country, city prioritized)
        SearchableSelectorField(
            label = "University / College / Institute *",
            value = state.university,
            placeholder = if (state.currentCountry.isBlank()) "Select Current Country first"
            else "Universities & institutes in ${state.currentCountry}",
            onClick = { activePicker = "university" }
        )
        AnimatedVisibility(visible = state.university == "Other") {
            OutlinedTextField(
                value = state.customUniversity,
                onValueChange = { viewModel.updateCustomUniversity(it) },
                label = { Text("Enter University / College Name *", fontFamily = GoogleSans) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = onboardingTextFieldColors()
            )
        }

        // 6. Degree (Required - Mapped to standard program duration)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Degree *",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileOptions.degreeLevels.forEach { degree ->
                    val selected = state.degreeLevel == degree
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) PeersPrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(
                            1.dp,
                            if (selected) PeersPrimaryIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { viewModel.updateDegreeLevel(degree) }
                    ) {
                        Text(
                            text = degree,
                            fontFamily = GoogleSans,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
            if (state.degreeLevel.isNotBlank() && state.degreeDurationYears > 0) {
                Text(
                    text = "Mapped degree duration: ${state.degreeDurationYears} year(s)",
                    fontFamily = Lato,
                    fontSize = 12.5.sp,
                    color = PeersPrimaryIndigo,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // 7. Domain / Specialization (Required)
        SearchableSelectorField(
            label = "Domain / Specialization *",
            value = state.course,
            placeholder = "e.g. Computer Science, IT, Food Technology, Performing Arts",
            onClick = { activePicker = "course" }
        )
        AnimatedVisibility(visible = state.course == "Other") {
            OutlinedTextField(
                value = state.customCourse,
                onValueChange = { viewModel.updateCustomCourse(it) },
                label = { Text("Enter Domain / Specialization *", fontFamily = GoogleSans) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = onboardingTextFieldColors()
            )
        }

        // 8. Degree Commencing Month & Year -> Auto-calculates Graduation Year & Study Year
        Text(
            text = "Degree Commencing *",
            fontFamily = GoogleSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SimpleDropdownField(
                label = "Commencing Month *",
                value = state.commencingMonth,
                options = ProfileOptions.commencingMonths,
                onSelect = { viewModel.updateCommencingMonth(it) },
                modifier = Modifier.weight(1f)
            )
            SimpleDropdownField(
                label = "Commencing Year *",
                value = state.commencingYear,
                options = ProfileOptions.commencingYears,
                onSelect = { viewModel.updateCommencingYear(it) },
                modifier = Modifier.weight(1f)
            )
        }

        // Auto-calculated Graduation Year summary + adjustable Current Study Year
        if (state.expectedGraduationYear.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PeersPrimaryIndigo.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Calculated Graduation Year",
                        fontFamily = Lato,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${state.expectedGraduationYear} (${state.degreeDurationYears} yrs)",
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PeersPrimaryIndigo
                    )
                }
            }
        }

        SimpleDropdownField(
            label = "Current Year of Study *",
            value = state.yearOfStudy,
            options = ProfileOptions.studyYears,
            onSelect = { viewModel.updateYearOfStudy(it) },
            modifier = Modifier.fillMaxWidth()
        )

        // 9. Phone Number with Country Code (for international & local students)
        Text(
            text = "Phone Number (Optional)",
            fontFamily = GoogleSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.36f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { activePicker = "dial_code" }
            ) {
                OutlinedTextField(
                    value = state.phoneCountryCode,
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    label = { Text("Code", fontFamily = GoogleSans, fontSize = 12.sp) },
                    trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        disabledBorderColor = PeersPrimaryIndigo.copy(alpha = 0.45f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
            }
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = { viewModel.updatePhoneNumber(it) },
                label = { Text("Mobile Number", fontFamily = GoogleSans) },
                placeholder = { Text("9876543210", fontFamily = Lato) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(0.64f),
                colors = onboardingTextFieldColors()
            )
        }

        // 9. Bio / About Me (Optional)
        OutlinedTextField(
            value = state.bio,
            onValueChange = { if (it.length <= 300) viewModel.updateBio(it) },
            label = { Text("Bio / About Me (Optional)", fontFamily = GoogleSans) },
            placeholder = { Text("Tell peers about your interests, hometown, or goals...", fontFamily = Lato) },
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp),
            maxLines = 4,
            shape = RoundedCornerShape(14.dp),
            colors = onboardingTextFieldColors()
        )

        // 10. Languages Known (Optional - Multi-select)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Languages Known (Optional)",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { showLanguagesSheet = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = PeersPrimaryIndigo)
                    Spacer(Modifier.width(4.dp))
                    Text("Add / Edit", fontFamily = GoogleSans, color = PeersPrimaryIndigo, fontSize = 13.sp)
                }
            }
            if (state.languagesKnown.isEmpty()) {
                val quickLanguages = listOf("English", "Hindi", "Punjabi", "Spanish", "French", "German")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickLanguages.forEach { lang ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { viewModel.toggleLanguage(lang) }
                        ) {
                            Text(
                                text = "+ $lang",
                                fontFamily = Lato,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.languagesKnown.forEach { lang ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PeersPrimaryIndigo.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, PeersPrimaryIndigo),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { viewModel.toggleLanguage(lang) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = lang,
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Remove $lang",
                                    tint = PeersPrimaryIndigo,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ── Searchable Picker Bottom Sheet ────────────────────────────────────────
    activePicker?.let { key ->
        val (title, options, allowCustom) = when (key) {
            "origin_country" -> Triple("Select Country of Origin", ProfileOptions.countries, false)
            "current_country" -> Triple("Select Current Country", ProfileOptions.countries, false)
            "current_state" -> Triple(
                "Select Current State / Province",
                ProfileOptions.statesForCountry(state.currentCountry),
                true
            )
            "current_city" -> Triple(
                "Select Current City",
                ProfileOptions.citiesForState(state.currentCountry, state.currentState),
                true
            )
            "university" -> Triple(
                "Universities in ${state.currentCountry.ifBlank { "Selected Country" }}",
                ProfileOptions.universitiesForLocation(state.currentCountry, state.currentCity),
                false
            )
            "dial_code" -> Triple(
                "Select Country Dial Code",
                ProfileOptions.dialCodes.map { it.displayLabel },
                false
            )
            else -> Triple("Select Domain / Specialization", ProfileOptions.courses, false)
        }
        SearchableListBottomSheet(
            title = title,
            options = options,
            selected = when (key) {
                "origin_country" -> state.countryOfOrigin
                "current_country" -> state.currentCountry
                "current_state" -> state.currentState
                "current_city" -> state.currentCity
                "university" -> state.university
                "dial_code" -> ProfileOptions.dialCodes.firstOrNull { it.code == state.phoneCountryCode }?.displayLabel
                    ?: state.phoneCountryCode
                else -> state.course
            },
            allowCustom = allowCustom,
            onSelect = { chosen ->
                when (key) {
                    "origin_country" -> viewModel.updateCountryOfOrigin(chosen)
                    "current_country" -> viewModel.updateCurrentCountry(chosen)
                    "current_state" -> viewModel.updateCurrentState(chosen)
                    "current_city" -> viewModel.updateCurrentCity(chosen)
                    "university" -> viewModel.updateUniversity(chosen)
                    "dial_code" -> viewModel.updatePhoneCountryCode(chosen.substringBefore(" ").trim())
                    "course" -> viewModel.updateCourse(chosen)
                }
                activePicker = null
            },
            onDismiss = { activePicker = null }
        )
    }

    // ── Multi-Select Languages Sheet ──────────────────────────────────────────
    if (showLanguagesSheet) {
        MultiSelectLanguagesSheet(
            selectedLanguages = state.languagesKnown,
            onToggle = { viewModel.toggleLanguage(it) },
            onDismiss = { showLanguagesSheet = false }
        )
    }
}

@Composable
private fun onboardingTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    focusedBorderColor = PeersPrimaryIndigo,
    unfocusedBorderColor = Color.Transparent,
    focusedLabelColor = PeersPrimaryIndigo,
)

@Composable
private fun SearchableSelectorField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            enabled = false,
            readOnly = true,
            label = { Text(label, fontFamily = GoogleSans) },
            placeholder = { Text(placeholder, fontFamily = Lato) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                disabledBorderColor = if (value.isNotBlank()) PeersPrimaryIndigo.copy(alpha = 0.45f) else Color.Transparent,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = GoogleSans, fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            colors = onboardingTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt, fontFamily = Lato) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchableListBottomSheet(
    title: String,
    options: List<String>,
    selected: String,
    allowCustom: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, options) {
        if (query.isBlank()) options
        else options.filter { it.contains(query.trim(), ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = title,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search...", fontFamily = Lato) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = PeersPrimaryIndigo) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = onboardingTextFieldColors()
            )
            Spacer(Modifier.height(10.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (allowCustom && query.trim().isNotBlank() && filtered.none { it.equals(query.trim(), ignoreCase = true) }) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(query.trim()) }
                                .padding(vertical = 14.dp, horizontal = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = PeersPrimaryIndigo)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Use \"${query.trim()}\"",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.SemiBold,
                                color = PeersPrimaryIndigo
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
                items(filtered) { item ->
                    val isChosen = item == selected
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                            .padding(vertical = 14.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = item,
                            fontFamily = if (isChosen) GoogleSans else Lato,
                            fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isChosen) PeersPrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isChosen) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = PeersPrimaryIndigo, modifier = Modifier.size(18.dp))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiSelectLanguagesSheet(
    selectedLanguages: List<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) ProfileOptions.languages
        else ProfileOptions.languages.filter { it.contains(query.trim(), ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Select Languages Known",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                TextButton(onClick = onDismiss) {
                    Text("Done", fontFamily = GoogleSans, fontWeight = FontWeight.Bold, color = PeersPrimaryIndigo)
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search or type a language...", fontFamily = Lato) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = PeersPrimaryIndigo) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = onboardingTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (query.trim().isNotBlank() && filtered.none { it.equals(query.trim(), ignoreCase = true) }) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onToggle(query.trim())
                                    query = ""
                                }
                                .padding(vertical = 14.dp, horizontal = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = PeersPrimaryIndigo)
                            Spacer(Modifier.width(10.dp))
                            Text("Add \"${query.trim()}\"", fontFamily = GoogleSans, color = PeersPrimaryIndigo)
                        }
                    }
                }
                items(filtered) { lang ->
                    val chosen = selectedLanguages.contains(lang)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(lang) }
                            .padding(vertical = 14.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = lang,
                            fontFamily = if (chosen) GoogleSans else Lato,
                            fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (chosen) PeersPrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (chosen) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = PeersPrimaryIndigo)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}
