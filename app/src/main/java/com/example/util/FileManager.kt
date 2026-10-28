package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.security.CryptoManager
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileManager {

    fun getRecordsDir(context: Context): File {
        val dir = File(context.filesDir, "encrypted_records")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getCacheImagesDir(context: Context): File {
        val dir = File(context.cacheDir, "record_images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getShareableUri(context: Context, file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Safely saves an encrypted file into app-private storage.
     * Uses a sanitized UUID-based filename to prevent path traversal attacks.
     * Cleans up partial files on failure and rejects empty streams.
     */
    fun saveEncryptedFile(context: Context, uri: Uri, originalName: String): Pair<String, Long> {
        val recordsDir = getRecordsDir(context)
        val safeName = "rec_${UUID.randomUUID()}.enc"
        val destFile = File(recordsDir, safeName)

        val bytesWritten = try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Cannot open stream for URI: $uri")
            input.use { stream ->
                CryptoManager.encryptFile(stream, destFile)
            }
        } catch (t: Throwable) {
            if (destFile.exists()) destFile.delete()
            throw t
        }

        if (bytesWritten <= 0L) {
            if (destFile.exists()) destFile.delete()
            throw IllegalArgumentException("Zero bytes read from attachment URI")
        }

        return Pair(destFile.absolutePath, bytesWritten)
    }

    fun deleteEncryptedFile(path: String): Boolean {
        if (path.isBlank()) return false
        val file = File(path)
        return if (file.exists()) file.delete() else true
    }

    fun wipeAllEncryptedFiles(context: Context) {
        val recordsDir = getRecordsDir(context)
        recordsDir.listFiles()?.forEach { it.delete() }
        cleanCacheDir(context)
    }

    fun cleanCacheDir(context: Context) {
        val cacheDir = getCacheImagesDir(context)
        cacheDir.listFiles()?.forEach { file ->
            // Delete temp or shareable decrypted files older than 5 minutes
            if (System.currentTimeMillis() - file.lastModified() > 300_000L || file.name.startsWith("share_")) {
                file.delete()
            }
        }
    }

    fun loadDecryptedBitmap(filePath: String): Bitmap? {
        val file = File(filePath)
        if (!file.exists()) return null
        return try {
            val bytes = CryptoManager.decryptToByteArray(file)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    fun decryptToShareableFile(context: Context, filePath: String, fileName: String): File? {
        val srcFile = File(filePath)
        if (!srcFile.exists()) return null

        cleanCacheDir(context)
        val cacheDir = getCacheImagesDir(context)
        val sanitizedName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val destFile = File(cacheDir, "share_${UUID.randomUUID()}_$sanitizedName")
        return try {
            FileOutputStream(destFile).use { output ->
                CryptoManager.decryptFile(srcFile, output)
            }
            destFile
        } catch (e: Exception) {
            if (destFile.exists()) destFile.delete()
            null
        }
    }
}
