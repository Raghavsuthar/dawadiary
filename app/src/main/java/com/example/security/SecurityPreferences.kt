package com.example.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dawadiary_sec_prefs", Context.MODE_PRIVATE)

    private val _isLocked = MutableStateFlow(isPinOrBiometricEnabled())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _flagSecureFlow = MutableStateFlow(isFlagSecureEnabled())
    val flagSecureFlow: StateFlow<Boolean> = _flagSecureFlow.asStateFlow()

    private val _activeProfileIdFlow = MutableStateFlow(getActiveProfileId())
    val activeProfileIdFlow: StateFlow<Long> = _activeProfileIdFlow.asStateFlow()

    private val _fontScaleFlow = MutableStateFlow(getFontScale())
    val fontScaleFlow: StateFlow<Float> = _fontScaleFlow.asStateFlow()

    private val _highContrastFlow = MutableStateFlow(isHighContrast())
    val highContrastFlow: StateFlow<Boolean> = _highContrastFlow.asStateFlow()

    fun isConsentAccepted(): Boolean = prefs.getBoolean(KEY_CONSENT_ACCEPTED, false)
    fun setConsentAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_CONSENT_ACCEPTED, accepted).apply()
    }

    fun isDisclaimerAccepted(): Boolean = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
    fun setDisclaimerAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted).apply()
    }

    fun isPinSet(): Boolean = prefs.getString(KEY_PIN_HASH, null) != null

    fun setPin(pin: String) {
        val hash = CryptoManager.hashPin(pin)
        prefs.edit().putString(KEY_PIN_HASH, hash).apply()
        _isLocked.value = false
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val inputHash = CryptoManager.hashPin(pin)
        val valid = storedHash == inputHash
        if (valid) {
            _isLocked.value = false
        }
        return valid
    }

    fun clearPin() {
        prefs.edit().remove(KEY_PIN_HASH).putBoolean(KEY_BIOMETRIC_ENABLED, false).apply()
        _isLocked.value = false
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isPinOrBiometricEnabled(): Boolean = isPinSet() || isBiometricEnabled()

    fun unlock() {
        _isLocked.value = false
    }

    fun lock() {
        if (isPinOrBiometricEnabled()) {
            _isLocked.value = true
        }
    }

    fun isFlagSecureEnabled(): Boolean = prefs.getBoolean(KEY_FLAG_SECURE, false)
    fun setFlagSecureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLAG_SECURE, enabled).apply()
        _flagSecureFlow.value = enabled
    }

    fun getActiveProfileId(): Long = prefs.getLong(KEY_ACTIVE_PROFILE_ID, -1L)
    fun setActiveProfileId(id: Long) {
        prefs.edit().putLong(KEY_ACTIVE_PROFILE_ID, id).apply()
        _activeProfileIdFlow.value = id
    }

    fun getFontScale(): Float = prefs.getFloat(KEY_FONT_SCALE, 1.0f)
    fun setFontScale(scale: Float) {
        prefs.edit().putFloat(KEY_FONT_SCALE, scale).apply()
        _fontScaleFlow.value = scale
    }

    fun isHighContrast(): Boolean = prefs.getBoolean(KEY_HIGH_CONTRAST, false)
    fun setHighContrast(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
        _highContrastFlow.value = enabled
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
        _isLocked.value = false
        _activeProfileIdFlow.value = -1L
        _flagSecureFlow.value = false
    }

    companion object {
        private const val KEY_CONSENT_ACCEPTED = "consent_accepted"
        private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_FLAG_SECURE = "flag_secure"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_LANGUAGE = "selected_language"
    }
}
