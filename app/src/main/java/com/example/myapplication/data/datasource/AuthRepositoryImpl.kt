package com.example.myapplication.data.datasource

import android.content.Context
import com.example.myapplication.data.datasource.local.UserStorage
import com.example.myapplication.data.repository.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override fun signUp(ctx: Context, username: String, password: String) =
        UserStorage.saveUser(ctx, username, password)

    override fun signIn(ctx: Context, username: String, password: String) =
        UserStorage.validateUser(ctx, username, password)

    override fun currentUser(ctx: Context): String? =
        UserStorage.getCurrentUser(ctx)

    override fun setCurrentUser(ctx: Context, username: String) =
        UserStorage.setCurrentUser(ctx, username)

    override fun logout(ctx: Context) =
        UserStorage.logout(ctx)
}
