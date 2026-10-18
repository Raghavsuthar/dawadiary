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

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    private fun getOrCreateMasterKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
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
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    fun encryptFile(inputStream: InputStream, destinationFile: File): Long {
        destinationFile.parentFile?.mkdirs()
        val secretKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv

        FileOutputStream(destinationFile).use { fos ->
            fos.write(iv)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                val output = cipher.update(buffer, 0, bytesRead)
                if (output != null) {
                    fos.write(output)
                }
            }
            val finalBytes = cipher.doFinal()
            if (finalBytes != null) {
                fos.write(finalBytes)
            }
        }
        return destinationFile.length()
    }

    fun decryptFile(encryptedFile: File, outputStream: OutputStream) {
        if (!encryptedFile.exists()) return
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

    fun encryptBackup(data: ByteArray, password: String): ByteArray {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }

        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(data)

        val result = ByteArray(salt.size + iv.size + ciphertext.size)
        System.arraycopy(salt, 0, result, 0, salt.size)
        System.arraycopy(iv, 0, result, salt.size, iv.size)
        System.arraycopy(ciphertext, 0, result, salt.size + iv.size, ciphertext.size)
        return result
    }

    fun decryptBackup(encryptedData: ByteArray, password: String): ByteArray {
        if (encryptedData.size < 16 + GCM_IV_LENGTH) {
            throw IllegalArgumentException("Corrupted backup file: insufficient data")
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

    fun hashPin(pin: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
