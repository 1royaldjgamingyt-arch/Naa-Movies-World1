package com.example.admin.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

object PosterImageEncoder {
    private const val MAX_DIMENSION_PX = 360
    private const val MAX_BASE64_LENGTH = 140_000

    suspend fun encodePosterToDataUri(context: Context, uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, bounds)
                }

                var sampleSize = 1
                val maxSide = max(bounds.outWidth, bounds.outHeight)
                if (maxSide > MAX_DIMENSION_PX) {
                    var half = maxSide / 2
                    while (half / sampleSize >= MAX_DIMENSION_PX) {
                        sampleSize *= 2
                    }
                }

                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decoded = resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                } ?: return@withContext Result.failure(IllegalStateException("Unable to read selected image"))

                val scaleRatio = if (max(decoded.width, decoded.height) > MAX_DIMENSION_PX) {
                    MAX_DIMENSION_PX.toFloat() / max(decoded.width, decoded.height).toFloat()
                } else {
                    1f
                }

                val scaled = if (scaleRatio < 1f) {
                    val targetW = (decoded.width * scaleRatio).toInt().coerceAtLeast(1)
                    val targetH = (decoded.height * scaleRatio).toInt().coerceAtLeast(1)
                    Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
                        if (it !== decoded) decoded.recycle()
                    }
                } else {
                    decoded
                }

                var quality = 78
                var encodedDataUri: String
                do {
                    val out = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                    encodedDataUri = "data:image/jpeg;base64,$b64"
                    quality -= 12
                } while (encodedDataUri.length > MAX_BASE64_LENGTH && quality >= 30)

                scaled.recycle()
                if (encodedDataUri.length > 220_000) {
                    return@withContext Result.failure(IllegalStateException("Poster image is too large; choose a smaller image or HTTPS URL."))
                }
                Result.success(encodedDataUri)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
