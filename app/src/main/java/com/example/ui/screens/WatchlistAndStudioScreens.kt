package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Movie
import com.example.data.model.WatchlistItem
import com.example.data.model.toMovie
import com.example.ui.components.GlassSurface
import com.example.ui.components.MoviePosterImage
import com.example.ui.theme.CinemaBrushes
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianGlass
import com.example.ui.theme.ObsidianGlassElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.UiState
import java.util.Locale

@Composable
fun WatchlistScreen(
    watchlistState: UiState<List<WatchlistItem>>,
    onMovieClick: (Movie) -> Unit,
    onPlayClick: (Movie) -> Unit,
    onRemoveFromWatchlist: (Movie) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CinemaBrushes.goldMetallic)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "My Watchlist",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    val count = (watchlistState as? UiState.Success)?.data?.size ?: 0
                    Text(
                        text = "$count ${if (count == 1) "saved title" else "saved titles"} on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            val count = (watchlistState as? UiState.Success)?.data?.size ?: 0
            Surface(
                color = ObsidianGlass,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(0.8.dp, CinemaGold.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "$count Saved",
                    style = MaterialTheme.typography.labelMedium,
                    color = CinemaGoldSoft,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        when (watchlistState) {
            is UiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(color = CinemaGold)
                }
            }

            is UiState.Error -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = watchlistState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = CinemaCrimson,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is UiState.Success -> {
                val items = watchlistState.data
                if (items.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, end = 24.dp, bottom = 96.dp)
                    ) {
                        GlassSurface(
                            shape = RoundedCornerShape(22.dp),
                            containerColor = ObsidianGlassElevated,
                            shadowElevation = 10.dp,
                            modifier = Modifier.widthIn(max = 420.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(28.dp)
                            ) {
                                Surface(
                                    color = CinemaCrimson.copy(alpha = 0.16f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.45f)),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmarks,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.empty_watchlist_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.empty_watchlist_body),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onExploreClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = TextPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CinemaBrushes.crimsonAction)
                                ) {
                                    Text(
                                        text = "Browse Movies",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 108.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = items,
                            key = { it.movieId },
                            contentType = { "watchlist_row" }
                        ) { item ->
                            val movie = item.toMovie()
                            WatchlistRowCard(
                                item = item,
                                onClick = { onMovieClick(movie) },
                                onPlay = { onPlayClick(movie) },
                                onRemove = { onRemoveFromWatchlist(movie) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistRowCard(
    item: WatchlistItem,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    GlassSurface(
        shape = RoundedCornerShape(16.dp),
        containerColor = ObsidianGlassElevated,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("watchlist_row_${item.movieId}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            MoviePosterImage(
                posterUrl = item.posterUrl,
                posterKey = item.posterKey,
                language = item.language,
                contentDescription = "${item.title} poster",
                thumbnailMode = true,
                modifier = Modifier
                    .width(78.dp)
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(11.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = CinemaCrimson.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(0.6.dp, CinemaCrimson.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = item.language.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaCrimson,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        color = CinemaGold.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(
                            text = item.qualityBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGoldSoft,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", item.rating)} • ${item.releaseYear} • ${item.durationMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("watchlist_play_${item.movieId}")
            ) {
                Surface(
                    color = CinemaCrimson,
                    shape = CircleShape,
                    border = BorderStroke(0.8.dp, CinemaGold.copy(alpha = 0.5f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play ${item.title}",
                        tint = TextPrimary,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("watchlist_remove_${item.movieId}")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Remove ${item.title}",
                    tint = TextSecondary
                )
            }
        }
    }
}
