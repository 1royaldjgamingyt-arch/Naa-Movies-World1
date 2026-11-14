package com.example.admin.ui.components

import android.util.Base64
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.admin.R

@DrawableRes
fun adminPosterDrawableRes(posterKey: String, language: String): Int {
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

@Composable
fun AdminPosterImage(
    posterUrl: String,
    posterKey: String,
    language: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallbackRes = adminPosterDrawableRes(posterKey, language)
    val decodedModel = remember(posterUrl) {
        if (posterUrl.startsWith("data:image/", ignoreCase = true)) {
            val commaIndex = posterUrl.indexOf(',')
            if (commaIndex != -1 && commaIndex + 1 < posterUrl.length) {
                runCatching {
                    Base64.decode(posterUrl.substring(commaIndex + 1), Base64.DEFAULT)
                }.getOrNull()
            } else {
                null
            }
        } else {
            posterUrl.takeIf { it.isNotBlank() }
        }
    }

    if (decodedModel != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(decodedModel)
                .crossfade(true)
                .build(),
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
