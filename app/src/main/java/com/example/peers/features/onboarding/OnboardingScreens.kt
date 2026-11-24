package com.example.peers.features.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.R
import com.example.peers.ui.theme.AgravePro
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky
import com.example.peers.ui.theme.PeersSplashBackground
import kotlinx.coroutines.delay

object OnboardingScreens {

    /**
     * Animated Splash Screen:
     * - Background: Light warm peach/orange (#FFF2D6)
     * - Mascot (splash_screen_mascot.xml): Slides up from bottom, covering half the screen
     * - Header: "PEERS" in Agrave Pro font with playful theme colors
     * - Subtext: "We grow together as a community" in Google Sans font
     */
    @Composable
    fun Splash() {
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isVisible = true
        }

        // Mascot slide-up animation
        val mascotOffset by animateDpAsState(
            targetValue = if (isVisible) 0.dp else 450.dp,
            animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            label = "mascotSlideUp"
        )

        // Text fade & gentle entrance
        val textAlpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(durationMillis = 800, delayMillis = 150, easing = FastOutSlowInEasing),
            label = "textFadeIn"
        )

        // Colorful PEERS title matching the splash_screen_reference design
        val peersAnnotated = buildAnnotatedString {
            withStyle(SpanStyle(color = Color(0xFFF7B731))) { append("P") }
            withStyle(SpanStyle(color = Color(0xFF4CAE4F))) { append("E") }
            withStyle(SpanStyle(color = Color(0xFFC76D43))) { append("E") }
            withStyle(SpanStyle(color = Color(0xFF2980B9))) { append("R") }
            withStyle(SpanStyle(color = Color(0xFF8E44AD))) { append("S") }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PeersSplashBackground)
        ) {
            // ── Upper Portion: Branding & Slogan ─────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.48f)
                    .padding(horizontal = 24.dp)
                    .align(Alignment.TopCenter)
                    .alpha(textAlpha),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = peersAnnotated,
                    fontFamily = AgravePro,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "We grow together as a community",
                    fontFamily = GoogleSans,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF3B405A),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.2.sp,
                )
            }

            // ── Lower Portion: Mascot Sliding Up From Bottom ────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f)
                    .align(Alignment.BottomCenter)
                    .offset(y = mascotOffset),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(R.drawable.splash_screen_mascot),
                    contentDescription = "PEERS Mascot",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(0.92f)
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Welcome(
        isSigningIn: Boolean,
        onContinueWithGoogle: () -> Unit,
        onToggleTheme: () -> Unit,
        isNeonDark: Boolean
    ) {
        var showSignIn by remember { mutableStateOf(false) }
        var signingUp by remember { mutableStateOf(true) }
        val pagerState = rememberPagerState(pageCount = { slides.size })

        LaunchedEffect(pagerState) {
            while (true) {
                delay(3200)
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onToggleTheme) {
                    Text(
                        if (isNeonDark) "Switch to Aesthetic Light" else "Switch to Neon Dark",
                        fontFamily = Lato,
                    )
                }
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Get started with",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontFamily = Lato,
                )
                Text(
                    "PEERS",
                    color = PeersPrimaryIndigo,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AgravePro,
                    letterSpacing = 3.sp,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    "A space to connect, learn, and grow with your community.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                    fontSize = 15.sp,
                    fontFamily = Lato,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(24.dp))
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { page ->
                val slide = slides[page]
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Illustration(slide.color, slide.accent, slide.symbol, Modifier.fillMaxWidth().weight(1f))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        slide.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GoogleSans,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        slide.caption,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                        fontSize = 14.sp,
                        fontFamily = Lato,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                slides.indices.forEach { index ->
                    Box(
                        Modifier
                            .size(if (pagerState.currentPage == index) 9.dp else 7.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == index) PeersPrimaryIndigo else PeersPrimaryIndigo.copy(alpha = 0.25f))
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { signingUp = true; showSignIn = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo)
            ) {
                Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = GoogleSans)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { signingUp = false; showSignIn = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Login", color = PeersPrimaryIndigo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = GoogleSans)
            }
        }

        if (showSignIn) {
            ModalBottomSheet(onDismissRequest = { showSignIn = false }, sheetState = rememberModalBottomSheetState()) {
                Column(
                    Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (signingUp) "Create your PEERS profile" else "Welcome back",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GoogleSans,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Choose how you’d like to continue.",
                        textAlign = TextAlign.Center,
                        fontFamily = Lato,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(22.dp))
                    OutlinedButton(
                        onClick = onContinueWithGoogle,
                        enabled = !isSigningIn,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSigningIn) {
                            Text("Signing in…", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontFamily = Lato)
                        } else {
                            Text("G", color = PeersPrimaryIndigo, fontWeight = FontWeight.Bold, fontSize = 19.sp, fontFamily = GoogleSans)
                            Text("   Continue with Google", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontFamily = GoogleSans)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun Illustration(base: Color, accent: Color, symbol: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().padding(vertical = 10.dp).clip(RoundedCornerShape(30.dp)).background(base))
        Canvas(Modifier.size(180.dp)) {
            drawCircle(color = accent.copy(alpha = 0.65f), radius = size.minDimension * 0.43f)
            drawCircle(color = PeersPrimaryIndigo.copy(alpha = 0.20f), radius = size.minDimension * 0.27f, center = Offset(size.width * 0.62f, size.height * 0.42f))
        }
        Text(symbol, fontSize = 72.sp)
    }
}

private data class OnboardingSlide(val title: String, val caption: String, val color: Color, val accent: Color, val symbol: String)

private val slides = listOf(
    OnboardingSlide("Find your people", "Meet peers who share your interests and goals.", PeersLavender.copy(alpha = 0.32f), PeersSky, "🤝"),
    OnboardingSlide("Learn together", "Share knowledge, ideas, and encouragement.", PeersSky.copy(alpha = 0.42f), PeersLavender, "💡"),
    OnboardingSlide("Grow as a community", "Build meaningful connections, one step at a time.", PeersMint.copy(alpha = 0.58f), PeersLavender, "🌱")
)
