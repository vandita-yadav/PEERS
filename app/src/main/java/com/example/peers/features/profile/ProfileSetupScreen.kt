package com.example.peers.features.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.peers.features.profile.components.BasicInformationStep
import com.example.peers.features.profile.components.InterestsStep
import com.example.peers.features.profile.components.NeedsStep
import com.example.peers.features.profile.components.ProfilePictureStep
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel = viewModel(),
    onProfileComplete: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val completion by viewModel.completion.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Prevent hardware back button from skipping onboarding
    BackHandler(enabled = !state.isCompletedScreen) {
        if (state.currentStep > 1) {
            viewModel.previousStep()
        }
    }

    LaunchedEffect(completion) {
        when (val c = completion) {
            is ProfileCompletion.Error -> {
                snackbarHostState.showSnackbar(c.message)
                viewModel.consumeCompletion()
            }
            else -> Unit
        }
    }

    if (state.isCompletedScreen) {
        ProfileCompletedSummaryScreen(
            state = state,
            onEnterDashboard = {
                val uid = state.completedUserId ?: viewModel.currentUserId.orEmpty()
                viewModel.consumeCompletion()
                onProfileComplete(uid)
            }
        )
        return
    }

    val isSaving = completion is ProfileCompletion.Saving
    val canProceed = viewModel.canProceedToNextStep() && !isSaving

    val primaryButtonLabel = when (state.currentStep) {
        3 -> if (state.selectedInterests.isNotEmpty()) {
            "Continue (${state.selectedInterests.size} selected)"
        } else {
            "Continue"
        }
        4 -> "Complete Profile"
        else -> "Continue"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        if (state.currentStep > 1) {
                            IconButton(onClick = { viewModel.previousStep() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // 4-step segmented progress bar matching sample layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (i <= state.currentStep) PeersPrimaryIndigo
                                    else PeersPrimaryIndigo.copy(alpha = 0.22f)
                                )
                        )
                    }
                }

                Text(
                    text = "Step ${state.currentStep} of 4",
                    fontFamily = Lato,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text(
                                "Back",
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (state.currentStep == 4) {
                                viewModel.completeProfile()
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        enabled = canProceed,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PeersPrimaryIndigo,
                            disabledContainerColor = PeersPrimaryIndigo.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = primaryButtonLabel,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
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

/**
 * Celebratory "You're All Set!" completion screen (per profileOnboardingSampleLayout2.png)
 * confirming saved user info, avatar/photo, and selected tags before opening the Dashboard.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileCompletedSummaryScreen(
    state: ProfileSetupState,
    onEnterDashboard: () -> Unit,
) {
    val displayUni = if (state.university == "Other") state.customUniversity else state.university
    val displayCourse = if (state.course == "Other") state.customCourse else state.course

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PeersPrimaryIndigo.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(PeersPrimaryIndigo.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "All Set",
                tint = PeersPrimaryIndigo,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "You're All Set!",
            fontFamily = GoogleSans,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Your profile is ready and your personalized dashboard has been created.",
            fontFamily = Lato,
            fontSize = 14.5.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                PeersAvatarBadge(
                    avatarIndex = if (state.profileImageUri == null) (state.selectedAvatar ?: "neutral/neutral1.png") else null,
                    photoUrl = state.profileImageUri ?: state.croppedPhotoDataUri ?: state.photoUrl,
                    size = 84.dp,
                    borderWidth = 3.dp,
                    borderColor = PeersPrimaryIndigo
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = state.name.ifBlank { "PEERS Student" },
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${state.currentCity}, ${state.currentCountry}",
                    fontFamily = Lato,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (displayUni.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$displayCourse · $displayUni",
                        fontFamily = Lato,
                        fontSize = 13.sp,
                        color = PeersPrimaryIndigo,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(14.dp))

                val previewTags = state.selectedInterests.take(4)
                val chipColors = listOf(PeersLavender, PeersSky, PeersMint, PeersPrimaryIndigo)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    previewTags.forEachIndexed { idx, tag ->
                        val chipColor = chipColors[idx % chipColors.size]
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = chipColor.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, chipColor)
                        ) {
                            Text(
                                text = tag,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onEnterDashboard,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Go to Dashboard",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}
