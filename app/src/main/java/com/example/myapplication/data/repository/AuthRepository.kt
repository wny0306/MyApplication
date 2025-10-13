package com.example.myapplication.data.repository

import android.content.Context

interface AuthRepository {
    fun signUp(ctx: Context, username: String, password: String): Boolean
    fun signIn(ctx: Context, username: String, password: String): Boolean
    fun currentUser(ctx: Context): String?
    fun setCurrentUser(ctx: Context, username: String)
    fun logout(ctx: Context)
}
