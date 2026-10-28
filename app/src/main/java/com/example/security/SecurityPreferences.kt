package com.example.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PinVerifyResult {
    object Success : PinVerifyResult()
    data class Incorrect(val attemptsRemaining: Int) : PinVerifyResult()
    data class LockedOut(val secondsRemaining: Long) : PinVerifyResult()
}

class SecurityPreferences(context: Context) {

    var isHardwareKeystoreAvailable: Boolean = true
        private set

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
        isHardwareKeystoreAvailable = false
        context.getSharedPreferences("dawadiary_sec_prefs_fallback", Context.MODE_PRIVATE)
    }

    private val _isLocked = MutableStateFlow(isPinSet())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _flagSecureFlow = MutableStateFlow(prefs.getBoolean("pref_flag_secure", false))
    val flagSecureFlow: StateFlow<Boolean> = _flagSecureFlow.asStateFlow()

    private val _fontScaleFlow = MutableStateFlow(prefs.getFloat("pref_font_scale", 1.0f))
    val fontScaleFlow: StateFlow<Float> = _fontScaleFlow.asStateFlow()

    private val _highContrastFlow = MutableStateFlow(prefs.getBoolean("pref_high_contrast", false))
    val highContrastFlow: StateFlow<Boolean> = _highContrastFlow.asStateFlow()

    private val _activeProfileIdFlow = MutableStateFlow(prefs.getLong("pref_active_profile_id", -1L))
    val activeProfileIdFlow: StateFlow<Long> = _activeProfileIdFlow.asStateFlow()

    private val _privateNotificationsFlow = MutableStateFlow(prefs.getBoolean("pref_private_notifications", true))
    val privateNotificationsFlow: StateFlow<Boolean> = _privateNotificationsFlow.asStateFlow()

    private var failedPinAttempts = 0
    private var lockoutUntilMillis = 0L

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 30_000L
    }

    fun isPinSet(): Boolean {
        return prefs.contains("pref_pin_hash")
    }

    fun setPin(pin: String) {
        require(pin.length in 4..6 && pin.all { it.isDigit() }) {
            "PIN must be between 4 and 6 digits"
        }
        val salt = CryptoManager.generatePinSalt()
        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hash = CryptoManager.hashPinWithSalt(pin, salt)

        prefs.edit()
            .putString("pref_pin_hash", hash)
            .putString("pref_pin_salt", saltHex)
            .apply()

        failedPinAttempts = 0
        lockoutUntilMillis = 0L
        _isLocked.value = false
    }

    fun verifyPin(pin: String): PinVerifyResult {
        val now = System.currentTimeMillis()
        if (now < lockoutUntilMillis) {
            val remainingSec = ((lockoutUntilMillis - now) / 1000) + 1
            return PinVerifyResult.LockedOut(remainingSec)
        }

        val storedHash = prefs.getString("pref_pin_hash", null)
            ?: return PinVerifyResult.Success

        val storedSaltHex = prefs.getString("pref_pin_salt", null)
        val valid = if (storedSaltHex != null) {
            val salt = storedSaltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val computedHash = CryptoManager.hashPinWithSalt(pin, salt)
            storedHash == computedHash
        } else {
            // Legacy single-round SHA-256 fallback; if valid, seamlessly migrate to PBKDF2
            val legacyHash = CryptoManager.hashPinLegacy(pin)
            val matches = (storedHash == legacyHash)
            if (matches) {
                setPin(pin) // Upgrade to salted PBKDF2 hash
            }
            matches
        }

        return if (valid) {
            failedPinAttempts = 0
            lockoutUntilMillis = 0L
            _isLocked.value = false
            PinVerifyResult.Success
        } else {
            failedPinAttempts++
            if (failedPinAttempts >= MAX_FAILED_ATTEMPTS) {
                lockoutUntilMillis = System.currentTimeMillis() + LOCKOUT_DURATION_MS
                failedPinAttempts = 0
                PinVerifyResult.LockedOut(LOCKOUT_DURATION_MS / 1000)
            } else {
                PinVerifyResult.Incorrect(MAX_FAILED_ATTEMPTS - failedPinAttempts)
            }
        }
    }

    fun clearPin() {
        prefs.edit()
            .remove("pref_pin_hash")
            .remove("pref_pin_salt")
            .apply()
        failedPinAttempts = 0
        lockoutUntilMillis = 0L
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

    fun setPrivateNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("pref_private_notifications", enabled).apply()
        _privateNotificationsFlow.value = enabled
    }

    fun isPrivateNotificationsEnabled(): Boolean {
        return prefs.getBoolean("pref_private_notifications", true)
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
        _flagSecureFlow.value = false
        _fontScaleFlow.value = 1.0f
        _highContrastFlow.value = false
        _activeProfileIdFlow.value = -1L
        _privateNotificationsFlow.value = true
        failedPinAttempts = 0
        lockoutUntilMillis = 0L
    }
}
