package com.example.peers.features.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.peers.R
import com.example.peers.features.auth.AuthMode
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSplashBackground
import kotlinx.coroutines.delay

/**
 * Data model for carousel slides.
 */
data class CarouselSlide(
    val assetName: String,
    val headline: String,
    val subtitle: String
)

val carouselSlides = listOf(
    CarouselSlide(
        assetName = "welcome.svg",
        headline = "Welcome to PEERS",
        subtitle = "Connect with local and international students across your campus and city."
    ),
    CarouselSlide(
        assetName = "accomodation.svg",
        headline = "Find Verified Housing",
        subtitle = "Explore student-reviewed PGs, flats, and compatible roommates near your university."
    ),
    CarouselSlide(
        assetName = "Events-cuate.svg",
        headline = "Never Miss an Event",
        subtitle = "Discover campus fests, workshops, orientation sessions, and student meetups."
    ),
    CarouselSlide(
        assetName = "Group discussion.svg",
        headline = "Ask, Share & Discuss",
        subtitle = "Join university and city communities to get real answers from fellow students."
    ),
    CarouselSlide(
        assetName = "checklist_illustration.svg",
        headline = "Stay on Track",
        subtitle = "Follow personalized checklists for visas, banking, housing, and campus arrival."
    ),
    CarouselSlide(
        assetName = "finance_management.svg",
        headline = "Plan Your Student Budget",
        subtitle = "Estimate monthly rent, food, and travel costs while tracking your actual spending."
    ),
    CarouselSlide(
        assetName = "emergency_illustration.svg",
        headline = "Quick Help When It Counts",
        subtitle = "Access verified emergency contacts, safety alerts, and campus support anytime."
    ),
    CarouselSlide(
        assetName = "rental_illustration.svg",
        headline = "Get Around Easily",
        subtitle = "Discover local transit options, vehicle rentals, and essential services nearby."
    )
)

/** Validation constraints helper */
object AuthValidation {
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.trim().matches(Regex(emailPattern))
    }

    fun isPasswordLongEnough(p: String): Boolean = p.length >= 6
    fun hasUppercase(p: String): Boolean = p.any { it.isUpperCase() }
    fun hasLowercase(p: String): Boolean = p.any { it.isLowerCase() }
    fun hasDigit(p: String): Boolean = p.any { it.isDigit() }
    fun hasSpecialChar(p: String): Boolean {
        val specialChars = """,/.;'":><*&^%,-_#@!+=~`|\\!?"'$%^&*()_+={}[]:;<>?,./-"""
        return p.any { specialChars.contains(it) }
    }

    fun isPasswordValid(p: String): Boolean =
        isPasswordLongEnough(p) && hasUppercase(p) && hasLowercase(p) && hasDigit(p) && hasSpecialChar(p)
}

object OnboardingScreens {

    /**
     * Animated Splash Screen:
     * - Background: Light warm peach/orange (#FFF4E8)
     * - Mascot (splash_screen_mascot.xml): Slides up from bottom, covering half the screen
     * - Logo: Funky, playful, colorful PEERS vector (peers_funky_logo.xml)
     * - Subtext: "We grow together as a community" in Google Sans font, brought close to mascot
     */
    @Composable
    fun Splash() {
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isVisible = true
        }

        val mascotOffset by animateDpAsState(
            targetValue = if (isVisible) 0.dp else 450.dp,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            label = "mascotSlideUp"
        )

