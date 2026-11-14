package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.NaaMoviesBrandCrest
import com.example.ui.theme.CinemaBrushes
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianGlass
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.84f) }
    val alpha = remember { Animatable(0f) }
    var showTagline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(360, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
        showTagline = true
        delay(550L)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_hero_1794667014216),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.45f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CinemaCrimson.copy(alpha = 0.28f),
                            ObsidianBlack.copy(alpha = 0.78f),
                            ObsidianDeep
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
            Surface(
                color = ObsidianGlass,
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(1.5.dp, CinemaBrushes.goldMetallic),
                shadowElevation = 18.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(18.dp)
                ) {
                    NaaMoviesBrandCrest(size = 76.dp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "NAA MOVIES ",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "WORLD",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    color = CinemaGold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = showTagline, enter = fadeIn(tween(280))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.brand_tagline),
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.6.sp),
                        color = CinemaGoldSoft,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CinemaBrushes.crimsonAction)
                    )
                }
            }
        }
    }
}
