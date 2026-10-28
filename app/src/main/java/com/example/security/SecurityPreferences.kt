package com.example.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "dawadiary_sec_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (t: Throwable) {
        context.getSharedPreferences("dawadiary_sec_prefs_fallback", Context.MODE_PRIVATE)
    }

    private val _isLocked = MutableStateFlow(isPinSet())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _flagSecureFlow = MutableStateFlow(prefs.getBoolean("pref_flag_secure", true))
    val flagSecureFlow: StateFlow<Boolean> = _flagSecureFlow.asStateFlow()

    private val _fontScaleFlow = MutableStateFlow(prefs.getFloat("pref_font_scale", 1.0f))
    val fontScaleFlow: StateFlow<Float> = _fontScaleFlow.asStateFlow()

    private val _highContrastFlow = MutableStateFlow(prefs.getBoolean("pref_high_contrast", false))
    val highContrastFlow: StateFlow<Boolean> = _highContrastFlow.asStateFlow()

    private val _activeProfileIdFlow = MutableStateFlow(prefs.getLong("pref_active_profile_id", -1L))
    val activeProfileIdFlow: StateFlow<Long> = _activeProfileIdFlow.asStateFlow()

    fun isPinSet(): Boolean {
        return prefs.contains("pref_pin_hash")
    }

    fun setPin(pin: String) {
        val hash = CryptoManager.hashPin(pin)
        prefs.edit().putString("pref_pin_hash", hash).apply()
        _isLocked.value = false
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString("pref_pin_hash", null) ?: return true
        val incomingHash = CryptoManager.hashPin(pin)
        val valid = storedHash == incomingHash
        if (valid) {
            _isLocked.value = false
        }
        return valid
    }

    fun clearPin() {
        prefs.edit().remove("pref_pin_hash").apply()
        _isLocked.value = false
    }

    fun lock() {
        if (isPinSet()) {
            _isLocked.value = true
        }
    }

    fun unlock() {
        _isLocked.value = false
    }

    fun setFlagSecureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_flag_secure", enabled).apply()
        _flagSecureFlow.value = enabled
    }

    fun setFontScale(scale: Float) {
        prefs.edit().putFloat("pref_font_scale", scale).apply()
        _fontScaleFlow.value = scale
    }

    fun setHighContrast(enabled: Boolean) {
        prefs.edit().putBoolean("pref_high_contrast", enabled).apply()
        _highContrastFlow.value = enabled
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("pref_language", lang).apply()
    }

    fun getLanguage(): String = prefs.getString("pref_language", "en") ?: "en"

    fun setActiveProfileId(id: Long) {
        prefs.edit().putLong("pref_active_profile_id", id).apply()
        _activeProfileIdFlow.value = id
    }

    fun getActiveProfileId(): Long = prefs.getLong("pref_active_profile_id", -1L)

    fun isConsentAccepted(): Boolean = prefs.getBoolean("pref_consent_accepted", false)

    fun setConsentAccepted(accepted: Boolean) {
        prefs.edit().putBoolean("pref_consent_accepted", accepted).apply()
    }

    fun isDisclaimerAccepted(): Boolean = prefs.getBoolean("pref_disclaimer_accepted", false)

    fun setDisclaimerAccepted(accepted: Boolean) {
        prefs.edit().putBoolean("pref_disclaimer_accepted", accepted).apply()
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
        _isLocked.value = false
        _flagSecureFlow.value = true
        _fontScaleFlow.value = 1.0f
        _highContrastFlow.value = false
        _activeProfileIdFlow.value = -1L
    }
}
