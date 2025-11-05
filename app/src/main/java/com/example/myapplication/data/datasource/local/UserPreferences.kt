package com.example.myapplication.data.datasource.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_PIC_KEY = stringPreferencesKey("user_pic")
    }

    suspend fun saveUser(userId: String, name: String, pic: String?) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
            prefs[USER_NAME_KEY] = name
            if (pic != null) prefs[USER_PIC_KEY] = pic
        }
    }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ID_KEY]
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}