//package com.example.peers.features.profile.components
//
//class BasicInformationStep {
//}

package com.example.peers.features.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.profile.ProfileSetupState
import com.example.peers.features.profile.ProfileSetupViewModel

val LightSurface = Color(0xFFFFFFFF)
val PeersInkText = Color(0xFF1B1C3A) // Alias to avoid naming conflicts locally

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInformationStep(state: ProfileSetupState, viewModel: ProfileSetupViewModel) {
    val mockUniversities = listOf("Lovely Professional University", "Delhi University", "VIT", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Tell Us About You", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PeersInkText)
        Text("This helps us personalize your PEERS experience.", color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

        // Example Input: Country
        OutlinedTextField(
            value = state.currentCountry,
            onValueChange = { viewModel.updateCurrentCountry(it) },
            label = { Text("Current Country") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightSurface,
                unfocusedContainerColor = LightSurface
            )
        )

        // Dropdown: University
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.university,
                onValueChange = {},
                readOnly = true,
                label = { Text("University / College") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable).fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                mockUniversities.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            viewModel.updateUniversity(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Smoothly animated "Other" custom university field
        AnimatedVisibility(visible = state.university == "Other") {
            OutlinedTextField(
                value = state.customUniversity,
                onValueChange = { viewModel.updateCustomUniversity(it) },
                label = { Text("Enter your University Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                )
            )
        }

        // Other required fields...
        OutlinedTextField(
            value = state.course,
            onValueChange = { viewModel.updateCourse(it) },
            label = { Text("Course / Program") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightSurface,
                unfocusedContainerColor = LightSurface
            )
        )

        OutlinedTextField(
            value = state.bio,
            onValueChange = { viewModel.updateBio(it) },
            label = { Text("Bio (Optional)") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LightSurface,
                unfocusedContainerColor = LightSurface
            )
        )

        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
    }
}