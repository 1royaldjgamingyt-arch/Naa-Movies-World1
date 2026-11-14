package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.ads.metadata.MetaData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UnityAdsManager {

    private const val TAG = "UnityAdsManager"
    private const val PREFS_NAME = "naa_movies_ad_prefs"
    private const val KEY_AD_FREE_UNTIL_MS = "ad_free_until_ms"
    private const val INTERSTITIAL_COOLDOWN_MS = 90_000L
    private const val INTERSTITIAL_EVERY_N_TRANSITIONS = 3
    private const val REWARD_PASS_MINUTES = 30

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isInterstitialReady = MutableStateFlow(false)
    val isInterstitialReady: StateFlow<Boolean> = _isInterstitialReady.asStateFlow()

    private val _isRewardedReady = MutableStateFlow(false)
    val isRewardedReady: StateFlow<Boolean> = _isRewardedReady.asStateFlow()

    private val _adFreeRemainingMinutes = MutableStateFlow(0)
    val adFreeRemainingMinutes: StateFlow<Int> = _adFreeRemainingMinutes.asStateFlow()

    private var initAttempted = false
    private var navigationTransitionCount = 0
    private var transitionsSinceLastInterstitial = 0
    private var lastInterstitialShownMs = 0L

    val isSdkConfigured: Boolean
        get() = BuildConfig.ADS_ENABLED && isValidGameId(BuildConfig.UNITY_GAME_ID)

    fun isValidGameId(gameId: String): Boolean {
        val trimmed = gameId.trim()
        return trimmed.isNotEmpty() &&
            trimmed != "0000000" &&
            trimmed.all { it.isDigit() }
    }

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        refreshAdFreeStatus(appContext)

        if (!BuildConfig.ADS_ENABLED || initAttempted) return
        initAttempted = true

        val gameId = BuildConfig.UNITY_GAME_ID.trim()
        if (!isValidGameId(gameId)) {
            Log.i(
                TAG,
                "Unity Ads waiting for valid numeric UNITY_GAME_ID (current placeholder: '$gameId')."
            )
            return
        }

        runCatching {
            val gdprMetaData = MetaData(appContext)
            gdprMetaData["gdpr.consent"] = true
            gdprMetaData.commit()

            UnityAds.initialize(
                appContext,
                gameId,
                BuildConfig.UNITY_TEST_MODE,
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        Log.i(TAG, "Unity Ads initialized (testMode=${BuildConfig.UNITY_TEST_MODE})")
                        _isInitialized.value = true
                        preloadInterstitial()
                        preloadRewarded()
                    }

                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError?,
                        message: String?
                    ) {
                        Log.w(TAG, "Unity Ads initialization failed [$error]: $message")
                        _isInitialized.value = false
                    }
                }
            )
        }.onFailure { err ->
            Log.w(TAG, "Unity Ads SDK unavailable in current runtime: ${err.message}")
        }
    }

    fun refreshAdFreeStatus(context: Context): Int {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val untilMs = prefs.getLong(KEY_AD_FREE_UNTIL_MS, 0L)
        val remainingMs = (untilMs - System.currentTimeMillis()).coerceAtLeast(0L)
        val minutes = if (remainingMs > 0L) ((remainingMs + 59_999L) / 60_000L).toInt() else 0
        _adFreeRemainingMinutes.value = minutes
        return minutes
    }

    fun grantAdFreePass(context: Context, minutes: Int = REWARD_PASS_MINUTES): Int {
        val untilMs = System.currentTimeMillis() + minutes * 60_000L
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_AD_FREE_UNTIL_MS, untilMs)
            .apply()
        _adFreeRemainingMinutes.value = minutes
        return minutes
    }

    fun isAdFreeActive(context: Context): Boolean = refreshAdFreeStatus(context) > 0

    fun preloadInterstitial() {
        if (!_isInitialized.value) return
        val placementId = BuildConfig.UNITY_INTERSTITIAL_PLACEMENT
        runCatching {
            UnityAds.load(
                placementId,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placement: String?) {
                        _isInterstitialReady.value = true
                    }

                    override fun onUnityAdsFailedToLoad(
                        placement: String?,
                        error: UnityAds.UnityAdsLoadError?,
                        message: String?
                    ) {
                        _isInterstitialReady.value = false
                        Log.d(TAG, "Interstitial failed to load [$error]: $message")
                    }
                }
            )
        }
    }

    fun preloadRewarded() {
        if (!_isInitialized.value) return
        val placementId = BuildConfig.UNITY_REWARDED_PLACEMENT
        runCatching {
            UnityAds.load(
                placementId,
                object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placement: String?) {
                        _isRewardedReady.value = true
                    }

                    override fun onUnityAdsFailedToLoad(
                        placement: String?,
                        error: UnityAds.UnityAdsLoadError?,
                        message: String?
                    ) {
                        _isRewardedReady.value = false
                        Log.d(TAG, "Rewarded ad failed to load [$error]: $message")
                    }
                }
            )
        }
    }

    fun shouldTriggerInterstitial(
        nowMs: Long,
        transitionsSinceLast: Int,
        lastShownMs: Long,
        adFreeActive: Boolean,
        interstitialReady: Boolean
    ): Boolean {
        if (adFreeActive || !interstitialReady) return false
        if (transitionsSinceLast < INTERSTITIAL_EVERY_N_TRANSITIONS) return false
        return lastShownMs == 0L || (nowMs - lastShownMs) >= INTERSTITIAL_COOLDOWN_MS
    }

    fun recordNavigationAndMaybeShowInterstitial(activity: Activity?, reason: String) {
        navigationTransitionCount++
        transitionsSinceLastInterstitial++

        if (activity == null || !isSdkConfigured || !_isInitialized.value) return
        val nowMs = System.currentTimeMillis()
        val adFree = isAdFreeActive(activity)

        if (!shouldTriggerInterstitial(
                nowMs = nowMs,
                transitionsSinceLast = transitionsSinceLastInterstitial,
                lastShownMs = lastInterstitialShownMs,
                adFreeActive = adFree,
                interstitialReady = _isInterstitialReady.value
            )
        ) {
            if (!_isInterstitialReady.value && !adFree) {
                preloadInterstitial()
            }
            return
        }

        val placementId = BuildConfig.UNITY_INTERSTITIAL_PLACEMENT
        _isInterstitialReady.value = false
        lastInterstitialShownMs = nowMs
        transitionsSinceLastInterstitial = 0

        runCatching {
            UnityAds.show(
                activity,
                placementId,
                UnityAdsShowOptions(),
                object : IUnityAdsShowListener {
                    override fun onUnityAdsShowFailure(
                        placement: String?,
                        error: UnityAds.UnityAdsShowError?,
                        message: String?
                    ) {
                        Log.w(TAG, "Interstitial show failed ($reason): $message")
                        preloadInterstitial()
                    }

                    override fun onUnityAdsShowStart(placement: String?) = Unit
                    override fun onUnityAdsShowClick(placement: String?) = Unit

                    override fun onUnityAdsShowComplete(
                        placement: String?,
                        state: UnityAds.UnityAdsShowCompletionState?
                    ) {
                        preloadInterstitial()
                    }
                }
            )
        }.onFailure {
            preloadInterstitial()
        }
    }

    fun showRewardedAd(
        activity: Activity?,
        onRewardGranted: (grantedMinutes: Int) -> Unit,
        onUnavailable: (message: String) -> Unit
    ) {
        if (activity == null) {
            onUnavailable("Unable to display ad outside active screen.")
            return
        }

        if (!isSdkConfigured) {
            onUnavailable("Configure your Unity Game ID (UNITY_GAME_ID) to enable live Rewarded Ads.")
            return
        }

        if (!_isInitialized.value) {
            initialize(activity)
            onUnavailable("Initializing Unity Ads… Please try again in a moment.")
            return
        }

        if (!_isRewardedReady.value) {
            preloadRewarded()
            onUnavailable("Rewarded ad is loading. Please try again in a few seconds.")
            return
        }

        val placementId = BuildConfig.UNITY_REWARDED_PLACEMENT
        _isRewardedReady.value = false

        runCatching {
            UnityAds.show(
                activity,
                placementId,
                UnityAdsShowOptions(),
                object : IUnityAdsShowListener {
                    override fun onUnityAdsShowFailure(
                        placement: String?,
                        error: UnityAds.UnityAdsShowError?,
                        message: String?
                    ) {
                        preloadRewarded()
                        onUnavailable("Rewarded ad unavailable right now. Please try again later.")
                    }

                    override fun onUnityAdsShowStart(placement: String?) = Unit
                    override fun onUnityAdsShowClick(placement: String?) = Unit

                    override fun onUnityAdsShowComplete(
                        placement: String?,
                        state: UnityAds.UnityAdsShowCompletionState?
                    ) {
                        preloadRewarded()
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            val granted = grantAdFreePass(activity, REWARD_PASS_MINUTES)
                            onRewardGranted(granted)
                        } else {
                            onUnavailable("Complete watching the rewarded video to unlock the 30m Ad-Free Pass.")
                        }
                    }
                }
            )
        }.onFailure {
            preloadRewarded()
            onUnavailable("Unable to open rewarded ad right now.")
        }
    }

    internal fun resetForTest() {
        initAttempted = false
        navigationTransitionCount = 0
        transitionsSinceLastInterstitial = 0
        lastInterstitialShownMs = 0L
        _isInitialized.value = false
        _isInterstitialReady.value = false
        _isRewardedReady.value = false
        _adFreeRemainingMinutes.value = 0
    }
}
