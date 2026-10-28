package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.AppDatabase
import com.example.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupManager {

    suspend fun createEncryptedBackup(context: Context, password: String, destinationUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context)
                val rootJson = JSONObject()

                // Profiles
                val profiles = db.profileDao().getAllProfiles()
                val profilesArr = JSONArray()
                for (p in profiles) {
                    val pObj = JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("relationship", p.relationship)
                        put("dob", p.dob)
                        put("gender", p.gender)
                        put("bloodGroup", p.bloodGroup)
                        put("allergies", p.allergies)
                        put("chronicConditions", p.chronicConditions)
                        put("emergencyContactName", p.emergencyContactName)
                        put("emergencyContactPhone", p.emergencyContactPhone)
                        put("isSelf", p.isSelf)
                    }
                    profilesArr.put(pObj)
                }
                rootJson.put("profiles", profilesArr)

                val plainBytes = rootJson.toString().toByteArray(Charsets.UTF_8)
                val encryptedBytes = CryptoManager.encryptBackup(plainBytes, password)

                context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    out.write(encryptedBytes)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun restoreEncryptedBackup(context: Context, password: String, sourceUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val encBytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
                    ?: return@withContext false

                val decryptedBytes = CryptoManager.decryptBackup(encBytes, password)
                val jsonStr = String(decryptedBytes, Charsets.UTF_8)
                val rootJson = JSONObject(jsonStr)

                // Validate backup format
                if (!rootJson.has("profiles")) return@withContext false

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
