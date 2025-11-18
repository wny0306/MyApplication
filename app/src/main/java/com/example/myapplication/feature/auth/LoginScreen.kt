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
            val providerId = account.id ?: ""   // ⭐ Google provider_id = Google UID

            Log.d("GoogleLogin", "OAuth 成功: $displayName ($email) providerId=$providerId")

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
                            providerId = providerId,                               // ⭐ 必須
                            name = displayName,
                            avatarUrl = avatarServer
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
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
                val providerId = profile?.userId.orEmpty()  // ⭐ LINE provider_id
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
                            providerId = providerId,              // ⭐ 必須
                            name = displayName,
                            avatarUrl = avatarServer.ifEmpty { pictureUrl }
                        )

                        CoroutineScope(Dispatchers.Main).launch {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
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

            /* ---------------- Google Login Button ---------------- */
            Button(
                onClick = {
                    val intent = googleSignInClient.signInIntent
                    googleLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.google),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("使用 Google 登入", fontSize = 16.sp)
                }
            }

            /* ---------------- LINE Login Button ---------------- */
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
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06C755))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.line),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("使用 LINE 登入", fontSize = 16.sp)
                }
            }

            /* ---------------- Developer Login ---------------- */
            OutlinedTextField(
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
                                providerId = "dev_user",        // ⭐ 必須
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
            }
        }
    }
}
