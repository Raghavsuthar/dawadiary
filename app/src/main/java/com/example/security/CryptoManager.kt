package com.example.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "DawaDiaryMasterKey"
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // Backup Constants
    private val BACKUP_MAGIC = "DAWADIARY".toByteArray(Charsets.US_ASCII)
    private const val BACKUP_VERSION_V2: Byte = 0x02
    private const val BACKUP_SALT_LENGTH = 16
    private const val BACKUP_PBKDF2_ITERATIONS = 65536
    private const val BACKUP_KEY_LENGTH = 256

    // PIN Hashing Constants
    private const val PIN_SALT_LENGTH = 16
    private const val PIN_PBKDF2_ITERATIONS = 25000
    private const val PIN_KEY_LENGTH = 128

    private var inMemoryMasterKey: SecretKey? = null

    private fun getOrCreateMasterKey(): SecretKey {
        return try {
            val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!ks.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(ALGORITHM, ANDROID_KEYSTORE)
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
            val entry = ks.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            entry.secretKey
        } catch (e: Throwable) {
            // Software fallback for tests/environments where AndroidKeyStore is unavailable
            inMemoryMasterKey ?: run {
                val rawKey = ByteArray(32).apply { SecureRandom().nextBytes(this) }
                val fallbackKey = SecretKeySpec(rawKey, "AES")
                inMemoryMasterKey = fallbackKey
                fallbackKey
            }
        }
    }

    fun encryptFile(inputStream: InputStream, destinationFile: File): Long {
        destinationFile.parentFile?.mkdirs()
        val tempFile = File(destinationFile.parentFile, "${destinationFile.name}.tmp")
        try {
            val secretKey = getOrCreateMasterKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv

            FileOutputStream(tempFile).use { fos ->
                fos.write(iv)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalPayload = 0L
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val output = cipher.update(buffer, 0, bytesRead)
                    if (output != null) {
                        fos.write(output)
                    }
                    totalPayload += bytesRead
                }
                if (totalPayload == 0L) {
                    throw IllegalArgumentException("Cannot encrypt empty stream")
                }
                val finalBytes = cipher.doFinal()
                if (finalBytes != null) {
                    fos.write(finalBytes)
                }
            }

            if (destinationFile.exists()) destinationFile.delete()
            if (!tempFile.renameTo(destinationFile)) {
                tempFile.copyTo(destinationFile, overwrite = true)
                tempFile.delete()
            }
            return destinationFile.length()
        } catch (t: Throwable) {
            if (tempFile.exists()) tempFile.delete()
            throw t
        }
    }

    fun decryptFile(encryptedFile: File, outputStream: OutputStream) {
        if (!encryptedFile.exists() || encryptedFile.length() < GCM_IV_LENGTH) {
            throw IllegalArgumentException("Invalid or missing encrypted file: ${encryptedFile.path}")
        }
        val secretKey = getOrCreateMasterKey()
        FileInputStream(encryptedFile).use { fis ->
            val iv = ByteArray(GCM_IV_LENGTH)
            val readIv = fis.read(iv)
            if (readIv != GCM_IV_LENGTH) {
                throw IllegalStateException("Invalid encrypted file header")
            }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                val output = cipher.update(buffer, 0, bytesRead)
                if (output != null) {
                    outputStream.write(output)
                }
            }
            val finalBytes = cipher.doFinal()
            if (finalBytes != null) {
                outputStream.write(finalBytes)
            }
        }
    }

    fun decryptToByteArray(encryptedFile: File): ByteArray {
        val baos = java.io.ByteArrayOutputStream()
        decryptFile(encryptedFile, baos)
        return baos.toByteArray()
    }

    /**
     * Authenticated, versioned backup encryption with PBKDF2 (65,536 rounds) + AES-256-GCM.
     * Format: [MAGIC (9B)] [VERSION (1B)] [SALT (16B)] [IV (12B)] [CIPHERTEXT + GCM_TAG]
     */
    fun encryptBackup(data: ByteArray, password: String): ByteArray {
        require(password.isNotBlank()) { "Backup password cannot be empty" }
        require(data.isNotEmpty()) { "Backup data cannot be empty" }

        val salt = ByteArray(BACKUP_SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }

        val spec = PBEKeySpec(password.toCharArray(), salt, BACKUP_PBKDF2_ITERATIONS, BACKUP_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(data)

        val headerSize = BACKUP_MAGIC.size + 1 + salt.size + iv.size
        val result = ByteArray(headerSize + ciphertext.size)

        var offset = 0
        System.arraycopy(BACKUP_MAGIC, 0, result, offset, BACKUP_MAGIC.size)
        offset += BACKUP_MAGIC.size
        result[offset] = BACKUP_VERSION_V2
        offset += 1
        System.arraycopy(salt, 0, result, offset, salt.size)
        offset += salt.size
        System.arraycopy(iv, 0, result, offset, iv.size)
        offset += iv.size
        System.arraycopy(ciphertext, 0, result, offset, ciphertext.size)

        return result
    }

    /**
     * Decrypts authenticated backup, strictly validating header, version, and GCM authentication tag.
     */
    fun decryptBackup(encryptedData: ByteArray, password: String): ByteArray {
        require(password.isNotBlank()) { "Password required for backup restore" }

        // Check if data starts with V2 Magic
        if (encryptedData.size >= BACKUP_MAGIC.size + 1 + BACKUP_SALT_LENGTH + GCM_IV_LENGTH &&
            encryptedData.copyOfRange(0, BACKUP_MAGIC.size).contentEquals(BACKUP_MAGIC)
        ) {
            val version = encryptedData[BACKUP_MAGIC.size]
            if (version != BACKUP_VERSION_V2) {
                throw IllegalArgumentException("Unsupported backup format version: $version")
            }

            var offset = BACKUP_MAGIC.size + 1
            val salt = ByteArray(BACKUP_SALT_LENGTH)
            System.arraycopy(encryptedData, offset, salt, 0, BACKUP_SALT_LENGTH)
            offset += BACKUP_SALT_LENGTH

            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(encryptedData, offset, iv, 0, GCM_IV_LENGTH)
            offset += GCM_IV_LENGTH

            val ciphertextSize = encryptedData.size - offset
            if (ciphertextSize <= 16) { // Tag alone is 16 bytes
                throw IllegalArgumentException("Corrupted backup file: truncated ciphertext")
            }
            val ciphertext = ByteArray(ciphertextSize)
            System.arraycopy(encryptedData, offset, ciphertext, 0, ciphertextSize)

            val spec = PBEKeySpec(password.toCharArray(), salt, BACKUP_PBKDF2_ITERATIONS, BACKUP_KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = factory.generateSecret(spec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            return cipher.doFinal(ciphertext)
        }

        // Legacy V1 format fallback (16B salt + 12B IV + ciphertext, 10000 rounds)
        val minLegacySize = 16 + GCM_IV_LENGTH + 16
        if (encryptedData.size < minLegacySize) {
            throw IllegalArgumentException("Invalid backup file: header missing or corrupted")
        }

        val salt = ByteArray(16)
        val iv = ByteArray(GCM_IV_LENGTH)
        System.arraycopy(encryptedData, 0, salt, 0, 16)
        System.arraycopy(encryptedData, 16, iv, 0, GCM_IV_LENGTH)

        val ciphertextOffset = 16 + GCM_IV_LENGTH
        val ciphertextSize = encryptedData.size - ciphertextOffset
        val ciphertext = ByteArray(ciphertextSize)
        System.arraycopy(encryptedData, ciphertextOffset, ciphertext, 0, ciphertextSize)

        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }

    fun generatePinSalt(): ByteArray {
        return ByteArray(PIN_SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
    }

    /**
     * Derives a cryptographic hash of the PIN using PBKDF2 with salt.
     */
    fun hashPinWithSalt(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PIN_PBKDF2_ITERATIONS, PIN_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Legacy hash function kept for smooth verification of existing installations.
     */
    fun hashPinLegacy(pin: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
