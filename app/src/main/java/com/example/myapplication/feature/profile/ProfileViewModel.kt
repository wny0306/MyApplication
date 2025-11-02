package com.example.myapplication.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.datasource.AuthRepositoryImpl
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val PREFS = "profile_prefs"
private const val KEY_NICK = "nickname"

class ProfileViewModel(
    private val auth: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _username = MutableStateFlow("訪客")
    val username: StateFlow<String> = _username

    private val _nickname = MutableStateFlow("暱稱")
    val nickname: StateFlow<String> = _nickname

    fun load(ctx: Context) {
        _username.value = auth.currentUser(ctx) ?: "訪客"
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _nickname.value = p.getString(KEY_NICK, "暱稱") ?: "暱稱"
    }

    fun save(ctx: Context, nickname: String) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putString(KEY_NICK, nickname).apply()
        _nickname.value = nickname
    }

    fun logout(ctx: Context) = auth.logout(ctx)
}
