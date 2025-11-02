package com.example.myapplication.feature.auth

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.linecorp.linesdk.auth.LineLoginApi
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.net.HttpURLConnection
import android.net.Uri

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, vm: AuthViewModel = viewModel()) {
    val ctx = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPwd by remember { mutableStateOf(false) }
    val msg by vm.message.collectAsState()

    // 👉 建立 LINE 登入的 launcher
    val lineLoginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val intentData = result.data
            if (intentData == null) {
                Log.e("LINE_LOGIN", "result.data 為 null，登入流程可能被中斷")
                return@rememberLauncherForActivityResult
            }

            val loginResult = LineLoginApi.getLoginResultFromIntent(intentData)
            val code = loginResult.responseCode?.name ?: "UNKNOWN"
            Log.d("LINE_LOGIN", "ResponseCode: $code")

            when (code) {
                "SUCCESS" -> {
                    val profile = loginResult.lineProfile
                    val userId = profile?.userId ?: ""
                    val displayName = profile?.displayName ?: ""
                    val pictureUrl = profile?.pictureUrl ?: ""

                    // ✅ 將資料上傳到你的 PHP 伺服器
                    // ✅ 將資料上傳到你的 PHP 伺服器
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val url = URL("http://59.127.30.235:85/api/api_line_login.php")


                            // 👇 指定使用 Charset 版本
                            val encodedName = Uri.encode(displayName).toString()
                            val encodedPic = Uri.encode(pictureUrl.toString())
                            val postData = "userId=$userId&displayName=$encodedName&pictureUrl=$encodedPic"

                            val conn = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "POST"
                                doOutput = true
                                outputStream.write(postData.toByteArray())
                            }

                            val response = conn.inputStream.bufferedReader().readText()
                            Log.d("LINE_DB", "伺服器回應：$response")

                        } catch (e: Exception) {
                            Log.e("LINE_DB", "上傳失敗: ${e.message}")
                        }
                    }

                    navController.navigate("home")
                }
                "CANCEL" -> {
                    Log.d("LINE_LOGIN", "使用者取消登入")
                }
                else -> {
                    Log.e("LINE_LOGIN", "登入失敗: ${loginResult.errorData.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("LINE_LOGIN", "例外：${e.stackTraceToString()}")
        }
    }




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("返回") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 32.dp)
                .padding(top = 130.dp),
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
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("帳號") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密碼") },
                visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPwd = !showPwd }) {
                        Icon(
                            if (showPwd) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Button(
                onClick = { vm.signIn(ctx, username, password) { navController.navigate("home") } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = Color.DarkGray
                )
            ) { Text("登入") }

            // 👇 新增 LINE 登入按鈕
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
                    containerColor = Color(0xFF06C755), // LINE 綠
                    contentColor = Color.White
                )
            ) {
                Text("使用 LINE 登入")
            }


            if (!msg.isNullOrBlank())
                Text(msg!!, color = MaterialTheme.colorScheme.error)
        }
    }
}
