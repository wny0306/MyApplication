package com.example.myapplication.feature.profile

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    vm: ProfileViewModel
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { UserPreferences(ctx) }

    // 載入暱稱 / 頭貼 / 自我介紹
    LaunchedEffect(Unit) { vm.load(ctx) }

    val nickname by vm.nickname.collectAsState(initial = "")
    Text(text = nickname)
    val avatarUri by vm.avatarUri.collectAsState()
    val intro by vm.intro.collectAsState()

    // 編輯中的自我介紹文字
    var editedIntro by remember { mutableStateOf("") }

    // 是否展開編輯模式
    var isEditingIntro by remember { mutableStateOf(false) }

    // 當 intro 改變時，同步到輸入框
    LaunchedEffect(intro) {
        editedIntro = intro
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

    // 取得完整使用者資料
    var userInfo by remember { mutableStateOf<UserData?>(null) }
    LaunchedEffect(Unit) {
        userInfo = prefs.getUser()
    }

    val scrollState = rememberScrollState()

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
                    .verticalScroll(scrollState)
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
                }

                // 使用者自訂暱稱
                Text(
                    text = "暱稱：$nickname",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ===== 自我介紹：折疊樣式 ＋ 展開編輯樣式 =====
                if (!isEditingIntro) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)          // 拿掉 .height(72.dp)
                            .clickable { isEditingIntro = true },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp) // ↑ 上下 padding 加大
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "自我介紹",
                                    tint = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "自我介紹",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (intro.isBlank()) "點擊編輯你的自我介紹" else intro,
                                fontSize = 14.sp,                // 字稍微放大一點
                                color = Color.Gray,
                                maxLines = 2,                     // 最多顯示兩行
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "自我介紹",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editedIntro,
                                onValueChange = { editedIntro = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                placeholder = { Text("寫點關於自己的介紹吧～") },
                                singleLine = false,
                                maxLines = 5
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${editedIntro.length}/200",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Row {
                                    TextButton(
                                        onClick = {
                                            // 取消編輯，恢復原本 intro
                                            editedIntro = intro
                                            isEditingIntro = false
                                        }
                                    ) {
                                        Text("取消")
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val ok = vm.saveIntro(ctx, editedIntro)
                                                if (ok) {
                                                    Toast.makeText(ctx, "自我介紹已儲存", Toast.LENGTH_SHORT).show()
                                                    isEditingIntro = false
                                                } else {
                                                    Toast.makeText(
                                                        ctx,
                                                        "儲存失敗，請稍後再試",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 6.dp
                                        )
                                    ) {
                                        Text("儲存", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 功能選項
                ProfileOption("對局紀錄", Icons.Default.SportsEsports) {
                    navController.navigate("matchHistory")
                }
                // 已移除「發起紀錄」
                ProfileOption("設定", Icons.Default.Settings) {
                    navController.navigate("editProfile")
                }
                ProfileOption("關於我們", Icons.Default.Info) {
                    navController.navigate("about")
                }

                Spacer(modifier = Modifier.height(40.dp))

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
