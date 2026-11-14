package com.example.ads

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.BuildConfig
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UnityAdsManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        UnityAdsManager.resetForTest()
        context.getSharedPreferences("naa_movies_ad_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun tearDown() {
        UnityAdsManager.resetForTest()
    }

    @Test
    fun `user app enables ads build config flag and validates numeric Unity Game IDs`() {
        assertTrue(BuildConfig.ADS_ENABLED)
        assertFalse(UnityAdsManager.isValidGameId(""))
        assertFalse(UnityAdsManager.isValidGameId("0000000"))
        assertFalse(UnityAdsManager.isValidGameId("abc1234"))
        assertTrue(UnityAdsManager.isValidGameId("5712345"))
        assertTrue(UnityAdsManager.isValidGameId(" 5712345 "))
    }

    @Test
    fun `shouldTriggerInterstitial enforces transition count cooldown adFree and readiness`() {
        val now = 200_000L

        assertFalse(
            UnityAdsManager.shouldTriggerInterstitial(
                nowMs = now,
                transitionsSinceLast = 2,
                lastShownMs = 0L,
                adFreeActive = false,
                interstitialReady = true
            )
        )

        assertFalse(
            UnityAdsManager.shouldTriggerInterstitial(
                nowMs = now,
                transitionsSinceLast = 3,
                lastShownMs = now - 30_000L,
                adFreeActive = false,
                interstitialReady = true
            )
        )

        assertFalse(
            UnityAdsManager.shouldTriggerInterstitial(
                nowMs = now,
                transitionsSinceLast = 4,
                lastShownMs = 0L,
                adFreeActive = true,
                interstitialReady = true
            )
        )

        assertFalse(
            UnityAdsManager.shouldTriggerInterstitial(
                nowMs = now,
                transitionsSinceLast = 4,
                lastShownMs = 0L,
                adFreeActive = false,
                interstitialReady = false
            )
        )

        assertTrue(
            UnityAdsManager.shouldTriggerInterstitial(
                nowMs = now,
                transitionsSinceLast = 3,
                lastShownMs = 0L,
                adFreeActive = false,
                interstitialReady = true
            )
        )

        assertTrue(
            UnityAdsManager.shouldTriggerInterstitial(
                nowMs = now,
                transitionsSinceLast = 3,
                lastShownMs = now - 90_000L,
                adFreeActive = false,
                interstitialReady = true
            )
        )
    }

    @Test
    fun `grantAdFreePass persists active ad-free status and remaining minutes`() {
        assertFalse(UnityAdsManager.isAdFreeActive(context))
        assertEquals(0, UnityAdsManager.adFreeRemainingMinutes.value)

        val granted = UnityAdsManager.grantAdFreePass(context, minutes = 30)
        assertEquals(30, granted)
        assertTrue(UnityAdsManager.isAdFreeActive(context))
        assertTrue(UnityAdsManager.refreshAdFreeStatus(context) in 29..30)
        assertTrue(UnityAdsManager.adFreeRemainingMinutes.value in 29..30)
    }
}
