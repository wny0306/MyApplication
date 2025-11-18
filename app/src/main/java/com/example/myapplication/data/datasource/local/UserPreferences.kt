package com.example.myapplication.data.datasource.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserData(
    val id: Int,
    val provider: String,
    val name: String,
    val nickname: String,
    val avatarUrl: String
)

class UserPreferences(private val context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_ID_INT = "user_id_int"
        const val KEY_ID_LEGACY = "user_id"
        const val KEY_PROVIDER = "provider"
        const val KEY_NAME = "name"
        const val KEY_NICKNAME = "nickname"
        const val KEY_AVATAR = "avatar_url"
    }

    /**
     * ✅ 儲存使用者資料（登入後 or 更新後）
     * 以 Int 寫入（新格式）
     */
    suspend fun saveUser(
        id: Int,
        provider: String,
        name: String,
        nickname: String,   // 參數先保留，不用它
        avatarUrl: String
    ) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putInt(KEY_ID_INT, id)
            .remove(KEY_ID_LEGACY)
            .putString(KEY_PROVIDER, provider)
            .putString(KEY_NAME, name)
            // .putString(KEY_NICKNAME, nickname) // ❌ 不再存暱稱
            .putString(KEY_AVATAR, avatarUrl)
            .apply()
    }


    /**
     * ✅ 取得使用者資料（非同步）
     * 先讀 Int；若沒有再嘗試讀舊的 String 並轉為 Int
     */
    suspend fun getUser(): UserData? = withContext(Dispatchers.IO) { getUserSync() }

    /**
     * ✅ 同步版本（非 suspend）
     * 具備舊資料相容：若只存在 KEY_ID_LEGACY，嘗試 toInt() 成功就回寫到新 Key
     */
    fun getUserSync(): UserData? {
        // 先讀新的 Int 格式
        val idFromInt = prefs.getInt(KEY_ID_INT, Int.MIN_VALUE)
        val id: Int = if (idFromInt != Int.MIN_VALUE) {
            idFromInt
        } else {
            // 相容：讀舊字串格式
            val legacy = prefs.getString(KEY_ID_LEGACY, null)?.toIntOrNull()
            if (legacy != null) {
                prefs.edit()
                    .putInt(KEY_ID_INT, legacy)
                    .remove(KEY_ID_LEGACY)
                    .apply()
            }
            legacy ?: return null
        }

        val provider = prefs.getString(KEY_PROVIDER, null) ?: return null
        val name = prefs.getString(KEY_NAME, "") ?: ""
        //val nickname = prefs.getString(KEY_NICKNAME, "") ?: ""
        val avatar = prefs.getString(KEY_AVATAR, "") ?: ""
        return UserData(
            id = id,
            provider = provider,
            name = name,
            nickname = "",
            avatarUrl = avatar
        )
    }

    /**
     * ✅ 清除所有登入資料（登出時）
     */
    suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }
}
