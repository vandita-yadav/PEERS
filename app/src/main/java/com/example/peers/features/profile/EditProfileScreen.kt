package com.example.peers.features.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
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
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AddPhotoAlternate
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryScrollableTabRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.peers.R
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.profile.components.ImageCropEditorDialog
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.PeersDrawableIcon
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun createEditCameraPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(dir, "peers_edit_profile_$stamp.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private val allInterestsList = ProfileOptions.allInterests
private val allNeedsList = ProfileOptions.needs
private val degreeOptions = ProfileOptions.degreeLevels
private val yearOptions = ProfileOptions.studyYears
private val universities = ProfileOptions.universities

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
    val initialAvatar = PeersAvatars.normalizePath(user?.avatarIndex)
    var selectedAvatar by remember { mutableStateOf(initialAvatar) }
    var photoUrl by remember { mutableStateOf(user?.photoUrl.orEmpty()) }
    var selectedCategory by remember { mutableStateOf("Neutral") }
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
    var currentState by remember { mutableStateOf(user?.currentState.orEmpty()) }
    var currentCity by remember { mutableStateOf(user?.city.orEmpty()) }
    var phoneCountryCode by remember { mutableStateOf(user?.phoneCountryCode.orEmpty().ifBlank { "+91" }) }
    var phoneNumber by remember { mutableStateOf(user?.phoneNumber.orEmpty()) }
    var bio by remember { mutableStateOf(user?.bio.orEmpty()) }
    var selectedLanguages by remember { mutableStateOf(user?.languagesKnown.orEmpty().toSet()) }
    var selectedInterests by remember { mutableStateOf(user?.interests.orEmpty().toSet()) }
    var selectedNeeds by remember {
        mutableStateOf(
            user?.needs.orEmpty().map { saved ->
                allNeedsList.firstOrNull { opt -> opt == saved || opt.contains(saved, ignoreCase = true) } ?: saved
            }.toSet()
        )
    }

    var isSaving by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var rawPhotoForEditor by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) rawPhotoForEditor = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { rawPhotoForEditor = it }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createEditCameraPhotoUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission is required to take a photo.", Toast.LENGTH_SHORT).show()
        }
    }

    rawPhotoForEditor?.let { uriToCrop ->
        ImageCropEditorDialog(
            imageUri = uriToCrop,
            onCancel = { rawPhotoForEditor = null },
            onSave = { _, dataUri ->
                photoUrl = dataUri
                rawPhotoForEditor = null
            },
        )
    }

    // Check if any modification exists
    val hasChanges = name != (user?.name.orEmpty()) ||
            selectedAvatar != initialAvatar ||
            photoUrl != (user?.photoUrl.orEmpty()) ||
            university != (user?.university.orEmpty()) ||
            course != (user?.course.orEmpty()) ||
            degreeLevel != (user?.degreeLevel.orEmpty()) ||
            yearOfStudy != (user?.year.orEmpty()) ||
            expectedGraduationYear != (user?.expectedGraduationYear.orEmpty()) ||
            countryOfOrigin != (user?.countryOfOrigin.orEmpty()) ||
            currentCountry != (user?.currentCountry.orEmpty()) ||
            currentState != (user?.currentState.orEmpty()) ||
            currentCity != (user?.city.orEmpty()) ||
            phoneCountryCode != (user?.phoneCountryCode.orEmpty().ifBlank { "+91" }) ||
            phoneNumber != (user?.phoneNumber.orEmpty()) ||
            bio != (user?.bio.orEmpty()) ||
            selectedLanguages != (user?.languagesKnown.orEmpty().toSet()) ||
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
                                    "photoUrl" to photoUrl,
                                    "university" to finalUni,
                                    "course" to course.trim(),
                                    "degreeLevel" to degreeLevel,
                                    "yearOfStudy" to yearOfStudy,
                                    "expectedGraduationYear" to expectedGraduationYear.trim(),
                                    "countryOfOrigin" to countryOfOrigin.trim(),
                                    "currentCountry" to currentCountry.trim(),
                                    "currentState" to currentState.trim(),
                                    "currentCity" to currentCity.trim(),
                                    "phoneCountryCode" to phoneCountryCode.trim(),
                                    "phoneNumber" to phoneNumber.trim(),
                                    "bio" to bio.trim(),
                                    "languagesKnown" to selectedLanguages.toList(),
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
            EditSectionTitle("PROFILE AVATAR")
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
                        avatarIndex = if (photoUrl.isBlank()) selectedAvatar else null,
                        photoUrl = photoUrl.takeIf { it.isNotBlank() },
                        size = 84.dp,
                        borderWidth = 3.dp,
                        borderColor = PeersPrimaryIndigo,
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "Change Profile Picture",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PeersPrimaryIndigo,
                        modifier = Modifier.align(Alignment.Start),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                        PackageManager.PERMISSION_GRANTED
                                    ) {
                                        val uri = createEditCameraPhotoUri(context)
                                        pendingCameraUri = uri
                                        cameraLauncher.launch(uri)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 11.dp, horizontal = 8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddAPhoto,
                                    contentDescription = "Take Photo",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Take Photo",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 11.dp, horizontal = 8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = "Upload Photo",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Upload Photo",
                                    fontFamily = GoogleSans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "Choose from Avatar Categories:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PeersPrimaryIndigo,
                        modifier = Modifier.align(Alignment.Start),
                    )

                    Spacer(Modifier.height(8.dp))

                    // Category Filter Chips
                    PrimaryScrollableTabRow(
                        selectedTabIndex = PeersAvatars.categories.indexOf(selectedCategory).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        indicator = {},
                        divider = {}
                    ) {
                        PeersAvatars.categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PeersPrimaryIndigo,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 4-column grid of avatars for selected category
                    val categoryAvatars = PeersAvatars.getAvatarsForCategory(selectedCategory)
                    val rows = categoryAvatars.chunked(4)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                            ) {
                                rowItems.forEach { assetPath ->
                                    val isChosen = photoUrl.isBlank() && selectedAvatar == assetPath
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(
                                                width = if (isChosen) 3.dp else 1.dp,
                                                color = if (isChosen) PeersPrimaryIndigo else MaterialTheme.colorScheme.outlineVariant,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedAvatar = assetPath
                                                photoUrl = ""
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = "file:///android_asset/avatars/$assetPath",
                                            contentDescription = assetPath,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                        )
                                        if (isChosen) {
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(PeersPrimaryIndigo.copy(alpha = 0.25f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                repeat(4 - rowItems.size) {
                                    Spacer(modifier = Modifier.size(54.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Section 2: Basic Information ──────────────────────────────────
            EditSectionTitle("BASIC INFORMATION", R.drawable.notes_black, R.drawable.notes_white)
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
                            value = currentState,
                            onValueChange = { currentState = it },
                            label = { Text("Current State") },
                            placeholder = { Text("e.g. Punjab") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = currentCity,
                            onValueChange = { currentCity = it },
                            label = { Text("Current City") },
                            placeholder = { Text("e.g. Phagwara") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = currentCountry,
                            onValueChange = { currentCountry = it },
                            label = { Text("Current Country") },
                            placeholder = { Text("e.g. India") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = countryOfOrigin,
                            onValueChange = { countryOfOrigin = it },
                            label = { Text("Country of Origin") },
                            placeholder = { Text("e.g. India") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = phoneCountryCode,
                            onValueChange = { phoneCountryCode = it },
                            label = { Text("Code") },
                            placeholder = { Text("+91") },
                            modifier = Modifier.weight(0.32f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it.filter(Char::isDigit).take(15) },
                            label = { Text("Phone Number") },
                            placeholder = { Text("9876543210") },
                            modifier = Modifier.weight(0.68f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                        )
                    }

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
            EditSectionTitle("INTERESTS (SELECT MIN 3)", R.drawable.stars_black, R.drawable.stars_white)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "Selected: ${selectedInterests.size} ${if (selectedInterests.size >= 3) "(Ready)" else "(Need at least 3)"}",
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
            EditSectionTitle("WHAT YOU NEED FROM PEERS (SELECT MIN 2)", R.drawable.handshake_black, R.drawable.handshake_white)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "Selected: ${selectedNeeds.size} ${if (selectedNeeds.size >= 2) "(Ready)" else "(Need at least 2)"}",
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

            Spacer(Modifier.height(20.dp))

            // ── Section 5: Languages Known ───────────────────────────────────
            EditSectionTitle("LANGUAGES KNOWN (OPTIONAL)", R.drawable.community_black, R.drawable.community_white)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                ) {
                    ProfileOptions.languages.forEach { lang ->
                        val isSelected = selectedLanguages.contains(lang)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) PeersPrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                selectedLanguages = if (isSelected) {
                                    selectedLanguages - lang
                                } else {
                                    selectedLanguages + lang
                                }
                            },
                        ) {
                            Text(
                                lang,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            )
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
private fun EditSectionTitle(
    title: String,
    @DrawableRes lightIconRes: Int? = null,
    @DrawableRes darkIconRes: Int? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
    ) {
        if (lightIconRes != null && darkIconRes != null) {
            PeersDrawableIcon(
                lightRes = lightIconRes,
                darkRes = darkIconRes,
                contentDescription = title,
                size = 15.dp,
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
    }
}
