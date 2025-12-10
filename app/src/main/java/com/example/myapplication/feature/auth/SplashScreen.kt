package com.example.myapplication.feature.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.myapplication.data.repository.datasource.local.UserPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.*

@Composable
fun SplashScreen(navController: NavController) {
    val ctx = LocalContext.current
    val prefs = remember { UserPreferences(ctx) }

    LaunchedEffect(Unit) {
        delay(500) // 稍微等一下確保 prefs 可用
        withContext(Dispatchers.IO) {
            val user = prefs.getUserSync()
            val googleAccount = GoogleSignIn.getLastSignedInAccount(ctx)

            Log.d("AutoLogin", "🟡 檢查登入狀態：user=${user?.id}, google=${googleAccount != null}")

            // ⚠️ 切回主執行緒再導頁
            withContext(Dispatchers.Main) {
                if (user != null || googleAccount != null) {
                    Log.d("AutoLogin", "✅ 偵測到登入紀錄，跳首頁")
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    Log.d("AutoLogin", "❌ 沒有登入紀錄，跳登入頁")
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("載入中…")
    }
}
