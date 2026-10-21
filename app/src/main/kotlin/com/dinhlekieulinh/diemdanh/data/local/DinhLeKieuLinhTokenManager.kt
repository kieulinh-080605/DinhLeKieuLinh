package com.dinhlekieulinh.diemdanh.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhRole
import com.dinhlekieulinh.diemdanh.data.model.DinhLeKieuLinhUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dinhLeKieuLinhDataStore by preferencesDataStore(name = "dinhlekieulinh_auth_prefs")

class DinhLeKieuLinhTokenManager(private val context: Context) {

    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = intPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_HOTEN = stringPreferencesKey("user_hoten")
        private val KEY_USER_MASO = stringPreferencesKey("user_maso")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_BIOMETRIC_TOKEN = stringPreferencesKey("biometric_token")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    // Lấy Token Flow
    val tokenFlow: Flow<String?> = context.dinhLeKieuLinhDataStore.data.map { prefs ->
        prefs[KEY_AUTH_TOKEN]
    }

    // Lấy Role Flow
    val roleFlow: Flow<DinhLeKieuLinhRole?> = context.dinhLeKieuLinhDataStore.data.map { prefs ->
        prefs[KEY_USER_ROLE]?.let {
            try {
                DinhLeKieuLinhRole.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Lấy thông tin user hiện tại Flow
    val currentUserFlow: Flow<DinhLeKieuLinhUser?> = context.dinhLeKieuLinhDataStore.data.map { prefs ->
        val id = prefs[KEY_USER_ID] ?: return@map null
        val roleStr = prefs[KEY_USER_ROLE] ?: DinhLeKieuLinhRole.SINH_VIEN.name
        val role = try { DinhLeKieuLinhRole.valueOf(roleStr) } catch (e: Exception) { DinhLeKieuLinhRole.SINH_VIEN }
        DinhLeKieuLinhUser(
            id = id,
            username = prefs[KEY_USERNAME] ?: "",
            email = prefs[KEY_USER_EMAIL] ?: "",
            hoTen = prefs[KEY_USER_HOTEN] ?: "",
            maSo = prefs[KEY_USER_MASO] ?: "",
            role = role
        )
    }

    // Lấy token đồng bộ (cho OkHttp Interceptor)
    suspend fun getToken(): String? {
        return context.dinhLeKieuLinhDataStore.data.map { it[KEY_AUTH_TOKEN] }.first()
    }

    // Lưu phiên đăng nhập
    suspend fun saveAuthSession(token: String, user: DinhLeKieuLinhUser) {
        context.dinhLeKieuLinhDataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USERNAME] = user.username
            prefs[KEY_USER_EMAIL] = user.email
            prefs[KEY_USER_HOTEN] = user.hoTen
            prefs[KEY_USER_MASO] = user.maSo
            prefs[KEY_USER_ROLE] = user.role.name
        }
    }

    // Bật/lưu token sinh trắc học
    suspend fun setBiometricToken(token: String, enabled: Boolean) {
        context.dinhLeKieuLinhDataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_TOKEN] = token
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun getBiometricToken(): String? {
        return context.dinhLeKieuLinhDataStore.data.map { it[KEY_BIOMETRIC_TOKEN] }.first()
    }

    suspend fun isBiometricEnabled(): Boolean {
        return context.dinhLeKieuLinhDataStore.data.map { it[KEY_BIOMETRIC_ENABLED] ?: false }.first()
    }

    // Đăng xuất và xóa session
    suspend fun clearSession() {
        context.dinhLeKieuLinhDataStore.edit { prefs ->
            prefs.remove(KEY_AUTH_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_HOTEN)
            prefs.remove(KEY_USER_MASO)
            prefs.remove(KEY_USER_ROLE)
        }
    }
}
