package com.example.peers.features.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val allInterestsList = listOf(
    "Academics & Study", "Technology", "Career & Jobs", "Entrepreneurship", "Coding",
    "Music", "Arts & Creativity", "Photography", "Filmography", "Dance",
    "Sports", "Gym & Fitness", "Travel", "Food & Cooking", "Gaming",
    "Books & Reading", "Movies & TV", "Culture & Languages", "Volunteering"
)

private val allNeedsList = listOf(
    "🏠 Accommodation", "👥 Make Friends", "🎓 University Help",
    "💼 Jobs & Internships", "📄 Visa & Documents", "💰 Budget & Finance",
    "🚇 Transportation", "🏥 Healthcare", "🛒 Daily Life",
    "🎉 Events & Activities", "🗣️ Language & Culture", "🤝 Community Support"
)

private val degreeOptions = listOf("Bachelor's", "Master's", "PhD", "Diploma", "Certificate")
private val yearOptions = listOf("1st Year", "2nd Year", "3rd Year", "4th Year", "Final Year", "Graduate")
private val universities = listOf(
    "Lovely Professional University",
    "Delhi University",
    "VIT Vellore",
    "BITS Pilani",
    "IIT Delhi",
    "University of Toronto",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    user: DashboardUser?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State initialized from user
    var name by remember { mutableStateOf(user?.name.orEmpty()) }
    var selectedAvatar by remember { mutableStateOf(user?.avatarIndex ?: 0) }
    var university by remember {
        mutableStateOf(
            if (user?.university.isNullOrBlank() || universities.contains(user?.university))
                user?.university?.ifBlank { universities[0] } ?: universities[0]
            else "Other"
        )
    }
    var customUniversity by remember {
        mutableStateOf(if (!universities.contains(user?.university)) user?.university.orEmpty() else "")
    }
    var course by remember { mutableStateOf(user?.course.orEmpty()) }
    var degreeLevel by remember { mutableStateOf(user?.degreeLevel.orEmpty().ifBlank { degreeOptions[0] }) }
    var yearOfStudy by remember { mutableStateOf(user?.year.orEmpty().ifBlank { yearOptions[0] }) }
    var expectedGraduationYear by remember { mutableStateOf(user?.expectedGraduationYear.orEmpty()) }
    var countryOfOrigin by remember { mutableStateOf(user?.countryOfOrigin.orEmpty()) }
    var currentCountry by remember { mutableStateOf(user?.currentCountry.orEmpty()) }
    var currentCity by remember { mutableStateOf(user?.city.orEmpty()) }
    var bio by remember { mutableStateOf(user?.bio.orEmpty()) }
    var selectedInterests by remember { mutableStateOf(user?.interests.orEmpty().toSet()) }
    var selectedNeeds by remember { mutableStateOf(user?.needs.orEmpty().toSet()) }

    var isSaving by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    // Check if any modification exists
    val hasChanges = name != (user?.name.orEmpty()) ||
            selectedAvatar != (user?.avatarIndex ?: 0) ||
            university != (user?.university.orEmpty()) ||
            course != (user?.course.orEmpty()) ||
            degreeLevel != (user?.degreeLevel.orEmpty()) ||
            yearOfStudy != (user?.year.orEmpty()) ||
            expectedGraduationYear != (user?.expectedGraduationYear.orEmpty()) ||
            countryOfOrigin != (user?.countryOfOrigin.orEmpty()) ||
            currentCountry != (user?.currentCountry.orEmpty()) ||
            currentCity != (user?.city.orEmpty()) ||
            bio != (user?.bio.orEmpty()) ||
            selectedInterests != (user?.interests.orEmpty().toSet()) ||
            selectedNeeds != (user?.needs.orEmpty().toSet())

    val isFormValid = name.isNotBlank() &&
            (if (university == "Other") customUniversity.isNotBlank() else university.isNotBlank()) &&
            course.isNotBlank() &&
            selectedInterests.size >= 3 &&
            selectedNeeds.size >= 2

    fun handleBack() {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler { handleBack() }

    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
            ) {
                IconButton(onClick = { handleBack() }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    "Edit Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                Button(
                    onClick = {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                        if (!isFormValid) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill required fields (min 3 interests, min 2 needs).")
                            }
                            return@Button
                        }
                        isSaving = true
                        scope.launch {
                            try {
                                val finalUni = if (university == "Other") customUniversity.trim() else university.trim()
                                val data = mapOf(
                                    "name" to name.trim(),
                                    "avatarIndex" to selectedAvatar,
                                    "university" to finalUni,
                                    "course" to course.trim(),
                                    "degreeLevel" to degreeLevel,
                                    "yearOfStudy" to yearOfStudy,
                                    "expectedGraduationYear" to expectedGraduationYear.trim(),
                                    "countryOfOrigin" to countryOfOrigin.trim(),
                                    "currentCountry" to currentCountry.trim(),
                                    "currentCity" to currentCity.trim(),
                                    "bio" to bio.trim(),
                                    "interests" to selectedInterests.toList(),
                                    "needs" to selectedNeeds.toList(),
                                )
                                FirebaseFirestore.getInstance().collection("users").document(uid)
                                    .set(data, SetOptions.merge())
                                    .await()
                                isSaving = false
                                Toast.makeText(context, "Profile updated.", Toast.LENGTH_SHORT).show()
                                onBack()
                            } catch (e: Exception) {
                                isSaving = false
                                snackbarHostState.showSnackbar("Error saving profile: ${e.message}")
                            }
                        }
                    },
                    enabled = !isSaving && isFormValid && hasChanges,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PeersPrimaryIndigo,
                        disabledContainerColor = PeersPrimaryIndigo.copy(alpha = 0.4f),
                    ),
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            // ── Section 1: Avatar Picker ──────────────────────────────────────
            EditSectionTitle("PROFILE AVATAR", "🧑‍🎨")
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                ) {
                    PeersAvatarBadge(
                        avatarIndex = selectedAvatar,
                        photoUrl = null, // In avatar mode
                        size = 84.dp,
                        borderWidth = 3.dp,
                        borderColor = PeersPrimaryIndigo,
                    )

                    val currentDef = PeersAvatars.get(selectedAvatar)
                    Text(
                        currentDef.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        currentDef.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "Choose from 13 Predefined Avatars:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PeersPrimaryIndigo,
                        modifier = Modifier.align(Alignment.Start),
                    )

                    Spacer(Modifier.height(10.dp))

                    // 13 Avatars Grid
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        PeersAvatars.list.forEach { item ->
                            val isChosen = selectedAvatar == item.id
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isChosen) item.backgroundColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isChosen) BorderStroke(2.dp, PeersPrimaryIndigo) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clickable { selectedAvatar = item.id }
                                    .padding(2.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Text(item.emoji, fontSize = 20.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    if (isChosen) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = PeersPrimaryIndigo,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 2: Basic Information ──────────────────────────────────
            EditSectionTitle("BASIC INFORMATION", "📋")
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(18.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                    )

                    // University Dropdown
                    var uniExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = uniExpanded,
                        onExpandedChange = { uniExpanded = !uniExpanded },
                    ) {
                        OutlinedTextField(
                            value = university,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("University / College *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uniExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                        )
                        ExposedDropdownMenu(
                            expanded = uniExpanded,
                            onDismissRequest = { uniExpanded = false },
                        ) {
                            universities.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        university = option
                                        uniExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = university == "Other") {
                        OutlinedTextField(
                            value = customUniversity,
                            onValueChange = { customUniversity = it },
                            label = { Text("Enter University Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                        )
                    }

                    OutlinedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = { Text("Course / Major *") },
                        placeholder = { Text("e.g. Computer Science & Engineering") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                    )

                    // Degree Level & Year of Study Row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        var degExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = degExpanded,
                            onExpandedChange = { degExpanded = !degExpanded },
                            modifier = Modifier.weight(1f),
                        ) {
                            OutlinedTextField(
                                value = degreeLevel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Degree") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = degExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                            )
                            ExposedDropdownMenu(
                                expanded = degExpanded,
                                onDismissRequest = { degExpanded = false },
                            ) {
                                degreeOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = { degreeLevel = opt; degExpanded = false },
                                    )
                                }
                            }
                        }

                        var yrExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = yrExpanded,
                            onExpandedChange = { yrExpanded = !yrExpanded },
                            modifier = Modifier.weight(1f),
                        ) {
                            OutlinedTextField(
                                value = yearOfStudy,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Year") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yrExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                            )
                            ExposedDropdownMenu(
                                expanded = yrExpanded,
                                onDismissRequest = { yrExpanded = false },
                            ) {
                                yearOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = { yearOfStudy = opt; yrExpanded = false },
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = expectedGraduationYear,
                        onValueChange = { expectedGraduationYear = it },
                        label = { Text("Expected Graduation (Year)") },
                        placeholder = { Text("e.g. 2026") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = currentCity,
                            onValueChange = { currentCity = it },
                            label = { Text("Current City") },
                            placeholder = { Text("e.g. Phagwara / Jalandhar") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = currentCountry,
                            onValueChange = { currentCountry = it },
                            label = { Text("Current Country") },
                            placeholder = { Text("e.g. India") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                        )
                    }

                    OutlinedTextField(
                        value = countryOfOrigin,
                        onValueChange = { countryOfOrigin = it },
                        label = { Text("Country of Origin") },
                        placeholder = { Text("e.g. India") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { if (it.length <= 300) bio = it },
                        label = { Text("About Me / Bio (${bio.length}/300)") },
                        placeholder = { Text("Tell your peers about yourself, what you study and what you love...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 3,
                        maxLines = 5,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 3: Interests ──────────────────────────────────────────
            EditSectionTitle("INTERESTS (SELECT MIN 3)", "✨")
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "Selected: ${selectedInterests.size} ${if (selectedInterests.size >= 3) "✓" else "(Need at least 3)"}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedInterests.size >= 3) Color(0xFF1B6B2E) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        allInterestsList.forEach { interest ->
                            val isSelected = selectedInterests.contains(interest)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) PeersPrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    selectedInterests = if (isSelected) {
                                        selectedInterests - interest
                                    } else {
                                        selectedInterests + interest
                                    }
                                },
                            ) {
                                Text(
                                    interest,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 4: Needs ──────────────────────────────────────────────
            EditSectionTitle("WHAT YOU NEED FROM PEERS (SELECT MIN 2)", "🤝")
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "Selected: ${selectedNeeds.size} ${if (selectedNeeds.size >= 2) "✓" else "(Need at least 2)"}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedNeeds.size >= 2) Color(0xFF1B6B2E) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        allNeedsList.forEach { need ->
                            val isSelected = selectedNeeds.contains(need)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) PeersPrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    selectedNeeds = if (isSelected) {
                                        selectedNeeds - need
                                    } else {
                                        selectedNeeds + need
                                    }
                                },
                            ) {
                                Text(
                                    need,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── Discard Changes Confirmation Dialog ───────────────────────────────────
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?", fontWeight = FontWeight.Bold) },
            text = { Text("You have unsaved edits. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Discard", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            },
        )
    }
}

@Composable
private fun EditSectionTitle(title: String, emoji: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
    ) {
        Text(emoji, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
    }
}
