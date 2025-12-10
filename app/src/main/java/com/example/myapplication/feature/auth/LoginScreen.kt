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
import com.example.myapplication.data.repository.datasource.local.UserPreferences
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
//import java.nio.charset.Charsets   // ⭐ 這行很重要
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke

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
            .requestIdToken("213521066881-nttt15ipnd5mg500oeu0an81bq7ejthf.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(ctx, gso) }

    /* ---------------------------------------------------------------------- */
    /* ⭐ Google Login */
    /* ---------------------------------------------------------------------- */
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: ""
            val displayName = account.displayName ?: "Google 使用者"
            val email = account.email.orEmpty()
            val providerId = account.id ?: ""   // Google UID

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
                        setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        outputStream.use { it.write(postData.toByteArray(Charsets.UTF_8)) }
                    }

                    val code = conn.responseCode
                    val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    conn.disconnect()

                    val json = runCatching { JSONObject(body) }.getOrNull()
                    val success = json?.optBoolean("success") == true
                    val userId = json?.optInt("user_id", -1) ?: -1
                    val nicknameServer = json?.optString("nickname").orEmpty()
                    val avatarServer = json?.optString("avatar_url").orEmpty()

                    if (success && userId > 0) {

                        prefs.saveUser(
                            id = userId,
                            provider = "google",
                            providerId = providerId,
                            name = displayName,
                            avatarUrl = avatarServer
                        )

                        // ⭐ 第一次登入判斷（nickname 空）
                        val isFirstLogin = nicknameServer.isBlank()

                        CoroutineScope(Dispatchers.Main).launch {
                            if (isFirstLogin) {
                                navController.navigate("editProfile") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e("GoogleLogin", "Exception: ${e.message}")
                }
            }

        } catch (e: ApiException) {
            Log.e("GoogleLogin", "OAuth Failed: ${e.statusCode}")
        }
    }

    /* ---------------------------------------------------------------------- */
    /* ⭐ LINE Login */
    /* ---------------------------------------------------------------------- */
    val lineLoginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        try {
            val intentData = result.data ?: return@rememberLauncherForActivityResult
            val loginResult = LineLoginApi.getLoginResultFromIntent(intentData)

            if (loginResult.responseCode?.name == "SUCCESS") {
                val profile = loginResult.lineProfile
                val providerId = profile?.userId.orEmpty()
                val displayName = profile?.displayName ?: "LINE 使用者"
                val pictureUrl = profile?.pictureUrl?.toString().orEmpty()

                CoroutineScope(Dispatchers.IO).launch {
                    val url = URL("http://59.127.30.235:85/api/api_line_login.php")

                    val postData =
                        "userId=${Uri.encode(providerId)}" +
                                "&displayName=${Uri.encode(displayName)}" +
                                "&pictureUrl=${Uri.encode(pictureUrl)}"

                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        doOutput = true
                        outputStream.use { it.write(postData.toByteArray()) }
                    }

                    val code = conn.responseCode
                    val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader()?.use { it.readText() }.orEmpty()
                    conn.disconnect()

                    val json = runCatching { JSONObject(body) }.getOrNull()
                    val success = json?.optBoolean("success") == true
                    val userId = json?.optInt("user_id", -1) ?: -1
                    val nicknameServer = json?.optString("nickname").orEmpty()
                    val avatarServer = json?.optString("avatar_url").orEmpty()

                    if (success && userId > 0) {

                        prefs.saveUser(
                            id = userId,
                            provider = "line",
                            providerId = providerId,
                            name = displayName,
                            avatarUrl = avatarServer.ifEmpty { pictureUrl }
                        )

                        // ⭐ 第一次登入判斷
                        val isFirstLogin = nicknameServer.isBlank()

                        CoroutineScope(Dispatchers.Main).launch {
                            if (isFirstLogin) {
                                navController.navigate("editProfile") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("LINE_LOGIN", "Exception: ${e.message}")
        }
    }

    /* ---------------------------------------------------------------------- */
    /* ⭐ UI */
    /* ---------------------------------------------------------------------- */
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
                contentDescription = "Logo",
                modifier = Modifier.size(140.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "Link UP",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        val intent = googleSignInClient.signInIntent
                        googleLauncher.launch(intent)
                    },
                shape = RoundedCornerShape(28.dp),               // 圓角更大（接近膠囊按鈕）
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF7F7F7)          // 淺灰白背景
                ),
                elevation = CardDefaults.cardElevation(0.dp),     // 無陰影
                border = BorderStroke(1.dp, Color(0xFFD0D0D0))    // 淺灰色外框
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Icon(
                        painter = painterResource(R.drawable.google),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        "以 Google 帳號登入",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 16.dp)
                    .clickable {
                        val intent = LineLoginApi.getLoginIntent(
                            ctx,
                            "2008319508",
                            LineAuthenticationParams.Builder()
                                .scopes(listOf(Scope.PROFILE, Scope.OPENID_CONNECT))
                                .build()
                        )
                        lineLoginLauncher.launch(intent)
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF06C755)   // ⭐ LINE 綠
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Icon(
                        painter = painterResource(R.drawable.line),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        "以 LINE 帳號登入",               // ⭐ 文案可改回你的版本
                        fontSize = 16.sp,
                        color = Color.White               // ⭐ 文字白色
                    )
                }
            }


            /*OutlinedTextField(
                value = devPassword,
                onValueChange = { devPassword = it },
                label = { Text("開發者密碼") },
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp)
            )

            Button(
                onClick = {
                    if (devPassword == developerPass) {
                        CoroutineScope(Dispatchers.IO).launch {
                            prefs.saveUser(
                                id = 1,
                                provider = "developer",
                                providerId = "dev_user",
                                name = "Developer",
                                avatarUrl = ""
                            )
                        }
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 12.dp)
            ) {
                Text("開發者快速登入", fontSize = 16.sp)
            }*/
        }
    }
}
