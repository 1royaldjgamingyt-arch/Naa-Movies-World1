package com.example.ui.components

import android.util.Base64
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.R
import com.example.data.model.Movie
import com.example.data.model.MovieCategory
import com.example.data.model.PopularCollection
import com.example.ui.theme.CinemaBrushes
import com.example.ui.theme.CinemaCrimson
import com.example.ui.theme.CinemaCrimsonBright
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldDeep
import com.example.ui.theme.CinemaGoldSoft
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianGlass
import com.example.ui.theme.ObsidianGlassElevated
import com.example.ui.theme.ObsidianGlassStrong
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

private object PosterDecodeCache {
    private val cache = object : LruCache<Int, ByteArray>(32) {}

    fun decode(dataUri: String): ByteArray? {
        val key = dataUri.hashCode()
        cache.get(key)?.let { return it }
        val commaIndex = dataUri.indexOf(',')
        if (commaIndex == -1 || commaIndex + 1 >= dataUri.length) return null
        val decoded = runCatching {
            Base64.decode(dataUri.substring(commaIndex + 1), Base64.DEFAULT)
        }.getOrNull() ?: return null
        cache.put(key, decoded)
        return decoded
    }
}

@DrawableRes
fun posterDrawableRes(posterKey: String, language: String): Int {
    return when {
        posterKey.contains("deccan", ignoreCase = true) ->
            R.drawable.img_poster_deccan_fort_1794669635334
        posterKey.contains("quantum", ignoreCase = true) ->
            R.drawable.img_poster_quantum_drift_1794669635127
        posterKey.contains("wildlands", ignoreCase = true) ->
            R.drawable.img_poster_wildlands_1794669636555
        posterKey.contains("telugu", ignoreCase = true) || language.equals("Telugu", ignoreCase = true) ->
            R.drawable.img_poster_telugu_1794667015025
        posterKey.contains("tamil", ignoreCase = true) || language.equals("Tamil", ignoreCase = true) ->
            R.drawable.img_poster_tamil_1794667014761
        posterKey.contains("hindi", ignoreCase = true) || language.equals("Hindi", ignoreCase = true) ->
            R.drawable.img_poster_hindi_1794667015021
        posterKey.contains("english", ignoreCase = true) || language.equals("English", ignoreCase = true) ->
            R.drawable.img_poster_english_1794667014724
        posterKey.contains("splash", ignoreCase = true) ->
            R.drawable.img_splash_hero_1794667014216
        else -> R.drawable.img_poster_telugu_1794667015025
    }
}

/**
 * Custom metallic gold & crimson cinema monogram drawn crisply at any size.
 */
@Composable
fun NaaMoviesBrandCrest(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val radius = w * 0.24f

        // Obsidian base crest
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF1D1D2C), ObsidianDeep)
            ),
            cornerRadius = CornerRadius(radius, radius)
        )

        // Metallic gold aperture ring
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(CinemaGoldSoft, CinemaGold, CinemaGoldDeep)
            ),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(width = w * 0.08f)
        )

        // Subtle crimson inner accent ribbon ("N" cinema bar)
        val nPath = Path().apply {
            moveTo(w * 0.27f, h * 0.72f)
            lineTo(w * 0.27f, h * 0.28f)
            lineTo(w * 0.56f, h * 0.72f)
            lineTo(w * 0.56f, h * 0.28f)
        }
        drawPath(
            path = nPath,
            brush = Brush.verticalGradient(listOf(CinemaCrimsonBright, CinemaCrimson)),
            style = Stroke(width = w * 0.105f, cap = StrokeCap.Round)
        )

        // Gold precision play prism
        val playPath = Path().apply {
            moveTo(w * 0.58f, h * 0.34f)
            lineTo(w * 0.80f, h * 0.50f)
            lineTo(w * 0.58f, h * 0.66f)
            close()
        }
        drawPath(
            path = playPath,
            brush = Brush.linearGradient(listOf(CinemaGoldSoft, CinemaGold))
        )

        // Specular glint top-left
        drawCircle(
            color = Color.White.copy(alpha = 0.28f),
            radius = w * 0.06f,
            center = Offset(w * 0.22f, h * 0.22f)
        )
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    containerColor: Color = ObsidianGlass,
    borderBrush: Brush = CinemaBrushes.glassRim,
    shadowElevation: Dp = 6.dp,
    content: @Composable () -> Unit
) {
    Surface(
        color = containerColor,
        shape = shape,
        border = BorderStroke(borderWidth, borderBrush),
        shadowElevation = shadowElevation,
        modifier = modifier,
        content = content
    )
}

