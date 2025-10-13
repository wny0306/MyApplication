package com.example.myapplication.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.datasource.AuthRepositoryImpl
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val PREFS = "profile_prefs"
private const val KEY_NICK = "nickname"
private const val KEY_BIO = "bio"

class ProfileViewModel(
    private val auth: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _username = MutableStateFlow<String>("訪客")
    val username: StateFlow<String> = _username

    private val _nickname = MutableStateFlow("暱稱")
    val nickname: StateFlow<String> = _nickname

    private val _bio = MutableStateFlow("這是我的自我介紹")
    val bio: StateFlow<String> = _bio

    fun load(ctx: Context) {
        _username.value = auth.currentUser(ctx) ?: "訪客"
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _nickname.value = p.getString(KEY_NICK, "暱稱") ?: "暱稱"
        _bio.value = p.getString(KEY_BIO, "這是我的自我介紹") ?: "這是我的自我介紹"
    }

    fun save(ctx: Context, nickname: String, bio: String) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putString(KEY_NICK, nickname).putString(KEY_BIO, bio).apply()
        _nickname.value = nickname
        _bio.value = bio
    }

    fun logout(ctx: Context) = auth.logout(ctx)
}
