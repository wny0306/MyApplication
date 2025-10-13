package com.example.myapplication.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.datasource.AuthRepositoryImpl
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(
    private val repo: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun signIn(ctx: Context, username: String, password: String, onSuccess: () -> Unit) {
        if (repo.signIn(ctx, username, password)) {
            repo.setCurrentUser(ctx, username)
            onSuccess()
        } else _message.value = "帳號或密碼錯誤"
    }

    fun signUp(ctx: Context, username: String, password: String, onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            _message.value = "帳號或密碼不可為空"; return
        }
        if (repo.signUp(ctx, username, password)) {
            _message.value = "註冊成功"
            onSuccess()
        } else _message.value = "帳號已存在"
    }
}
