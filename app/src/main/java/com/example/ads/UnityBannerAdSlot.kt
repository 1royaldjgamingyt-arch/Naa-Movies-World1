package com.example.ads

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.example.ui.components.GlassSurface
import com.example.ui.theme.CinemaBrushes
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldSoft
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianGlass
import com.example.ui.theme.ObsidianGlassElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

@Composable
fun UnityBannerAdSlot(
    adFreeMinutes: Int,
    placementId: String = BuildConfig.UNITY_BANNER_PLACEMENT,
    modifier: Modifier = Modifier
) {
    if (adFreeMinutes > 0 || !UnityAdsManager.isSdkConfigured) {
        return
    }

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    var isBannerLoaded by remember(placementId) { mutableStateOf(false) }

    val bannerView = remember(activity, placementId) {
        runCatching {
            BannerView(activity, placementId, UnityBannerSize(320, 50)).apply {
                listener = object : BannerView.Listener() {
                    override fun onBannerLoaded(bannerAdView: BannerView?) {
                        isBannerLoaded = true
                    }

                    override fun onBannerFailedToLoad(
                        bannerAdView: BannerView?,
                        errorInfo: BannerErrorInfo?
                    ) {
                        isBannerLoaded = false
                    }
                }
                load()
            }
        }.getOrNull()
    }

    DisposableEffect(bannerView) {
        onDispose {
            runCatching { bannerView?.destroy() }
        }
    }

    if (bannerView == null) return

    AnimatedVisibility(
        visible = isBannerLoaded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        GlassSurface(
            shape = RoundedCornerShape(14.dp),
            containerColor = ObsidianGlass,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("unity_banner_ad_slot")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "SPONSORED",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                AndroidView(
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            val params = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER
                            )
                            (bannerView.parent as? android.view.ViewGroup)?.removeView(bannerView)
                            addView(bannerView, params)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }
        }
    }
}

@Composable
fun AdFreeCinemaPassCard(
    adFreeMinutes: Int,
    onWatchRewardedAd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = adFreeMinutes > 0

    GlassSurface(
        shape = RoundedCornerShape(16.dp),
        containerColor = ObsidianGlassElevated,
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (!active) {
                    Modifier.clickable(onClick = onWatchRewardedAd)
                } else {
                    Modifier
                }
            )
            .testTag("rewarded_ad_pass_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = if (active) EmeraldVerified.copy(alpha = 0.16f) else CinemaGold.copy(alpha = 0.16f),
                    shape = CircleShape,
                    border = BorderStroke(
                        1.dp,
                        if (active) EmeraldVerified.copy(alpha = 0.6f) else CinemaGold.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (active) Icons.Filled.Verified else Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = if (active) EmeraldVerified else CinemaGold,
                        modifier = Modifier.padding(9.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (active) {
                            "Ad-Free Cinema Pass Active"
                        } else {
                            "Unlock 30m Ad-Free Cinema"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (active) {
                            "${adFreeMinutes} min uninterrupted browsing remaining"
                        } else {
                            "Optional rewarded ad • Pause banners & breaks"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (active) EmeraldVerified else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!active) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CinemaBrushes.goldMetallic)
                        .padding(horizontal = 11.dp, vertical = 8.dp)
                        .testTag("rewarded_ad_pass_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PlayCircle,
                            contentDescription = "Watch rewarded ad",
                            tint = ObsidianDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "30m Free",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = ObsidianDeep
                        )
                    }
                }
            } else {
                Surface(
                    color = CinemaCrimson.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, ObsidianBorder)
                ) {
                    Text(
                        text = "${adFreeMinutes}m",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaGoldSoft,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}
