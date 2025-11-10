package com.example.myapplication.data.datasource.local

import android.content.Context
import android.util.Log
import org.json.JSONObject
import androidx.core.content.edit

object UserStorage {
    private const val PREF_NAME = "user_prefs"
    private const val USERS_KEY = "users_json"

    fun saveUser(context: Context, username: String, password: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val usersJson = prefs.getString(USERS_KEY, "{}")
        val users = JSONObject(usersJson!!)

        if (users.has(username)) return false // 帳號已存在

        users.put(username, password)
        prefs.edit { putString(USERS_KEY, users.toString()) }
        return true
    }

    fun validateUser(context: Context, username: String, password: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val usersJson = prefs.getString(USERS_KEY, "{}")
        val users = JSONObject(usersJson!!)
        return users.has(username) && users.getString(username) == password
    }

    fun getCurrentUser(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString("current_user", null)
    }

    fun setCurrentUser(context: Context, username: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString("current_user", username) }
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove("current_user") }
    }
}