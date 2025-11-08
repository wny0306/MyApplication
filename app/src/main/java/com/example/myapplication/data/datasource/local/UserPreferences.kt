package com.example.myapplication.data.datasource.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserData(
    val id: String,
    val provider: String,
    val name: String,
    val nickname: String,
    val avatarUrl: String
)

class UserPreferences(private val context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    /**
     * ✅ 儲存使用者資料（登入後 or 更新後）
     */
    suspend fun saveUser(
        id: String,
        provider: String,
        name: String,
        nickname: String,
        avatarUrl: String
    ) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString("user_id", id)
                .putString("provider", provider)
                .putString("name", name)
                .putString("nickname", nickname)
                .putString("avatar_url", avatarUrl)
                .apply()
        }
    }

    /**
     * ✅ 取得使用者資料（非同步）
     */
    suspend fun getUser(): UserData? = withContext(Dispatchers.IO) {
        val id = prefs.getString("user_id", null)
        val provider = prefs.getString("provider", null)
        val name = prefs.getString("name", null)
        val nickname = prefs.getString("nickname", null)
        val avatar = prefs.getString("avatar_url", null)
        return@withContext if (id != null && provider != null) {
            UserData(
                id = id,
                provider = provider,
                name = name ?: "",
                nickname = nickname ?: "",
                avatarUrl = avatar ?: ""
            )
        } else null
    }

    /**
     * ✅ 清除所有登入資料（登出時）
     */
    suspend fun clear() {
        withContext(Dispatchers.IO) {
            prefs.edit().clear().apply()
        }
    }

    /**
     * ✅ 同步版本（非 suspend）
     */
    fun getUserSync(): UserData? {
        val id = prefs.getString("user_id", null)
        val provider = prefs.getString("provider", null)
        val name = prefs.getString("name", null)
        val nickname = prefs.getString("nickname", null)
        val avatar = prefs.getString("avatar_url", null)
        return if (id != null && provider != null) {
            UserData(
                id = id,
                provider = provider,
                name = name ?: "",
                nickname = nickname ?: "",
                avatarUrl = avatar ?: ""
            )
        } else null
    }
}
