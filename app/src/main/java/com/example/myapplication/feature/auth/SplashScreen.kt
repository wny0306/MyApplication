package com.example.myapplication.feature.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.myapplication.data.datasource.local.UserPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val ctx = LocalContext.current
    val prefs = remember { UserPreferences(ctx) }
    val userId by prefs.userIdFlow.collectAsState(initial = null)
    val googleAccount = GoogleSignIn.getLastSignedInAccount(ctx)

    LaunchedEffect(userId, googleAccount) {
        Log.d("AutoLogin", "🟡 檢查登入狀態：userId=$userId, google=${googleAccount != null}")

        // 稍等一下確保 DataStore 已經讀完
        delay(500)

        if (!userId.isNullOrEmpty() || googleAccount != null) {
            Log.d("AutoLogin", "✅ 偵測到登入狀態，直接跳首頁")
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

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("載入中…")
    }
}
