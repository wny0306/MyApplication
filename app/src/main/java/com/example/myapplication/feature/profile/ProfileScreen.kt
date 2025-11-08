package com.example.myapplication.feature.profile

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.data.datasource.local.UserData
import com.example.myapplication.data.datasource.local.UserPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.linecorp.linesdk.api.LineApiClientBuilder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { UserPreferences(ctx) }

    // 載入暱稱與頭貼
    LaunchedEffect(Unit) { vm.load(ctx) }

    val nickname by vm.nickname.collectAsState()
    val avatarUri by vm.avatarUri.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // 取得完整使用者資料
    var userInfo by remember { mutableStateOf<UserData?>(null) }
    LaunchedEffect(Unit) {
        userInfo = prefs.getUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("個人資料", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE0E0E0),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .padding(padding)
        ) {
            // 上方灰色圓弧背景
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.62f)
                    quadraticBezierTo(w / 2f, h * 0.15f, 0f, h * 0.62f)
                }
                drawPath(path = path, color = Color(0xFFE0E0E0), style = Fill)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // 頭像
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(avatarUri),
                            contentDescription = "頭像",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "預設頭像",
                            tint = Color.DarkGray,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 顯示使用者資料
                userInfo?.let { user: UserData ->
                    Text(
                        text = "登入方式：${user.provider.uppercase()}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "官方名稱：${user.name.ifEmpty { "未提供" }}",
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 使用者自訂暱稱
                Text(
                    text = "暱稱：$nickname",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 功能選項
                ProfileOption("對局紀錄", Icons.Default.SportsEsports) {
                    navController.navigate("matchHistory")
                }
                ProfileOption("發起紀錄", Icons.Default.Description) {
                    navController.navigate("createHistory")
                }
                ProfileOption("設定", Icons.Default.Settings) {
                    navController.navigate("editProfile")
                }
                ProfileOption("關於我們", Icons.Default.Info) {
                    navController.navigate("about")
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 登出按鈕
                Button(
                    onClick = { showLogoutDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("登出帳號", fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 登出對話框
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("確認登出") },
                    text = { Text("確定要登出帳號嗎？") },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                try {
                                    prefs.clear()

                                    val googleClient = GoogleSignIn.getClient(
                                        ctx,
                                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                                    )
                                    googleClient.signOut()

                                    val lineClient =
                                        LineApiClientBuilder(ctx, "2008319508").build()
                                    lineClient.logout()

                                    Toast.makeText(ctx, "登出成功", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        ctx,
                                        "登出錯誤: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            showLogoutDialog = false
                        }) {
                            Text("確定")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) { Text("取消") }
                    }
                )
            }
        }
    }
}

// 共用選項卡
@Composable
fun ProfileOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = Color.DarkGray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp, color = Color.Black)
        }
    }
}
