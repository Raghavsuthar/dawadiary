package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.CryptoManager
import com.example.security.PinVerifyResult
import com.example.security.SecurityPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PinSecurityTest {

    private lateinit var context: Context
    private lateinit var secPrefs: SecurityPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        secPrefs = SecurityPreferences(context)
        secPrefs.clearAllData()
    }

    @Test
    fun `pin hashing generates unique salted hashes for same pin`() {
        val pin = "1234"
        val salt1 = CryptoManager.generatePinSalt()
        val salt2 = CryptoManager.generatePinSalt()

        val hash1 = CryptoManager.hashPinWithSalt(pin, salt1)
        val hash2 = CryptoManager.hashPinWithSalt(pin, salt2)

        assertNotEquals("Different salts must produce different hashes", hash1, hash2)
        assertEquals("Same salt and pin must produce identical hash", hash1, CryptoManager.hashPinWithSalt(pin, salt1))
    }

    @Test
    fun `five consecutive incorrect pin attempts trigger lockout`() {
        secPrefs.setPin("4321")
        assertTrue(secPrefs.isPinSet())

        // 4 failed attempts should return Incorrect with decreasing attempts
        for (i in 1..4) {
            val res = secPrefs.verifyPin("0000")
            assertTrue(res is PinVerifyResult.Incorrect)
            assertEquals(5 - i, (res as PinVerifyResult.Incorrect).attemptsRemaining)
        }

        // 5th failed attempt should trigger lockout
        val lockoutRes = secPrefs.verifyPin("0000")
        assertTrue(lockoutRes is PinVerifyResult.LockedOut)
        assertTrue((lockoutRes as PinVerifyResult.LockedOut).secondsRemaining > 0)

        // Subsequent attempt within lockout period should remain locked out even if correct PIN is entered!
        val attemptDuringLockout = secPrefs.verifyPin("4321")
        assertTrue("Must reject attempts during lockout", attemptDuringLockout is PinVerifyResult.LockedOut)
    }

    @Test
    fun `correct pin resets failed attempt counter`() {
        secPrefs.setPin("7890")

        secPrefs.verifyPin("1111")
        secPrefs.verifyPin("2222")

        val success = secPrefs.verifyPin("7890")
        assertTrue(success is PinVerifyResult.Success)

        // After success, wrong attempt starts from 4 attempts left again
        val nextWrong = secPrefs.verifyPin("0000")
        assertTrue(nextWrong is PinVerifyResult.Incorrect)
        assertEquals(4, (nextWrong as PinVerifyResult.Incorrect).attemptsRemaining)
    }
}
