package com.example.myapplication.feature.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.datasource.AuthRepositoryImpl
import com.example.myapplication.data.datasource.local.UserPreferences
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

private const val PREFS = "profile_prefs"
private const val KEY_AVATAR = "avatar_uri"
private const val KEY_INTRO = "intro"

private const val GET_PROFILE_URL =
    "http://59.127.30.235:85/api/get_profile.php"

private const val UPDATE_PROFILE_URL =
    "http://59.127.30.235:85/api/update_profile.php"

class ProfileViewModel(
    private val auth: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _username = MutableStateFlow("訪客")
    val username: StateFlow<String> = _username

    private val _nickname = MutableStateFlow("暱稱")
    val nickname: StateFlow<String> = _nickname

    private val _avatarUri = MutableStateFlow<Uri?>(null)
    val avatarUri: StateFlow<Uri?> = _avatarUri

    private val _intro = MutableStateFlow("")
    val intro: StateFlow<String> = _intro

    /** ⭐ 載入個人資料 */
    fun load(ctx: Context) {
        _username.value = auth.currentUser(ctx) ?: "訪客"
        viewModelScope.launch { loadProfileFromServer(ctx) }
    }

    /**
     * ⭐ 從後端使用 user_id 撈使用者資料
     * 不再使用 provider + provider_id
     */
    private suspend fun loadProfileFromServer(ctx: Context) {
        withContext(Dispatchers.IO) {
            try {
                val userPrefs = UserPreferences(ctx)
                val user = userPrefs.getUser() ?: return@withContext

                val url = URL("$GET_PROFILE_URL?user_id=${user.id}")   // ⭐ 只用 user_id

                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val responseText =
                    conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                Log.d("Profile", "get_profile response: $responseText")

                val json = JSONObject(responseText)
                if (!json.optBoolean("success", false)) {
                    Log.e("Profile", "後端回傳錯誤: ${json.optString("message")}")
                    return@withContext
                }

                val data = json.optJSONObject("data") ?: return@withContext

                val nicknameFromServer = data.optString("nickname", "暱稱")
                val avatarUrlFromServer = data.optString("avatar_url", "")
                val introFromServer = data.optString("intro", "")

                withContext(Dispatchers.Main) {
                    _nickname.value = nicknameFromServer
                    _intro.value = introFromServer
                    _avatarUri.value =
                        if (avatarUrlFromServer.isNotBlank()) Uri.parse(avatarUrlFromServer)
                        else null
                }

            } catch (e: Exception) {
                Log.e("Profile", "loadProfileFromServer error", e)
            }
        }
    }

    /** 本地更新暱稱（更新後端成功後呼叫） */
    fun updateNicknameInMemory(newNickname: String) {
        _nickname.value = newNickname
    }

    /** 儲存相簿頭貼 */
    fun saveAvatarUri(ctx: Context, uri: Uri?) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (uri != null)
            prefs.edit().putString(KEY_AVATAR, uri.toString()).apply()
        else
            prefs.edit().remove(KEY_AVATAR).apply()

        _avatarUri.value = uri
    }

    /** 儲存拍照頭貼 */
    fun saveAvatarBitmap(ctx: Context, bitmap: Bitmap) {
        try {
            val file = File(ctx.filesDir, "avatar_${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            val uri =
                FileProvider.getUriForFile(ctx, ctx.packageName + ".provider", file)
            saveAvatarUri(ctx, uri)
        } catch (e: Exception) {
            Log.e("Profile", "儲存頭貼失敗", e)
        }
    }

    /**
     * ⭐ 儲存自我介紹到後端
     * 注意：update_profile.php 是用 user_id 更新（不是 provider_id）
     */
    suspend fun saveIntro(ctx: Context, intro: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val userPrefs = UserPreferences(ctx)
                val user = userPrefs.getUser() ?: return@withContext false

                val params = listOf(
                    "user_id" to user.id.toString(),
                    "nickname" to _nickname.value,
                    "avatar_url" to (_avatarUri.value?.toString() ?: ""),
                    "intro" to intro
                )

                val postData = params.joinToString("&") { (k, v) ->
                    k + "=" + URLEncoder.encode(v, "UTF-8")
                }

                val url = URL(UPDATE_PROFILE_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 8000
                    doOutput = true
                    setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                    )
                }

                BufferedWriter(OutputStreamWriter(conn.outputStream, "UTF-8")).use { writer ->
                    writer.write(postData)
                    writer.flush()
                }

                val code = conn.responseCode
                val responseText = if (code in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                }
                conn.disconnect()

                Log.d("Profile", "update_profile response($code): $responseText")

                val json = JSONObject(responseText)
                val success = json.optBoolean("success", false)

                if (success) {
                    _intro.value = intro
                }

                success

            } catch (e: Exception) {
                Log.e("Profile", "saveIntro error", e)
                false
            }
        }
    }

    /** 登出 */
    fun logout(ctx: Context) = auth.logout(ctx)
}
