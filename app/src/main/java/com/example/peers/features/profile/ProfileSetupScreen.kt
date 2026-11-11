package com.example.peers.features.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.features.profile.components.BasicInformationStep
import com.example.peers.features.profile.components.InterestsStep
import com.example.peers.features.profile.components.NeedsStep
import com.example.peers.features.profile.components.ProfilePictureStep

// Colors from your PEERS theme
val LightBackground = Color(0xFFFAF8F4)
val PeersIndigo = Color(0xFF9FA1FF)
val PeersLavender = Color(0xFFB5BAFF)
val PeersInk = Color(0xFF1B1C3A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel = viewModel(),
    onProfileComplete: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val completion by viewModel.completion.collectAsState()

    // Navigate away when the profile has been saved successfully.
    LaunchedEffect(completion) {
        when (val c = completion) {
            is ProfileCompletion.Success -> {
                onProfileComplete(c.userId)
                viewModel.consumeCompletion()
            }
            is ProfileCompletion.Error -> {
                viewModel.consumeCompletion()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        if (state.currentStep > 1) {
                            IconButton(onClick = { viewModel.previousStep() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PeersInk)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBackground)
                )
                // Custom Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(if (i <= state.currentStep) PeersIndigo else PeersLavender)
                        )
                    }
                }
                Text(
                    text = "Step ${state.currentStep} of 4",
                    color = PeersInk,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        },
        bottomBar = {
            Surface(color = LightBackground, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (state.currentStep > 1) {
                        TextButton(onClick = { viewModel.previousStep() }) {
                            Text("Back", color = PeersInk)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp)) // Maintain alignment on step 1
                    }

                    Button(
                        onClick = {
                            if (state.currentStep == 4) {
                                viewModel.completeProfile()
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        enabled = viewModel.canProceedToNextStep() &&
                            viewModel.completion.value !is ProfileCompletion.Saving,
                        colors = ButtonDefaults.buttonColors(containerColor = PeersIndigo)
                    ) {
                        Text(if (state.currentStep == 4) "Complete Profile" else "Continue", color = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        // Smooth slide animations between the 4 steps
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut()
                } else {
                    slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut()
                }
            },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            label = "ProfileSetupTransition"
        ) { step ->
            when (step) {
                1 -> BasicInformationStep(state, viewModel)
                2 -> ProfilePictureStep(state, viewModel)
                3 -> InterestsStep(state, viewModel)
                4 -> NeedsStep(state, viewModel)
            }
        }
    }
}