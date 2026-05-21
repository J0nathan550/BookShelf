package com.j0nathan550.bookshelf.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "bookshelf_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun clearToken() = prefs.edit().remove(KEY_TOKEN).apply()

    fun saveUserId(id: String) = prefs.edit().putString(KEY_USER_ID, id).apply()
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun saveUserEmail(email: String) = prefs.edit().putString(KEY_EMAIL, email).apply()
    fun getUserEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun saveFullName(name: String) = prefs.edit().putString(KEY_FULL_NAME, name).apply()
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)

    fun saveRoles(roles: List<String>) =
        prefs.edit().putString(KEY_ROLES, roles.joinToString(",")).apply()

    fun getRoles(): Set<String> =
        prefs.getString(KEY_ROLES, "")?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()

    fun isAdmin(): Boolean = getRoles().contains("Admin")

    fun clearAll() {
        val biometricEnabled = isBiometricEnabled()
        prefs.edit()
            .clear()
            .putBoolean(KEY_BIOMETRIC_ENABLED, biometricEnabled)
            .apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    fun setBiometricEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()

    fun saveFcmToken(token: String) = prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    fun getFcmToken(): String? = prefs.getString(KEY_FCM_TOKEN, null)
    fun clearFcmToken() = prefs.edit().remove(KEY_FCM_TOKEN).apply()

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ROLES = "roles"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
}
