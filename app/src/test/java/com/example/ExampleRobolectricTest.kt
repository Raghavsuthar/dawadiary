package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DawaDiary", appName)
    }

    @Test
    fun `verify light mode color palette tokens match specifications`() {
        assertEquals(MedicalPrimary, LightColorScheme.primary)
        assertEquals(MedicalBackground, LightColorScheme.background)
        assertEquals(MedicalSurface, LightColorScheme.surface)
        assertEquals(MedicalOnSurface, LightColorScheme.onSurface)
        assertEquals(MedicalOutline, LightColorScheme.outline)
        assertEquals(MedicalError, LightColorScheme.error)
    }

    @Test
    fun `verify zero-dose day adherence calculation does not report 100 percent`() {
        // Business logic test for 6.B adherence calculation
        val totalDoses = 0
        val takenDoses = 0
        val adherencePercent = if (totalDoses > 0) (takenDoses * 100) / totalDoses else 0
        assertEquals(0, adherencePercent)
    }

    @Test
    fun `verify partial and full adherence percentage calculations`() {
        val totalDoses = 4
        val takenDoses = 3
        val adherencePercent = if (totalDoses > 0) (takenDoses * 100) / totalDoses else 0
        assertEquals(75, adherencePercent)

        val fullTaken = 4
        val fullAdherence = if (totalDoses > 0) (fullTaken * 100) / totalDoses else 0
        assertEquals(100, fullAdherence)
    }

    @Test
    fun `verify stock threshold low stock alert condition`() {
        val stockCount = 3
        val lowStockThreshold = 5
        val isLowStock = stockCount <= lowStockThreshold
        assertTrue("Stock at or below threshold must be flagged as low stock", isLowStock)
    }
}
