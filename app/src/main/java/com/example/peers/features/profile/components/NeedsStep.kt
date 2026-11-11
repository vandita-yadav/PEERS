package com.example.peers.features.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.profile.ProfileSetupState
import com.example.peers.features.profile.ProfileSetupViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NeedsStep(state: ProfileSetupState, viewModel: ProfileSetupViewModel) {

    // The exact needs from your documentation
    val needs = listOf(
        "🏠 Accommodation", "👥 Make Friends", "🎓 University Help",
        "💼 Jobs & Internships", "📄 Visa & Documents", "💰 Budget & Finance",
        "🚇 Transportation", "🏥 Healthcare", "🛒 Daily Life",
        "🎉 Events & Activities", "🗣️ Language & Culture", "🤝 Community Support"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text("What Do You Need From PEERS?", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PeersInkText, lineHeight = 34.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("This will customize your dashboard features (Select min 2)", color = Color.Gray)
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            needs.forEach { need ->
                SelectableCard(
                    text = need,
                    isSelected = state.selectedNeeds.contains(need),
                    onClick = { viewModel.toggleNeed(need) }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Space for the bottom Complete button
    }
}