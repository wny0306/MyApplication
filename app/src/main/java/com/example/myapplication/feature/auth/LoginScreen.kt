package com.example.myapplication.feature.auth

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.data.datasource.local.UserPreferences
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, vm: AuthViewModel = viewModel()) {
    val ctx = LocalContext.current
    val prefs = remember { UserPreferences(ctx) }
    var devPassword by remember { mutableStateOf("") }

    // 🧠 開發者密碼
    val developerPass = "1"

    val lineLoginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val intentData = result.data ?: return@rememberLauncherForActivityResult
            val loginResult = LineLoginApi.getLoginResultFromIntent(intentData)
            val code = loginResult.responseCode?.name ?: "UNKNOWN"

            when (code) {
                "SUCCESS" -> {
                    val profile = loginResult.lineProfile
                    val userId = profile?.userId ?: ""
                    val displayName = profile?.displayName ?: ""
                    val pictureUrl = profile?.pictureUrl?.toString() ?: ""

                    // ✅ 上傳到 PHP
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val url = URL("http://59.127.30.235:85/api/api_line_login.php")
                            val encodedName = Uri.encode(displayName)
                            val encodedPic = Uri.encode(pictureUrl)
                            val postData = "userId=$userId&displayName=$encodedName&pictureUrl=$encodedPic"
                            val conn = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "POST"
                                doOutput = true
                                outputStream.write(postData.toByteArray())
                            }
                            conn.inputStream.bufferedReader().readText()
                        } catch (e: Exception) {
                            Log.e("LINE_DB", "上傳失敗: ${e.message}")
                        }
                    }

                    // ✅ 儲存登入狀態
                    CoroutineScope(Dispatchers.IO).launch {
                        prefs.saveUser(userId, displayName, pictureUrl)
                    }

                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }

                else -> Log.e("LINE_LOGIN", "登入失敗: ${loginResult.errorData.message}")
            }
        } catch (e: Exception) {
            Log.e("LINE_LOGIN", "例外：${e.stackTraceToString()}")
        }
    }

    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 32.dp)
                .padding(top = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Link UP",
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 40.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.shimilogo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentScale = ContentScale.Fit
            )

            // ✅ 開發人員快速登入區塊
            OutlinedTextField(
                value = devPassword,
                onValueChange = { devPassword = it },
                label = { Text("開發者測試密碼") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (devPassword == developerPass) {
                        CoroutineScope(Dispatchers.IO).launch {
                            prefs.saveUser("dev_user", "Developer", "")
                            Log.d("AutoLogin", "✅ 使用開發者模式登入成功")
                        }
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Log.e("AutoLogin", "❌ 開發者密碼錯誤")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Black
                )
            ) {
                Text("開發者快速登入")
            }

            // ✅ LINE 登入按鈕
            Button(
                onClick = {
                    val loginIntent = LineLoginApi.getLoginIntent(
                        ctx,
                        "2008319508",
                        LineAuthenticationParams.Builder()
                            .scopes(listOf(Scope.PROFILE, Scope.OPENID_CONNECT))
                            .build()
                    )
                    lineLoginLauncher.launch(loginIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06C755),
                    contentColor = Color.White
                )
            ) {
                Text("使用 LINE 登入")
            }
        }
    }
}
