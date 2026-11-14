package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.example.data.model.Movie
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianGlassStrong
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

private enum class StreamQualityOption(
    val label: String,
    val maxWidth: Int,
    val maxHeight: Int
) {
    AUTO("Auto", Int.MAX_VALUE, Int.MAX_VALUE),
    UHD_4K("4K UHD", 3840, 2160),
    HD_1080P("1080p HD", 1920, 1080),
    HD_720P("720p HD", 1280, 720),
    SD_480P("480p Saver", 854, 480)
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    movie: Movie,
    initialPositionMs: Long,
    onSaveProgress: (positionMs: Long, durationMs: Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var playerError by remember { mutableStateOf<String?>(null) }
    var currentPositionMs by remember { mutableLongStateOf(initialPositionMs.coerceAtLeast(0L)) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isLandscape by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var resizeModeIndex by remember { mutableIntStateOf(0) }

    var selectedQuality by remember { mutableStateOf(StreamQualityOption.AUTO) }
    var qualityMenuExpanded by remember { mutableStateOf(false) }

    val availableSubtitles = remember(movie.subtitleLanguages) {
        listOf("Off") + movie.subtitleLanguages.ifEmpty { listOf("English") }
    }
    var selectedSubtitle by remember { mutableStateOf("Off") }
    var subtitleMenuExpanded by remember { mutableStateOf(false) }
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }

    val resizeModes = remember {
        listOf(
            AspectRatioFrameLayout.RESIZE_MODE_FIT to "Fit",
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM to "Zoom",
            AspectRatioFrameLayout.RESIZE_MODE_FILL to "Fill"
        )
    }

    val exoPlayer = remember(movie.id, movie.streamUrl) {
        ExoPlayer.Builder(context).build().apply {
            trackSelectionParameters = trackSelectionParameters.buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
            setMediaItem(MediaItem.fromUri(movie.streamUrl))
            if (initialPositionMs > 3_000L) {
                seekTo(initialPositionMs)
            }
            playWhenReady = true
            prepare()
        }
    }

    fun applyQualityConstraint(option: StreamQualityOption) {
        selectedQuality = option
        val builder = exoPlayer.trackSelectionParameters.buildUpon()
        if (option == StreamQualityOption.AUTO) {
            builder.clearVideoSizeConstraints()
        } else {
            builder.setMaxVideoSize(option.maxWidth, option.maxHeight)
        }
        exoPlayer.trackSelectionParameters = builder.build()
    }

    fun applySubtitleSelection(lang: String) {
        selectedSubtitle = lang
        val builder = exoPlayer.trackSelectionParameters.buildUpon()
        if (lang == "Off") {
            builder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
        } else {
            val isoCode = when {
                lang.equals("Telugu", true) -> "te"
                lang.equals("Tamil", true) -> "ta"
                lang.equals("Hindi", true) -> "hi"
                else -> "en"
            }
            builder
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .setPreferredTextLanguage(isoCode)
        }
        exoPlayer.trackSelectionParameters = builder.build()
    }

    // Listen to player events
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    playerError = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                playerError = error.localizedMessage ?: "Unable to stream video. Check your connection."
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            onSaveProgress(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Pause cleanly on lifecycle ON_PAUSE
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
                onSaveProgress(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Progress polling & checkpoint save
    LaunchedEffect(exoPlayer) {
        var tick = 0
        while (true) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            val dur = exoPlayer.duration
            if (dur > 0L) durationMs = dur
            tick++
            if (tick % 20 == 0 && exoPlayer.isPlaying) {
                onSaveProgress(currentPositionMs, durationMs)
            }
            delay(500L)
        }
    }

    // Auto-hide controls after 3.5 seconds while playing
    LaunchedEffect(controlsVisible, isPlaying, qualityMenuExpanded, subtitleMenuExpanded) {
        if (controlsVisible && isPlaying && !qualityMenuExpanded && !subtitleMenuExpanded) {
            delay(3500L)
            controlsVisible = false
        }
    }

    LaunchedEffect(seekFeedbackText) {
        if (seekFeedbackText != null) {
            delay(850L)
            seekFeedbackText = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(exoPlayer) {
                detectTapGestures(
                    onTap = { controlsVisible = !controlsVisible },
                    onDoubleTap = { offset ->
                        val isForward = offset.x > size.width / 2f
                        if (isForward) {
                            val maxDur = exoPlayer.duration.coerceAtLeast(0L)
                            val target = if (maxDur > 0L) {
                                (exoPlayer.currentPosition + 10_000L).coerceAtMost(maxDur)
                            } else {
                                exoPlayer.currentPosition + 10_000L
                            }
                            exoPlayer.seekTo(target)
                            seekFeedbackText = "+10s"
                        } else {
                            val target = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(target)
                            seekFeedbackText = "-10s"
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    keepScreenOn = true
                    subtitleView?.apply {
                        visibility = View.VISIBLE
                        setStyle(
                            CaptionStyleCompat(
                                AndroidColor.WHITE,
                                AndroidColor.argb(165, 9, 9, 16),
                                AndroidColor.TRANSPARENT,
                                CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                                AndroidColor.BLACK,
                                null
                            )
                        )
                    }
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    player = exoPlayer
                }
            },
            update = { view ->
                view.resizeMode = resizeModes[resizeModeIndex].first
                if (view.player !== exoPlayer) {
                    view.player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering && playerError == null) {
            CircularProgressIndicator(
                color = CinemaGold,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = seekFeedbackText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 72.dp)
        ) {
            Surface(
                color = ObsidianGlassStrong,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.65f))
            ) {
                Text(
                    text = seekFeedbackText.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaGoldSoft,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        if (playerError != null) {
            Surface(
                color = ObsidianCard.copy(alpha = 0.94f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Playback Interrupted",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = playerError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            playerError = null
                            isBuffering = true
                            exoPlayer.prepare()
                            exoPlayer.play()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaCrimson)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Stream")
                    }
                }
            }
        }

        // Custom Glass Cinema Overlay Controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianBlack.copy(alpha = 0.82f),
                                Color.Black.copy(alpha = 0.24f),
                                ObsidianBlack.copy(alpha = 0.88f)
                            )
                        )
                    )
            ) {
                // Top Control Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                onSaveProgress(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
                                onBackClick()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit Player",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = CinemaCrimson,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (selectedQuality == StreamQualityOption.AUTO) {
                                            movie.qualityBadge
                                        } else {
                                            selectedQuality.label
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = movie.language,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CinemaGoldSoft
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Quality Selector Menu
                        Box {
                            Surface(
                                color = ObsidianGlassStrong,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, ObsidianBorder),
                                modifier = Modifier
                                    .clickable { qualityMenuExpanded = true }
                                    .testTag("player_quality_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Hd,
                                        contentDescription = "Select video quality",
                                        tint = CinemaGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedQuality.label.replace(" UHD", "").replace(" HD", ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = qualityMenuExpanded,
                                onDismissRequest = { qualityMenuExpanded = false },
                                modifier = Modifier.background(ObsidianElevated)
                            ) {
                                StreamQualityOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = option.label,
                                                    color = if (selectedQuality == option) CinemaGold else TextPrimary
                                                )
                                                if (selectedQuality == option) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = null,
                                                        tint = CinemaGold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            qualityMenuExpanded = false
                                            applyQualityConstraint(option)
                                        }
                                    )
                                }
                            }
                        }

                        // Subtitle (CC) Selector Menu
                        Box {
                            IconButton(
                                onClick = { subtitleMenuExpanded = true },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("player_subtitles_button")
                            ) {
                                Icon(
                                    imageVector = if (selectedSubtitle == "Off") {
                                        Icons.Filled.ClosedCaptionDisabled
                                    } else {
                                        Icons.Filled.ClosedCaption
                                    },
                                    contentDescription = "Subtitles ($selectedSubtitle)",
                                    tint = if (selectedSubtitle == "Off") TextSecondary else CinemaGold
                                )
                            }

                            DropdownMenu(
                                expanded = subtitleMenuExpanded,
                                onDismissRequest = { subtitleMenuExpanded = false },
                                modifier = Modifier.background(ObsidianElevated)
                            ) {
                                availableSubtitles.forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = if (lang == "Off") "Subtitles Off" else "$lang (CC)",
                                                    color = if (selectedSubtitle == lang) CinemaGold else TextPrimary
                                                )
                                                if (selectedSubtitle == lang) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = null,
                                                        tint = CinemaGold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            subtitleMenuExpanded = false
                                            applySubtitleSelection(lang)
                                        }
                                    )
                                }
                            }
                        }

                        // Speed Cycle
                        Surface(
                            color = ObsidianGlassStrong,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.8.dp, ObsidianBorder),
                            modifier = Modifier.clickable {
                                val next = when (playbackSpeed) {
                                    1f -> 1.25f
                                    1.25f -> 1.5f
                                    1.5f -> 2f
                                    2f -> 0.75f
                                    else -> 1f
                                }
                                playbackSpeed = next
                                exoPlayer.playbackParameters = PlaybackParameters(next)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = "Playback Speed",
                                    tint = CinemaGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${playbackSpeed}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Resize Mode Cycle
                        IconButton(
                            onClick = { resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AspectRatio,
                                contentDescription = "Video Aspect Ratio (${resizeModes[resizeModeIndex].second})",
                                tint = TextPrimary
                            )
                        }

                        // Landscape Fullscreen Toggle
                        IconButton(
                            onClick = {
                                val targetLandscape = !isLandscape
                                isLandscape = targetLandscape
                                activity?.requestedOrientation = if (targetLandscape) {
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_fullscreen_button")
                        ) {
                            Icon(
                                imageVector = if (isLandscape) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                contentDescription = if (isLandscape) "Exit Fullscreen" else "Fullscreen",
                                tint = TextPrimary
                            )
                        }
                    }
                }

                // Center Transport Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    IconButton(
                        onClick = {
                            val target = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(target)
                            seekFeedbackText = "-10s"
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("player_rewind_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Surface(
                        color = CinemaCrimson.copy(alpha = 0.94f),
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, CinemaGold.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .size(68.dp)
                            .clickable {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = TextPrimary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val maxDur = exoPlayer.duration.coerceAtLeast(0L)
                            val target = if (maxDur > 0L) {
                                (exoPlayer.currentPosition + 10_000L).coerceAtMost(maxDur)
                            } else {
                                exoPlayer.currentPosition + 10_000L
                            }
                            exoPlayer.seekTo(target)
                            seekFeedbackText = "+10s"
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("player_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Forward10,
                            contentDescription = "Forward 10 seconds",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Bottom Timeline Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    val safeDuration = durationMs.coerceAtLeast(1L)
                    Slider(
                        value = (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f),
                        onValueChange = { fraction ->
                            val newPos = (fraction * safeDuration).toLong()
                            currentPositionMs = newPos
                            exoPlayer.seekTo(newPos)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = CinemaGold,
                            activeTrackColor = CinemaCrimson,
                            inactiveTrackColor = TextSecondary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formatDurationMs(currentPositionMs),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (selectedSubtitle == "Off") {
                                formatDurationMs(durationMs)
                            } else {
                                "CC: $selectedSubtitle • ${formatDurationMs(durationMs)}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaGoldSoft
                        )
                    }
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
