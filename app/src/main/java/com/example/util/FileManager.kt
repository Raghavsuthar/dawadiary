package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.security.CryptoManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object FileManager {

    fun getRecordsDir(context: Context): File {
        val dir = File(context.filesDir, "records")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getExportsDir(context: Context): File {
        val dir = File(context.filesDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getCacheImagesDir(context: Context): File {
        val dir = File(context.cacheDir, "images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveEncryptedFile(context: Context, sourceUri: Uri, originalFileName: String): Pair<String, Long> {
        val uniqueName = "${UUID.randomUUID()}_$originalFileName.enc"
        val destinationFile = File(getRecordsDir(context), uniqueName)

        val inputStream: InputStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw IllegalArgumentException("Cannot open stream for Uri: $sourceUri")

        inputStream.use { stream ->
            CryptoManager.encryptFile(stream, destinationFile)
        }

        return Pair(destinationFile.absolutePath, destinationFile.length())
    }

    fun saveEncryptedBitmap(context: Context, bitmap: Bitmap, namePrefix: String): Pair<String, Long> {
        val uniqueName = "${UUID.randomUUID()}_${namePrefix}.jpg.enc"
        val destinationFile = File(getRecordsDir(context), uniqueName)

        val tempFile = File(context.cacheDir, "temp_cam_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }

        tempFile.inputStream().use { stream ->
            CryptoManager.encryptFile(stream, destinationFile)
        }
        tempFile.delete()

        return Pair(destinationFile.absolutePath, destinationFile.length())
    }

    fun decryptToShareableFile(context: Context, encryptedFilePath: String, targetFileName: String): File? {
        val encryptedFile = File(encryptedFilePath)
        if (!encryptedFile.exists()) return null

        val safeName = if (targetFileName.endsWith(".enc")) targetFileName.removeSuffix(".enc") else targetFileName
        val tempFile = File(getCacheImagesDir(context), "view_${System.currentTimeMillis()}_$safeName")

        FileOutputStream(tempFile).use { fos ->
            CryptoManager.decryptFile(encryptedFile, fos)
        }
        return tempFile
    }

    fun loadDecryptedBitmap(encryptedFilePath: String): Bitmap? {
        val file = File(encryptedFilePath)
        if (!file.exists()) return null
        return try {
            val bytes = CryptoManager.decryptToByteArray(file)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e("FileManager", "Failed decrypting bitmap preview", e)
            null
        }
    }

    fun deleteEncryptedFile(encryptedFilePath: String): Boolean {
        return try {
            val file = File(encryptedFilePath)
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            Log.e("FileManager", "Failed deleting file $encryptedFilePath", e)
            false
        }
    }

    fun wipeAllEncryptedFiles(context: Context) {
        try {
            getRecordsDir(context).deleteRecursively()
            getExportsDir(context).deleteRecursively()
            getCacheImagesDir(context).deleteRecursively()
        } catch (e: Exception) {
            Log.e("FileManager", "Error wiping storage", e)
        }
    }

    fun getShareableUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
