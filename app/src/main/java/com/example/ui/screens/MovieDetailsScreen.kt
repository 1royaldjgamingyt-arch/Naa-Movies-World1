package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ads.AdFreeCinemaPassCard
import com.example.ads.UnityBannerAdSlot
import com.example.data.model.CastCredit
import com.example.data.model.Movie
import com.example.ui.components.GlassSurface
import com.example.ui.components.MoviePosterImage
import com.example.ui.components.MovieSectionRow
import com.example.ui.theme.CinemaBrushes
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldSoft
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianGlass
import com.example.ui.theme.ObsidianGlassStrong
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieDetailsScreen(
    movie: Movie,
    isSaved: Boolean,
    resumePositionMs: Long,
    relatedMovies: List<Movie>,
    savedIds: Set<String>,
    onBackClick: () -> Unit,
    onPlayClick: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    onRelatedMovieClick: (Movie) -> Unit,
    adFreeMinutes: Int = 0,
    onWatchRewardedAd: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val runtimeFormatted = remember(movie.durationMinutes) { formatRuntime(movie.durationMinutes) }
    val qualityBadges = remember(movie.qualityBadge) { movie.availableQualityBadges() }
    val castAndCrew = remember(movie) { movie.resolvedCastAndCrew() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 40.dp),
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .testTag("details_lazy_column")
        ) {
            // Atmospheric Backdrop + High-Resolution Framed Poster Header
            item(key = "details_backdrop") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    MoviePosterImage(
                        posterUrl = movie.posterUrl,
                        posterKey = movie.posterKey,
                        language = movie.language,
                        contentDescription = null,
                        thumbnailMode = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.45f)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to ObsidianDeep.copy(alpha = 0.78f),
                                        0.35f to ObsidianBlack.copy(alpha = 0.42f),
                                        0.78f to ObsidianBlack.copy(alpha = 0.92f),
                                        1.0f to ObsidianBlack
                                    )
                                )
                            )
                    )

                    // Floating Glass Header Controls
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        GlassSurface(shape = CircleShape, containerColor = ObsidianGlassStrong) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("details_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary
                                )
                            }
                        }

                        GlassSurface(shape = CircleShape, containerColor = ObsidianGlassStrong) {
                            IconButton(
                                onClick = { onToggleWatchlist(movie) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("details_top_bookmark")
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = if (isSaved) "Remove from Watchlist" else "Add to Watchlist",
                                    tint = if (isSaved) CinemaGold else TextPrimary
                                )
                            }
                        }
                    }

                    // Framed High-Resolution Poster + Editorial Title Block
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(136.dp)
                                .aspectRatio(0.68f)
                                .shadow(18.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black, spotColor = CinemaGold.copy(alpha = 0.3f))
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, CinemaBrushes.goldMetallic, RoundedCornerShape(16.dp))
                        ) {
                            MoviePosterImage(
                                posterUrl = movie.posterUrl,
                                posterKey = movie.posterKey,
                                language = movie.language,
                                contentDescription = "${movie.title} poster",
                                thumbnailMode = false,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = CinemaCrimson,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = movie.language.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    color = ObsidianGlassStrong,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.8.dp, CinemaGold.copy(alpha = 0.55f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = CinemaGold,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = String.format(Locale.US, "%.1f", movie.rating),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CinemaGoldSoft
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${movie.releaseYear} • ${movie.category}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CinemaGoldSoft
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = runtimeFormatted,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                if (movie.subtitleLanguages.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.ClosedCaption,
                                            contentDescription = "Subtitles available",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = movie.subtitleLanguages.joinToString("/"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Available Video Quality Badges
            item(key = "details_quality_badges") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Available Quality:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    qualityBadges.forEachIndexed { index, tier ->
                        Surface(
                            color = if (index == 0) CinemaGold.copy(alpha = 0.16f) else ObsidianGlass,
                            shape = RoundedCornerShape(7.dp),
                            border = BorderStroke(
                                0.8.dp,
                                if (index == 0) CinemaGold.copy(alpha = 0.7f) else ObsidianBorder
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Hd,
                                    contentDescription = null,
                                    tint = if (index == 0) CinemaGold else TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = tier,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (index == 0) CinemaGoldSoft else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Primary Watch Now & Watchlist CTAs
            item(key = "details_actions") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { onPlayClick(movie) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextPrimary
                        ),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CinemaBrushes.crimsonAction)
                            .testTag("watch_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (resumePositionMs > 5000L) {
                                stringResource(R.string.resume_watching)
                            } else {
                                stringResource(R.string.watch_now_hd)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { onToggleWatchlist(movie) },
                        border = BorderStroke(1.dp, if (isSaved) CinemaGold else ObsidianBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ObsidianGlass,
                            contentColor = if (isSaved) CinemaGoldSoft else TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("watchlist_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            tint = if (isSaved) CinemaGold else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSaved) stringResource(R.string.in_watchlist) else stringResource(R.string.add_to_watchlist),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            // Storyline & Genre Tags
            item(key = "details_synopsis") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Storyline",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        movie.genres.forEach { genre ->
                            GlassSurface(
                                shape = RoundedCornerShape(9.dp),
                                containerColor = ObsidianElevated
                            ) {
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Cast & Crew Section
            item(key = "details_cast_crew") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CinemaGold)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Cast & Crew",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(castAndCrew, key = { "${it.name}_${it.role}" }) { credit ->
                            CastCreditCard(credit = credit)
                        }
                    }
                }
            }

            // License Verification Card
            item(key = "details_license") {
                GlassSurface(
                    shape = RoundedCornerShape(14.dp),
                    containerColor = ObsidianCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Surface(
                            color = EmeraldVerified.copy(alpha = 0.16f),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified License",
                                tint = EmeraldVerified,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Authorized Cinema Stream Verified",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldVerified
                            )
                            Text(
                                text = movie.licenseNote.ifBlank { "Authorized Studio / Open Film Stream" },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Optional Rewarded Ad-Free Pass + Banner Slot
            if (onWatchRewardedAd != null) {
                item(key = "details_rewarded_pass") {
                    AdFreeCinemaPassCard(
                        adFreeMinutes = adFreeMinutes,
                        onWatchRewardedAd = onWatchRewardedAd,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 14.dp)
                    )
                }
            }

            item(key = "details_unity_banner") {
                UnityBannerAdSlot(
                    adFreeMinutes = adFreeMinutes,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            // Related Movies
            if (relatedMovies.isNotEmpty()) {
                item(key = "details_related") {
                    MovieSectionRow(
                        title = "Related Movies",
                        movies = relatedMovies,
                        savedIds = savedIds,
                        accentColor = CinemaGold,
                        onMovieClick = onRelatedMovieClick,
                        onToggleWatchlist = onToggleWatchlist
                    )
                }
            }
        }
    }
}

@Composable
private fun CastCreditCard(credit: CastCredit) {
    GlassSurface(
        shape = RoundedCornerShape(14.dp),
        containerColor = ObsidianGlass,
        modifier = Modifier.width(156.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Surface(
                color = CinemaCrimson.copy(alpha = 0.18f),
                shape = CircleShape,
                border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.45f)),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = CinemaGoldSoft,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = credit.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = credit.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatRuntime(minutes: Long): String {
    val hrs = minutes / 60
    val mins = minutes % 60
    return if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
}
