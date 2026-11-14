package com.example.admin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.R
import com.example.admin.ui.theme.CinemaCrimson
import com.example.admin.ui.theme.CinemaGold
import com.example.admin.ui.theme.ObsidianBlack
import com.example.admin.ui.theme.ObsidianBorder
import com.example.admin.ui.theme.ObsidianCard
import com.example.admin.ui.theme.ObsidianElevated
import com.example.admin.ui.theme.TextPrimary
import com.example.admin.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun AdminSplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.78f) }
    val alpha = remember { Animatable(0f) }
    var showTagline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        showTagline = true
        delay(800L)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_hero_1794667014216),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.42f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CinemaGold.copy(alpha = 0.22f),
                            ObsidianBlack.copy(alpha = 0.78f),
                            ObsidianBlack
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(2.dp, CinemaGold.copy(alpha = 0.75f), RoundedCornerShape(28.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.admin_launcher_emblem_1794668485984),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.brand_uppercase),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 27.sp,
                    letterSpacing = 2.sp
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = showTagline, enter = fadeIn(tween(350))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.brand_tagline),
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.4.sp),
                        color = CinemaGold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CinemaCrimson)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminAuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onGoogleSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_hero_1794667014216),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.32f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianBlack.copy(alpha = 0.5f),
                            ObsidianBlack.copy(alpha = 0.92f),
                            ObsidianBlack
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .widthIn(max = 520.dp)
            ) {
                Surface(
                    color = CinemaGold.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.55f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESTRICTED STUDIO ACCESS",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Image(
                    painter = painterResource(id = R.drawable.admin_launcher_emblem_1794668485984),
                    contentDescription = stringResource(R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.5.dp, CinemaGold.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.sign_in_headline),
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.sign_in_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(26.dp))

                Surface(
                    color = ObsidianCard.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        SecurityHighlightRow("Publish & unpublish movies live to NAA MOVIES WORLD")
                        SecurityHighlightRow("Upload licensed movie posters & authorized HD stream URLs")
                        SecurityHighlightRow("Protected by Firebase Authentication & Security Rules")
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = CinemaCrimson.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CinemaCrimson.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = onGoogleSignInClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = ObsidianBlack,
                        disabledContainerColor = ObsidianElevated,
                        disabledContentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("admin_google_sign_in_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = ObsidianBlack,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Verifying Studio Credentials…",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.sign_in_google),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUnauthorizedScreen(
    signedInEmail: String?,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Surface(
            color = ObsidianCard,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CinemaCrimson.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Surface(
                    color = CinemaCrimson.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Access denied",
                        tint = CinemaCrimson,
                        modifier = Modifier
                            .padding(14.dp)
                            .size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Administrator Access Required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The account ${signedInEmail ?: "(unknown)"} is not listed as an authorized NAA MOVIES WORLD studio administrator.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                OutlinedButton(
                    onClick = onSignOutClick,
                    border = BorderStroke(1.dp, CinemaGold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_unauthorized_sign_out")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out & Switch Account")
                }
            }
        }
    }
}

@Composable
private fun SecurityHighlightRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.VerifiedUser,
            contentDescription = null,
            tint = CinemaGold,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
