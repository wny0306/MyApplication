package com.example.myapplication.feature.auth

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.data.datasource.local.UserPreferences
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, vm: AuthViewModel = viewModel()) {
    val ctx = LocalContext.current
    val prefs = remember { UserPreferences(ctx) }
    var devPassword by remember { mutableStateOf("") }
    val developerPass = "1"
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("213521066881-nttt15ipnd5mg500oeu0an81bq7ejthf.apps.googleusercontent.c")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(ctx, gso) }


    // -------------- Google Sign-In 回呼 --------------
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: ""
            val displayName = account.displayName ?: "Google 使用者"
            val email = account.email.orEmpty()

            Log.d("GoogleLogin", "✅ OAuth 成功: $displayName ($email)")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = URL("http://59.127.30.235:85/api/google_login.php")
                    val postData =
                        "idToken=${Uri.encode(idToken)}&name=${Uri.encode(displayName)}&email=${Uri.encode(email)}"

                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        doOutput = true
                        connectTimeout = 15000
                        readTimeout = 15000
                        setRequestProperty(
                            "Content-Type",
                            "application/x-www-form-urlencoded; charset=UTF-8"
                        )
                        outputStream.use { it.write(postData.toByteArray(Charsets.UTF_8)) }
                    }

                    val code = conn.responseCode
                    val body = try {
                        (if (code in 200..299) conn.inputStream else conn.errorStream)
                            ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    } finally { conn.disconnect() }

                    Log.d("Google_DB", "HTTP $code body=$body")

                    val json = runCatching { JSONObject(body) }.getOrNull()
                    val success = json?.optBoolean("success") == true
                    val userId = json?.optInt("user_id", -1) ?: -1
                    val nameFromServer = json?.optString("name").orEmpty()
                    val avatarFromServer = json?.optString("avatar_url").orEmpty()

                    if (code in 200..299 && success && userId > 0) {
                        prefs.saveUser(
                            id = userId,
                            provider = "google",
                            name = nameFromServer.ifEmpty { displayName },
                            nickname = nameFromServer.ifEmpty { displayName },
                            avatarUrl = avatarFromServer
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    } else {
                        Log.e("Google_DB", "❌ 後端失敗或 user_id 無效 (code=$code)")
                    }
                } catch (e: Exception) {
                    Log.e("Google_DB", "❌ 例外: ${e.message}")
                }
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "❌ OAuth 失敗: ${e.statusCode}")
        }
    }

    // -------------- LINE Login 回呼 --------------
    val lineLoginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val intentData = result.data ?: return@rememberLauncherForActivityResult
            val loginResult = LineLoginApi.getLoginResultFromIntent(intentData)
            when (loginResult.responseCode?.name) {
                "SUCCESS" -> {
                    val profile = loginResult.lineProfile
                    val lineUid = profile?.userId.orEmpty()          // provider_id
                    val displayName = profile?.displayName ?: "LINE 使用者"
                    val pictureUrl = profile?.pictureUrl?.toString().orEmpty()

                    Log.d("LINE_LOGIN", "✅ OAuth 成功: $displayName ($lineUid)")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val url = URL("http://59.127.30.235:85/api/api_line_login.php")
                            val postData =
                                "userId=${Uri.encode(lineUid)}&displayName=${Uri.encode(displayName)}&pictureUrl=${Uri.encode(pictureUrl)}"

                            val conn = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "POST"
                                doOutput = true
                                connectTimeout = 15000
                                readTimeout = 15000
                                setRequestProperty(
                                    "Content-Type",
                                    "application/x-www-form-urlencoded; charset=UTF-8"
                                )
                                outputStream.use { it.write(postData.toByteArray(Charsets.UTF_8)) }
                            }

                            val code = conn.responseCode
                            val body = try {
                                (if (code in 200..299) conn.inputStream else conn.errorStream)
                                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                            } finally { conn.disconnect() }

                            Log.d("LINE_DB", "HTTP $code body=$body")

                            val json = runCatching { JSONObject(body) }.getOrNull()
                            val success = json?.optBoolean("success") == true
                            val userId = json?.optInt("user_id", -1) ?: -1   // 回全域 users.id
                            val nameFromServer = json?.optString("name").orEmpty()
                            val avatarFromServer = json?.optString("avatar_url").orEmpty()

                            if (code in 200..299 && success && userId > 0) {
                                prefs.saveUser(
                                    id = userId,                         // ✅ Int（全域）
                                    provider = "line",
                                    name = nameFromServer.ifEmpty { displayName },
                                    nickname = nameFromServer.ifEmpty { displayName },
                                    avatarUrl = avatarFromServer.ifEmpty { pictureUrl }
                                )
                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            } else {
                                Log.e("LINE_DB", "❌ 後端失敗或 user_id 無效 (code=$code)")
                            }
                        } catch (e: Exception) {
                            Log.e("LINE_DB", "❌ 例外: ${e.message}")
                        }
                    }
                }
                else -> Log.e("LINE_LOGIN", "❌ OAuth 失敗: ${loginResult.errorData.message}")
            }
        } catch (e: Exception) {
            Log.e("LINE_LOGIN", "❌ 例外：${e.stackTraceToString()}")
        }
    }

    // ---------- UI ----------
    Scaffold(containerColor = Color(0xFFF8F9FA)) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.shimilogo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .height(140.dp)
                    .width(140.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Link UP",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            val buttonHeight = 56.dp
            val buttonShape = RoundedCornerShape(14.dp)

            // Google 登入
            Button(
                onClick = {
                    val intent = googleSignInClient.signInIntent
                    googleLauncher.launch(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight)
                    .shadow(3.dp, buttonShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = buttonShape
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("使用 Google 登入", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            // LINE 登入
            Button(
                onClick = {
                    val intent = LineLoginApi.getLoginIntent(
                        ctx,
                        "2008319508",
                        LineAuthenticationParams.Builder()
                            .scopes(listOf(Scope.PROFILE, Scope.OPENID_CONNECT))
                            .build()
                    )
                    lineLoginLauncher.launch(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight)
                    .padding(top = 16.dp)
                    .shadow(3.dp, buttonShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06C755),
                    contentColor = Color.White
                ),
                shape = buttonShape
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.line),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("使用 LINE 登入", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            // 開發者登入（本地預設 Int ID）
            OutlinedTextField(
                value = devPassword,
                onValueChange = { devPassword = it },
                label = { Text("開發者測試密碼") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
            )
            Button(
                onClick = {
                    if (devPassword == developerPass) {
                        CoroutineScope(Dispatchers.IO).launch {
                            prefs.saveUser(
                                id = 1,                    // ✅ Int
                                provider = "developer",
                                name = "Developer",
                                nickname = "Developer",
                                avatarUrl = ""
                            )
                            Log.d("AutoLogin", "✅ 開發者模式登入成功")
                        }
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Log.e("AutoLogin", "❌ 密碼錯誤")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight)
                    .padding(top = 12.dp)
                    .shadow(2.dp, buttonShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0),
                    contentColor = Color(0xFF333333)
                ),
                shape = buttonShape
            ) {
                Text("開發者快速登入", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
