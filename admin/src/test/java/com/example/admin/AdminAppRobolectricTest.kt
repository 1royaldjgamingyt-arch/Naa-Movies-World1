package com.example.admin

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdminAppRobolectricTest {

    @Test
    fun `read admin app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Naa Movies Admin", appName)
    }

    @Test
    fun `admin app keeps ads completely disabled`() {
        assertFalse(BuildConfig.ADS_ENABLED)
        assertTrue(BuildConfig.UNITY_GAME_ID.isEmpty())
    }
}
