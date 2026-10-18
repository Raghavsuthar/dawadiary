package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupManager {

    suspend fun createEncryptedBackup(context: Context, destUri: Uri, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath("dawadiary.db")
            if (!dbFile.exists()) return@withContext false

            val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
            val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }

            val keySpec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
            val keyBytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

            val dbBytes = FileInputStream(dbFile).use { it.readBytes() }
            val encryptedBytes = cipher.doFinal(dbBytes)

            context.contentResolver.openOutputStream(destUri)?.use { out ->
                out.write(salt)
                out.write(iv)
                out.write(encryptedBytes)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreEncryptedBackup(context: Context, sourceUri: Uri, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() } ?: return@withContext false
            if (bytes.size < 28) return@withContext false

            val salt = bytes.copyOfRange(0, 16)
            val iv = bytes.copyOfRange(16, 28)
            val encryptedBytes = bytes.copyOfRange(28, bytes.size)

            val keySpec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
            val keyBytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

            val decryptedBytes = cipher.doFinal(encryptedBytes)

            val dbFile = context.getDatabasePath("dawadiary.db")
            FileOutputStream(dbFile).use { it.write(decryptedBytes) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