        val textAlpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(durationMillis = 750, delayMillis = 100, easing = FastOutSlowInEasing),
            label = "textFadeIn"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PeersSplashBackground)
        ) {
            // ── Upper Branding (Positioned closely above the mascot) ────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.48f)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .align(Alignment.TopCenter)
                    .alpha(textAlpha)
            ) {
                // Funky, playful PEERS logo
                Image(
                    painter = painterResource(R.drawable.peers_funky_logo),
                    contentDescription = "PEERS",
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .height(64.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "We grow together as a community",
                    fontFamily = GoogleSans,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E334D),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.2.sp
                )
            }

            // ── Lower Portion: Mascot Covering Half of Screen ──────────────
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
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .fillMaxHeight()
                )
            }
        }
    }

    /**
     * Welcome Screen:
     * - Top-right: Half moon icon button for dark mode toggle (with generous padding, no outline)
     * - Carousel: Floating card with no background behind illustrations, auto-scrolls
     * - Actions: Sign Up and Login buttons triggering Google / Manual choices
     * - Returning user detection banner: "this account already exists, please login to access"
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Welcome(
        isSigningIn: Boolean,
        accountExistsError: String?,
        onContinueWithGoogle: (AuthMode) -> Unit,
        onNavigateToManualSignUp: () -> Unit,
        onNavigateToManualLogIn: () -> Unit,
        onToggleTheme: () -> Unit,
        isNeonDark: Boolean,
        onDismissAccountExistsError: () -> Unit = {}
    ) {
        val context = LocalContext.current
        val imageLoader = remember {
            ImageLoader.Builder(context)
                .components { add(SvgDecoder.Factory()) }
                .build()
        }

        var showChoiceSheet by remember { mutableStateOf(false) }
        var activeAuthMode by remember { mutableStateOf(AuthMode.SIGN_UP) }

        val pagerState = rememberPagerState(pageCount = { carouselSlides.size })

        // Auto-slideshow scrolling
        LaunchedEffect(pagerState) {
            while (true) {
                delay(3200)
                if (carouselSlides.isNotEmpty()) {
                    pagerState.animateScrollToPage((pagerState.currentPage + 1) % carouselSlides.size)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top Bar: Dark Mode Half Moon Icon Button ────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, end = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (isNeonDark) R.drawable.darktheme_white else R.drawable.darktheme_black
                        ),
                        contentDescription = "Toggle Dark Mode",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ── Returning Account Alert Banner (if user clicked sign-up for existing account) ──
            if (accountExistsError != null) {
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNeonDark) Color(0xFF3A1C1C) else Color(0xFFFFF2F0)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFFA39E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFE04A41),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = accountExistsError,
                            color = if (isNeonDark) Color(0xFFFF948E) else Color(0xFFB01D16),
                            fontSize = 13.5.sp,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissAccountExistsError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = if (isNeonDark) Color(0xFFFF948E) else Color(0xFFB01D16),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ── Floating Carousel Card ──────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 6.dp)
            ) { page ->
                val slide = carouselSlides[page]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Floating illustration with NO background behind it
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data("file:///android_asset/carousel/${slide.assetName}")
                                    .decoderFactory(SvgDecoder.Factory())
                                    .crossfade(true)
                                    .build(),
                                imageLoader = imageLoader,
                                contentDescription = slide.headline,
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .fillMaxHeight(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Headline & Subtitle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = slide.headline,
                                fontFamily = GoogleSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = slide.subtitle,
                                fontFamily = Lato,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // ── Carousel Indicators (Active elongated pill, inactive dots) ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                carouselSlides.indices.forEach { index ->
                    val isSelected = pagerState.currentPage == index
                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 6.dp,
                        animationSpec = tween(durationMillis = 300),
                        label = "indicatorWidth"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(indicatorWidth)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) PeersPrimaryIndigo
                                else PeersPrimaryIndigo.copy(alpha = 0.25f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Action Buttons: Sign Up & Login ─────────────────────────────
            Button(
                onClick = {
                    activeAuthMode = AuthMode.SIGN_UP
                    showChoiceSheet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo)
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSans,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    activeAuthMode = AuthMode.LOG_IN
                    showChoiceSheet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, PeersPrimaryIndigo)
            ) {
                Text(
                    text = "Login",
                    color = PeersPrimaryIndigo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSans
                )
            }

            Spacer(Modifier.height(10.dp))
        }

        // ── Auth Choice Bottom Sheet ─────────────────────────────────────────
        if (showChoiceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showChoiceSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 36.dp, top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (activeAuthMode == AuthMode.SIGN_UP) "Create Your PEERS Account" else "Welcome Back to PEERS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GoogleSans,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Choose your preferred method to continue",
                        fontSize = 14.sp,
                        fontFamily = Lato,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(24.dp))

                    // Choice 1: Continue with Google
                    OutlinedButton(
                        onClick = {
                            showChoiceSheet = false
                            onContinueWithGoogle(activeAuthMode)
                        },
                        enabled = !isSigningIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "G",
                                color = PeersPrimaryIndigo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                fontFamily = GoogleSans
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = if (activeAuthMode == AuthMode.SIGN_UP) "Sign up using Google" else "Login using Google",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                fontFamily = GoogleSans
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Choice 2: Manual (Email & Password)
                    Button(
                        onClick = {
                            showChoiceSheet = false
                            if (activeAuthMode == AuthMode.SIGN_UP) {
                                onNavigateToManualSignUp()
                            } else {
                                onNavigateToManualLogIn()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = if (activeAuthMode == AuthMode.SIGN_UP) "Sign up manually" else "Login manually",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                fontFamily = GoogleSans
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Manual Sign-Up Screen:
     * - Fields: Full Name, Email Address, Password
     * - Email format validation
     * - Password constraint validation (min 6, 1 uppercase, 1 lowercase, 1 number, 1 special character)
     * - Divider: Or Continue With
     * - Google OAuth alternate button
     * - Link: "Already have an account? Log In" (Switches to Login screen, no action)
     */
    @Composable
    fun ManualSignUp(
        isSigningIn: Boolean,
        accountExistsError: String?,
        onSignUp: (String, String, String) -> Unit,
        onContinueWithGoogle: () -> Unit,
        onNavigateToLogIn: () -> Unit,
        onBack: () -> Unit
    ) {
        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        val emailValid = AuthValidation.isValidEmail(email)
        val passwordValid = AuthValidation.isPasswordValid(password)
        val canSubmit = fullName.isNotBlank() && emailValid && passwordValid && !isSigningIn

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sign Up Manually",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Create Your Account",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Join PEERS to connect with fellow students across campuses.",
                fontFamily = Lato,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Returning account error banner
            if (accountExistsError != null) {
                Spacer(Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2F0)),
                    border = BorderStroke(1.dp, Color(0xFFFFA39E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFE04A41),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = accountExistsError,
                            color = Color(0xFFB01D16),
                            fontSize = 13.5.sp,
                            fontFamily = GoogleSans,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Full Name Field ──────────────────────────────────────────────
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name", fontFamily = GoogleSans) },
                placeholder = { Text("e.g. Alex Johnson", fontFamily = Lato) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PeersPrimaryIndigo)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PeersPrimaryIndigo,
                    focusedLabelColor = PeersPrimaryIndigo
                )
            )

            Spacer(Modifier.height(16.dp))

            // ── Email Field ──────────────────────────────────────────────────
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", fontFamily = GoogleSans) },
                placeholder = { Text("name@example.com", fontFamily = Lato) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = PeersPrimaryIndigo)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = email.isNotBlank() && !emailValid,
                supportingText = {
                    if (email.isNotBlank() && !emailValid) {
                        Text(
                            "Enter a valid email address (e.g. @gmail.com, @safari.com)",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontFamily = Lato
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PeersPrimaryIndigo,
                    focusedLabelColor = PeersPrimaryIndigo
                )
            )

            Spacer(Modifier.height(10.dp))

            // ── Password Field ───────────────────────────────────────────────
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontFamily = GoogleSans) },
                placeholder = { Text("Min. 6 chars with mix of letters & numbers", fontFamily = Lato) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PeersPrimaryIndigo)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PeersPrimaryIndigo,
                    focusedLabelColor = PeersPrimaryIndigo
                )
            )

            // Password constraints checklist
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ConstraintCheckItem(
                    label = "Minimum length 6 characters",
                    satisfied = AuthValidation.isPasswordLongEnough(password)
                )
                ConstraintCheckItem(
                    label = "One uppercase letter (A-Z)",
                    satisfied = AuthValidation.hasUppercase(password)
                )
                ConstraintCheckItem(
                    label = "One lowercase letter (a-z)",
                    satisfied = AuthValidation.hasLowercase(password)
                )
                ConstraintCheckItem(
                    label = "One number (0-9)",
                    satisfied = AuthValidation.hasDigit(password)
                )
                ConstraintCheckItem(
                    label = "One unique symbol (,/.;'\":><*&^%,-_#@!+=)",
                    satisfied = AuthValidation.hasSpecialChar(password)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Sign Up Button ───────────────────────────────────────────────
            Button(
                onClick = { onSignUp(fullName, email, password) },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo)
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GoogleSans,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Dividing Line ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Text(
                    text = "  Or Continue With  ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontFamily = Lato
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }

            Spacer(Modifier.height(16.dp))

            // ── Continue with Google Button ──────────────────────────────────
            OutlinedButton(
                onClick = onContinueWithGoogle,
                enabled = !isSigningIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("G", color = PeersPrimaryIndigo, fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = GoogleSans)
                    Spacer(Modifier.width(12.dp))
                    Text("Continue with Google", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = GoogleSans)
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Already have an account? Log In ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontFamily = Lato,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Log In",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PeersPrimaryIndigo,
                    modifier = Modifier.clickable { onNavigateToLogIn() }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    /**
     * Manual Log-In Screen:
     * - Fields: Email Address, Password
     * - Log In button
     * - Divider: Or Continue With
     * - Google OAuth alternate button
     * - Link: "Don't have an account? Sign Up" (Switches to SignUp screen, no action)
     */
    @Composable
    fun ManualLogIn(
        isSigningIn: Boolean,
        onLogIn: (String, String) -> Unit,
        onContinueWithGoogle: () -> Unit,
        onNavigateToSignUp: () -> Unit,
        onBack: () -> Unit
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        val canSubmit = email.isNotBlank() && password.isNotBlank() && !isSigningIn

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Login Manually",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Welcome Back!",
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Log in with your email address and password to continue.",
                fontFamily = Lato,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            // ── Email Field ──────────────────────────────────────────────────
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", fontFamily = GoogleSans) },
                placeholder = { Text("name@example.com", fontFamily = Lato) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = PeersPrimaryIndigo)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PeersPrimaryIndigo,
                    focusedLabelColor = PeersPrimaryIndigo
                )
            )

            Spacer(Modifier.height(16.dp))

            // ── Password Field ───────────────────────────────────────────────
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontFamily = GoogleSans) },
                placeholder = { Text("Enter your password", fontFamily = Lato) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PeersPrimaryIndigo)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PeersPrimaryIndigo,
                    focusedLabelColor = PeersPrimaryIndigo
                )
            )

            Spacer(Modifier.height(24.dp))

            // ── Log In Button ────────────────────────────────────────────────
            Button(
                onClick = { onLogIn(email, password) },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeersPrimaryIndigo)
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Log In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GoogleSans,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Dividing Line ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Text(
                    text = "  Or Continue With  ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontFamily = Lato
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }

            Spacer(Modifier.height(16.dp))

            // ── Continue with Google Button ──────────────────────────────────
            OutlinedButton(
                onClick = onContinueWithGoogle,
                enabled = !isSigningIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("G", color = PeersPrimaryIndigo, fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = GoogleSans)
                    Spacer(Modifier.width(12.dp))
                    Text("Continue with Google", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = GoogleSans)
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Don't have an account? Sign Up ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontFamily = Lato,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign Up",
                    fontFamily = GoogleSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PeersPrimaryIndigo,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    @Composable
    private fun ConstraintCheckItem(label: String, satisfied: Boolean) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 1.dp)
        ) {
            Icon(
                imageVector = if (satisfied) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (satisfied) Color(0xFF389E0D) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = Lato,
                color = if (satisfied) Color(0xFF389E0D) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
