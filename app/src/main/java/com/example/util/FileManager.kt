package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.security.CryptoManager
import java.io.File
import java.io.FileOutputStream

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

    fun saveEncryptedFile(context: Context, uri: Uri, originalName: String): Pair<String, Long> {
        val recordsDir = getRecordsDir(context)
        val destFile = File(recordsDir, "${System.currentTimeMillis()}_$originalName.enc")
        val bytesWritten = context.contentResolver.openInputStream(uri)?.use { input ->
            CryptoManager.encryptFile(input, destFile)
        } ?: 0L
        return Pair(destFile.absolutePath, bytesWritten)
    }

    fun deleteEncryptedFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) file.delete() else true
    }

    fun wipeAllEncryptedFiles(context: Context) {
        val recordsDir = getRecordsDir(context)
        recordsDir.listFiles()?.forEach { it.delete() }
        val cacheDir = getCacheImagesDir(context)
        cacheDir.listFiles()?.forEach { it.delete() }
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
        val cacheDir = getCacheImagesDir(context)
        val destFile = File(cacheDir, "share_$fileName")
        return try {
            FileOutputStream(destFile).use { output ->
                CryptoManager.decryptFile(srcFile, output)
            }
            destFile
        } catch (e: Exception) {
            null
        }
    }
}
