package com.example.myapplication.data.datasource.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserData(
    val id: Int,
    val provider: String,
    val providerId: String,   // ⭐ 新增
    val name: String,
    val nickname: String,
    val avatarUrl: String
)

class UserPreferences(private val context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_ID_INT = "user_id_int"
        const val KEY_PROVIDER = "provider"
        const val KEY_PROVIDER_ID = "provider_id"   // ⭐ 新增
        const val KEY_NAME = "name"
        const val KEY_AVATAR = "avatar_url"
    }

    /** 儲存登入後的使用者資料 */
    suspend fun saveUser(
        id: Int,
        provider: String,
        providerId: String,        // ⭐ 新增
        name: String,
        avatarUrl: String
    ) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putInt(KEY_ID_INT, id)
            .putString(KEY_PROVIDER, provider)
            .putString(KEY_PROVIDER_ID, providerId)   // ⭐ 新增
            .putString(KEY_NAME, name)
            .putString(KEY_AVATAR, avatarUrl)
            .apply()
    }

    /** 取得使用者資料（非同步） */
    suspend fun getUser(): UserData? = withContext(Dispatchers.IO) { getUserSync() }

    /** 取得使用者資料（同步） */
    fun getUserSync(): UserData? {
        val id = prefs.getInt(KEY_ID_INT, Int.MIN_VALUE)
        if (id == Int.MIN_VALUE) return null

        val provider = prefs.getString(KEY_PROVIDER, null) ?: return null
        val providerId = prefs.getString(KEY_PROVIDER_ID, "") ?: ""   // ⭐ 新增
        val name = prefs.getString(KEY_NAME, "") ?: ""
        val avatar = prefs.getString(KEY_AVATAR, "") ?: ""

        return UserData(
            id = id,
            provider = provider,
            providerId = providerId,
            name = name,
            nickname = "",
            avatarUrl = avatar
        )
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }
}
