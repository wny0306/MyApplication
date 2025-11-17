package com.example.myapplication.feature.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.datasource.AuthRepositoryImpl
import com.example.myapplication.data.datasource.local.UserPreferences
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
private const val KEY_NICK = "nickname"
private const val KEY_AVATAR = "avatar_uri"
private const val KEY_INTRO = "intro"

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

    // 自我介紹
    private val _intro = MutableStateFlow("")
    val intro: StateFlow<String> = _intro

    // 🧠 載入使用者資料（帳號、暱稱、頭貼、自我介紹）
    fun load(ctx: Context) {
        _username.value = auth.currentUser(ctx) ?: "訪客"

        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _nickname.value = prefs.getString(KEY_NICK, "暱稱") ?: "暱稱"

        val avatarStr = prefs.getString(KEY_AVATAR, null)
        _avatarUri.value = if (avatarStr != null) Uri.parse(avatarStr) else null

        _intro.value = prefs.getString(KEY_INTRO, "") ?: ""
    }

    // 💾 儲存暱稱（本機）
    fun saveNickname(ctx: Context, nickname: String) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NICK, nickname).apply()
        _nickname.value = nickname
    }

    // 💾 儲存相簿選擇的頭貼 URI（本機）
    fun saveAvatarUri(ctx: Context, uri: Uri?) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (uri != null) {
            prefs.edit().putString(KEY_AVATAR, uri.toString()).apply()
        } else {
            prefs.edit().remove(KEY_AVATAR).apply()
        }
        _avatarUri.value = uri
    }

    // 📸 儲存拍照 Bitmap（本機）
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

    /**
     * 儲存自我介紹：
     * 1. 先更新本機 SharedPreferences
     * 2. 再呼叫後端 update_profile.php，把 intro 一起更新到 users 表
     */
    suspend fun saveIntro(ctx: Context, intro: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1) 先更新本機
                val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                prefs.edit().putString(KEY_INTRO, intro).apply()

                // 2) 取得目前登入使用者資訊（user_id / provider）
                val userPrefs = UserPreferences(ctx)
                val user = userPrefs.getUser()
                if (user == null) {
                    Log.e("Profile", "saveIntro: User is null, 無法呼叫後端")
                    _intro.value = intro
                    return@withContext false
                }

                val userId = user.id           // 假設 UserData 有 id:Int
                val provider = user.provider   // 已知有 provider 屬性
                val nicknameNow = _nickname.value
                val avatarUrlNow = _avatarUri.value?.toString() ?: ""

                // 3) 準備 POST 資料（x-www-form-urlencoded）
                val params = listOf(
                    "user_id" to userId.toString(),
                    "provider" to provider,
                    "provider_id" to userId.toString(), // 目前用 userId 當 provider_id
                    "nickname" to nicknameNow,
                    "avatar_url" to avatarUrlNow,
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
                    doInput = true
                    doOutput = true
                    setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                    )
                }

                // 寫出 POST body
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

                val json = try {
                    JSONObject(responseText)
                } catch (e: Exception) {
                    Log.e("Profile", "解析 JSON 失敗", e)
                    null
                }

                val success = json?.optBoolean("success", false) ?: false
                if (success) {
                    _intro.value = intro
                    true
                } else {
                    // 後端回傳失敗時你可以看 message
                    val msg = json?.optString("message")
                    Log.e("Profile", "後端更新失敗: $msg")
                    false
                }
            } catch (e: Exception) {
                Log.e("Profile", "儲存自我介紹 / 呼叫後端失敗", e)
                false
            }
        }
    }

    // 🚪 登出
    fun logout(ctx: Context) = auth.logout(ctx)
}
