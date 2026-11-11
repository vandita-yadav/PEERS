package com.example.peers.features.profile.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.profile.ProfileSetupState
import com.example.peers.features.profile.ProfileSetupViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun InterestsStep(state: ProfileSetupState, viewModel: ProfileSetupViewModel) {

    // The exact categories and items from your documentation
    val categorizedInterests = mapOf(
        "📚 LEARNING & CAREER" to listOf("Academics & Study", "Technology", "Career & Jobs", "Entrepreneurship", "Freelancing", "Content Writing", "Content Creation"),
        "🎨 CREATIVE" to listOf("Music", "Arts & Creativity", "Photography", "Filmography", "Dance", "Vlogging"),
        "🏃 LIFESTYLE & SPORTS" to listOf("Sports", "Gym & Fitness", "Martial Arts", "Travel", "Food & Cooking"),
        "🎮 ENTERTAINMENT" to listOf("Gaming", "Books", "Movies & Entertainment"),
        "🌍 SOCIAL & COMMUNITY" to listOf("Culture & Languages", "Volunteering", "Social Events")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp), // Bottom padding for the Continue button
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("What Do You Care About?", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PeersInkText)
            Text("Personalise your feed based on your values (Select min 3)", color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
        }

        categorizedInterests.forEach { (category, items) ->
            // 1. The Sticky Header
            stickyHeader {
                Text(
                    text = category,
                    fontWeight = FontWeight.Bold,
                    color = PeersIndigoLight,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAF8F4).copy(alpha = 0.95f)) // Matches LightBackground
                        .padding(vertical = 8.dp)
                )
            }

            // 2. The wrapping card grid
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items.forEach { interest ->
                        SelectableCard(
                            text = interest,
                            isSelected = state.selectedInterests.contains(interest),
                            onClick = { viewModel.toggleInterest(interest) }
                        )
                    }
                }
            }
        }
    }
}