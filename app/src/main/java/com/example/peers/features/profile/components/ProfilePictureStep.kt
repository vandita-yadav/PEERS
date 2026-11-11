package com.example.peers.features.profile.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.profile.ProfileSetupState
import com.example.peers.features.profile.ProfileSetupViewModel

val PeersIndigoAccent = Color(0xFF9FA1FF)
val PeersSkyBlue = Color(0xFFAEE2FF)

@Composable
fun ProfilePictureStep(state: ProfileSetupState, viewModel: ProfileSetupViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Profile Picture", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Personalise your feed and build community trust.", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Image Circle (Preview)
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            // TODO: Display uploaded photo OR selected avatar here
            Text("👤", fontSize = 64.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Swipeable Avatar Row
        Text("Or choose an avatar:", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Rendering 13 placeholder avatars
            items(13) { index ->
                val isSelected = state.selectedAvatar == index
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) PeersIndigoAccent.copy(alpha = 0.2f) else Color.White)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) PeersIndigoAccent else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable { viewModel.updateSelectedAvatar(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖") // Replace with actual avatar drawable resource
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Upload Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { /* TODO: Launch Camera */ }, modifier = Modifier.weight(1f)) {
                Text("Take Photo", color = Color.Black)
            }
            OutlinedButton(onClick = { /* TODO: Launch Gallery picker */ }, modifier = Modifier.weight(1f)) {
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