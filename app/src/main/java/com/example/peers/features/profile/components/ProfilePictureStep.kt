package com.example.peers.features.profile.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.peers.features.profile.ProfileSetupState
import com.example.peers.features.profile.ProfileSetupViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val PeersIndigoAccent = Color(0xFF9FA1FF)
val PeersSkyBlue = Color(0xFFAEE2FF)

// Brand palette used for the avatar circle backgrounds (alternating per docs).
private val avatarColors = listOf(
    Color(0xFF9FA1FF), // PeersIndigo
    Color(0xFFB5BAFF), // PeersLavender
    Color(0xFFAEE2FF), // PeersSky
    Color(0xFFD9F9DF), // PeersMint
)

// Placeholder labels until the 13 vector avatars are drawn.
private val avatarLabels = listOf(
    "🧑🏿", "👩🏿", "👦🏽", "👧🏼", "👩🏽", "🧔🏽", "🧕🏾",
    "🧑🏻", "👩🏻", "🧑🏻‍🦱", "👩🏻‍🦱", "👩🏽‍🦱", "🧑"
)

private fun createCameraPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(dir, "peers_profile_$stamp.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
fun ProfilePictureStep(state: ProfileSetupState, viewModel: ProfileSetupViewModel) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery picker (Photo Picker — no storage permission required)
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateProfileImage(uri.toString())
        }
    }

    // Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { viewModel.updateProfileImage(it.toString()) }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Profile Picture", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Personalise your feed and build community trust.", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Image Circle (Preview): uploaded photo takes precedence, then avatar
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.profileImageUri != null -> {
                    AsyncImage(
                        model = state.profileImageUri,
                        contentDescription = "Profile photo preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                state.selectedAvatar != null -> {
                    val index = state.selectedAvatar!!
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(avatarColors[index % avatarColors.size]),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatarLabels[index], fontSize = 64.sp)
                    }
                }
                else -> Text("👤", fontSize = 64.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Swipeable Avatar Row
        Text("Or choose an avatar:", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(13) { index ->
                val isSelected = state.selectedAvatar == index
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(avatarColors[index % avatarColors.size])
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) Color(0xFF1B1C3A) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            viewModel.updateSelectedAvatar(if (isSelected) null else index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(avatarLabels[index], fontSize = 28.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Upload Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = {
                    val uri = createCameraPhotoUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Take Photo", color = Color.Black)
            }
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Upload Photo", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Warning
        Surface(
            color = PeersSkyBlue.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🛡️ Your personal photos are not shared with third-party applications without your permission.",
                modifier = Modifier.padding(16.dp),
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}