@Composable
fun MoviePosterImage(
    posterUrl: String,
    posterKey: String,
    language: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    thumbnailMode: Boolean = true
) {
    val fallbackRes = posterDrawableRes(posterKey, language)
    val decodedModel = remember(posterUrl) {
        when {
            posterUrl.startsWith("data:image/", ignoreCase = true) ->
                PosterDecodeCache.decode(posterUrl)
            posterUrl.isNotBlank() -> posterUrl
            else -> null
        }
    }

    if (decodedModel != null) {
        val context = LocalContext.current
        val request = remember(decodedModel, thumbnailMode) {
            ImageRequest.Builder(context)
                .data(decodedModel)
                .crossfade(true)
                .apply {
                    if (thumbnailMode) {
                        size(440, 640)
                        scale(Scale.FILL)
                    }
                }
                .build()
        }
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            placeholder = painterResource(id = fallbackRes),
            error = painterResource(id = fallbackRes),
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

@Composable
fun MoviePosterCard(
    movie: Movie,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit,
    modifier: Modifier = Modifier,
    rankNumber: Int? = null
) {
    Column(
        modifier = modifier
            .width(148.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("movie_card_${movie.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black, spotColor = CinemaCrimson.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianCard)
                .border(1.dp, CinemaBrushes.glassRim, RoundedCornerShape(16.dp))
        ) {
            MoviePosterImage(
                posterUrl = movie.posterUrl,
                posterKey = movie.posterKey,
                language = movie.language,
                contentDescription = "${movie.title} poster",
                thumbnailMode = true,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom cinema vignette gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ObsidianBlack.copy(alpha = 0.65f),
                                ObsidianDeep.copy(alpha = 0.96f)
                            )
                        )
                    )
            )

            // Quality pill top-left
            Surface(
                color = if (movie.qualityBadge.contains("4K")) CinemaCrimson else ObsidianGlassStrong,
                shape = RoundedCornerShape(7.dp),
                border = BorderStroke(
                    0.75.dp,
                    if (movie.qualityBadge.contains("4K")) CinemaCrimsonBright else ObsidianBorder
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = movie.qualityBadge.replace(" UHD", "").replace(" HD", ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }

            // Metallic gold star rating pill top-right
            Surface(
                color = ObsidianGlassStrong,
                shape = RoundedCornerShape(7.dp),
                border = BorderStroke(0.75.dp, CinemaGold.copy(alpha = 0.42f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", movie.rating),
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaGoldSoft
                    )
                }
            }

            // Optional Trending Rank Badge
            if (rankNumber != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp, bottom = 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CinemaBrushes.goldMetallic)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#$rankNumber",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = ObsidianDeep
                    )
                }
            }

            // Language glass tag + Watchlist bookmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                Surface(
                    color = ObsidianGlassElevated,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.75.dp, ObsidianBorder)
                ) {
                    Text(
                        text = movie.language.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = if (isSaved) CinemaGold.copy(alpha = 0.2f) else ObsidianGlassStrong,
                    shape = CircleShape,
                    border = BorderStroke(0.75.dp, if (isSaved) CinemaGold else ObsidianBorder)
                ) {
                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("bookmark_card_${movie.id}")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove from Watchlist" else "Add to Watchlist",
                            tint = if (isSaved) CinemaGold else TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Text(
            text = "${movie.releaseYear} • ${movie.category}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun MovieSectionRow(
    title: String,
    movies: List<Movie>,
    savedIds: Set<String>,
    onMovieClick: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    onSeeAllClick: (() -> Unit)? = null,
    accentColor: Color = CinemaCrimson,
    showRankBadges: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(listOf(accentColor, CinemaGold))
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }

            if (onSeeAllClick != null) {
                Surface(
                    color = ObsidianGlass,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(0.8.dp, ObsidianBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(onClick = onSeeAllClick)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Explore",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaGoldSoft
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "See all $title",
                            tint = CinemaGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = movies,
                key = { _, item -> item.id },
                contentType = { _, _ -> "movie_poster_card" }
            ) { index, movie ->
                MoviePosterCard(
                    movie = movie,
                    isSaved = movie.id in savedIds,
                    rankNumber = if (showRankBadges && index < 6) index + 1 else null,
                    onClick = { onMovieClick(movie) },
                    onToggleWatchlist = { onToggleWatchlist(movie) }
                )
            }
        }
    }
}

@Composable
fun CategoryNavigationStrip(
    onCategoryClick: (MovieCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
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
                text = "Browse by Genre",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = MovieCategory.uploadCategories,
                key = { it.name },
                contentType = { "category_pill" }
            ) { category ->
                val icon = categoryIcon(category)
                GlassSurface(
                    shape = RoundedCornerShape(14.dp),
                    containerColor = ObsidianGlass,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onCategoryClick(category) }
                        .testTag("home_category_tile_${category.name.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Surface(
                            color = CinemaCrimson.copy(alpha = 0.16f),
                            shape = CircleShape,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PopularCollectionsSection(
    collections: List<PopularCollection>,
    onCollectionClick: (PopularCollection) -> Unit,
    modifier: Modifier = Modifier
) {
    if (collections.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CinemaBrushes.goldMetallic)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Popular Collections",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(
                items = collections,
                key = { it.id },
                contentType = { "popular_collection" }
            ) { collection ->
                val leadMovie = collection.movies.firstOrNull()
                val secondMovie = collection.movies.getOrNull(1) ?: leadMovie
                GlassSurface(
                    shape = RoundedCornerShape(18.dp),
                    containerColor = ObsidianGlassElevated,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .width(268.dp)
                        .height(142.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onCollectionClick(collection) }
                        .testTag("collection_card_${collection.id}")
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (leadMovie != null) {
                            MoviePosterImage(
                                posterUrl = leadMovie.posterUrl,
                                posterKey = leadMovie.posterKey,
                                language = leadMovie.language,
                                contentDescription = collection.title,
                                thumbnailMode = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Cinematic obsidian-to-transparent sweep for editorial legibility
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            ObsidianDeep.copy(alpha = 0.96f),
                                            ObsidianBlack.copy(alpha = 0.85f),
                                            ObsidianBlack.copy(alpha = 0.35f)
                                        )
                                    )
                                )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Surface(
                                    color = CinemaGold.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.75.dp, CinemaGold.copy(alpha = 0.55f))
                                ) {
                                    Text(
                                        text = collection.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CinemaGoldSoft,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = collection.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${collection.movies.size} Curated Films",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Mini stacked poster preview on right
                            if (leadMovie != null && secondMovie != null) {
                                Box(
                                    modifier = Modifier
                                        .width(74.dp)
                                        .height(104.dp)
                                ) {
                                    MoviePosterImage(
                                        posterUrl = secondMovie.posterUrl,
                                        posterKey = secondMovie.posterKey,
                                        language = secondMovie.language,
                                        contentDescription = null,
                                        thumbnailMode = true,
                                        modifier = Modifier
                                            .size(56.dp, 82.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                                    )
                                    MoviePosterImage(
                                        posterUrl = leadMovie.posterUrl,
                                        posterKey = leadMovie.posterKey,
                                        language = leadMovie.language,
                                        contentDescription = null,
                                        thumbnailMode = true,
                                        modifier = Modifier
                                            .size(60.dp, 88.dp)
                                            .align(Alignment.BottomStart)
                                            .clip(RoundedCornerShape(9.dp))
                                            .border(1.2.dp, CinemaGold.copy(alpha = 0.7f), RoundedCornerShape(9.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSpotlightBanner(
    featuredMovies: List<Movie>,
    savedIds: Set<String>,
    onPlayClick: (Movie) -> Unit,
    onDetailsClick: (Movie) -> Unit,
    onToggleWatchlist: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (featuredMovies.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { featuredMovies.size })

    LaunchedEffect(featuredMovies.size) {
        if (featuredMovies.size > 1) {
            while (true) {
                delay(5500L)
                val next = (pagerState.currentPage + 1) % featuredMovies.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(445.dp)
        ) { page ->
            val movie = featuredMovies[page]
            val isSaved = movie.id in savedIds
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDetailsClick(movie) }
            ) {
                MoviePosterImage(
                    posterUrl = movie.posterUrl,
                    posterKey = movie.posterKey,
                    language = movie.language,
                    contentDescription = "${movie.title} spotlight banner",
                    thumbnailMode = false,
                    modifier = Modifier.fillMaxSize()
                )

                // Multi-stop radial + vertical Obsidian cinema vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to ObsidianDeep.copy(alpha = 0.72f),
                                    0.28f to Color.Transparent,
                                    0.62f to ObsidianBlack.copy(alpha = 0.72f),
                                    1.0f to ObsidianBlack
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = CinemaGold.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(7.dp),
                            border = BorderStroke(1.dp, CinemaGold.copy(alpha = 0.65f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "FEATURED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaGoldSoft
                                )
                            }
                        }

                        Surface(
                            color = CinemaCrimson,
                            shape = RoundedCornerShape(7.dp)
                        ) {
                            Text(
                                text = movie.language.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = ObsidianGlassStrong,
                            shape = RoundedCornerShape(7.dp),
                            border = BorderStroke(0.8.dp, ObsidianBorder)
                        ) {
                            Text(
                                text = movie.qualityBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", movie.rating),
                                style = MaterialTheme.typography.labelLarge,
                                color = CinemaGoldSoft
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp, lineHeight = 35.sp),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "${movie.releaseYear} • ${movie.genres.joinToString(" • ")} • ${movie.durationMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                .weight(1.15f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CinemaBrushes.crimsonAction)
                                .testTag("hero_play_${movie.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Watch Now",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = { onToggleWatchlist(movie) },
                            border = BorderStroke(1.dp, if (isSaved) CinemaGold else Color.White.copy(alpha = 0.34f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ObsidianGlass,
                                contentColor = if (isSaved) CinemaGoldSoft else TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("hero_watchlist_${movie.id}")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = null,
                                tint = if (isSaved) CinemaGold else TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSaved) "In Watchlist" else "Watchlist",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }

        // Animated capsule dot indicators
        if (featuredMovies.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .animateContentSize()
            ) {
                repeat(featuredMovies.size) { index ->
                    val selected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (selected) 24.dp else 7.dp,
                        animationSpec = tween(220),
                        label = "banner_dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (selected) CinemaBrushes.goldMetallic
                                else Brush.horizontalGradient(listOf(ObsidianBorder, ObsidianBorder))
                            )
                    )
                }
            }
        }
    }
}

private fun categoryIcon(category: MovieCategory): ImageVector = when (category) {
    MovieCategory.ACTION -> Icons.Filled.LocalFireDepartment
    MovieCategory.THRILLER -> Icons.Filled.Psychology
    MovieCategory.DRAMA -> Icons.Filled.TheaterComedy
    MovieCategory.SCI_FI -> Icons.Filled.RocketLaunch
    MovieCategory.ROMANCE -> Icons.Filled.Favorite
    MovieCategory.COMEDY -> Icons.Filled.SentimentVerySatisfied
    MovieCategory.ADVENTURE -> Icons.Filled.Explore
    MovieCategory.DOCUMENTARY -> Icons.Filled.Public
    MovieCategory.ALL -> Icons.Filled.Movie
}
