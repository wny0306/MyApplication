package com.example.myapplication.feature.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.datasource.AuthRepositoryImpl
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.*

private const val PREFS = "profile_prefs"
private const val KEY_NICK = "nickname"
private const val KEY_AVATAR = "avatar_uri"

class ProfileViewModel(
    private val auth: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _username = MutableStateFlow("訪客")
    val username: StateFlow<String> = _username

    private val _nickname = MutableStateFlow("暱稱")
    val nickname: StateFlow<String> = _nickname

    private val _avatarUri = MutableStateFlow<Uri?>(null)
    val avatarUri: StateFlow<Uri?> = _avatarUri

    // 🧠 載入使用者資料（帳號、暱稱、頭貼）
    fun load(ctx: Context) {
        _username.value = auth.currentUser(ctx) ?: "訪客"

        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _nickname.value = prefs.getString(KEY_NICK, "暱稱") ?: "暱稱"

        val avatarStr = prefs.getString(KEY_AVATAR, null)
        _avatarUri.value = if (avatarStr != null) Uri.parse(avatarStr) else null
    }

    // 💾 儲存暱稱
    fun saveNickname(ctx: Context, nickname: String) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NICK, nickname).apply()
        _nickname.value = nickname
    }

    // 💾 儲存相簿選擇的頭貼 URI
    fun saveAvatarUri(ctx: Context, uri: Uri?) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (uri != null) {
            prefs.edit().putString(KEY_AVATAR, uri.toString()).apply()
        } else {
            prefs.edit().remove(KEY_AVATAR).apply()
        }
        _avatarUri.value = uri
    }

    // 📸 儲存拍照 Bitmap
    fun saveAvatarBitmap(ctx: Context, bitmap: Bitmap) {
        try {
            val file = File(ctx.filesDir, "avatar_${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".provider", file)
            saveAvatarUri(ctx, uri)
            Log.d("Profile", "已儲存拍照頭貼至：$uri")
        } catch (e: Exception) {
            Log.e("Profile", "儲存頭貼失敗", e)
        }
    }

    // 🚪 登出
    fun logout(ctx: Context) = auth.logout(ctx)
}
