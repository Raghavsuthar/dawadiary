package com.example

import com.example.security.CryptoManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPinHashing() {
        val pin = "1234"
        val hash1 = CryptoManager.hashPin(pin)
        val hash2 = CryptoManager.hashPin(pin)
        assertEquals(hash1, hash2)
        assertNotEquals(pin, hash1)
        assertEquals(64, hash1.length)
    }

    @Test
    fun testPasswordBackupEncryptionRoundTrip() {
        val originalText = "Patient health records backup test 2026"
        val originalBytes = originalText.toByteArray(Charsets.UTF_8)
        val password = "StrongSecretPassword123"

        val encrypted = CryptoManager.encryptBackup(originalBytes, password)
        assertNotNull(encrypted)
        assertTrue(encrypted.size > originalBytes.size)

        val decrypted = CryptoManager.decryptBackup(encrypted, password)
        val decryptedText = String(decrypted, Charsets.UTF_8)
        assertEquals(originalText, decryptedText)
    }

    @Test(expected = Exception::class)
    fun testPasswordBackupDecryptionFailsWithWrongPassword() {
        val originalText = "Medical Data"
        val encrypted = CryptoManager.encryptBackup(originalText.toByteArray(), "CorrectPassword")
        CryptoManager.decryptBackup(encrypted, "WrongPassword")
    }
}
