package com.example.myapplication.feature.profile

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, vm: ProfileViewModel = viewModel()) {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.load(ctx) }

    val nickname by vm.nickname.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {
        // 🔹 灰色圓弧背景（底邊向內凹）
        Canvas(modifier = Modifier.fillMaxWidth().height(260.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.62f)
                quadraticBezierTo(
                    w / 2f, h * 0.15f, // 中間上翹控制凹度
                    0f, h * 0.62f
                )
                close()
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
            // 🔹 返回鍵（真正與 TopAppBar 對齊）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars) // 對齊狀態列高度
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(start = 4.dp) // 左距略微補正，等同 TopAppBar 預設
                        .size(48.dp) // 按鈕點擊範圍一致
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp) // 與 TopAppBar 圖示大小一致
                    )
                }
            }


            Spacer(modifier = Modifier.height(40.dp))

            // 🔹 頭像
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "頭像",
                modifier = Modifier.size(100.dp),
                tint = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = nickname, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            // 🔹 功能選項
            ProfileOption("對局紀錄", Icons.Default.SportsEsports) {
                navController.navigate("matchHistory")
            }
            ProfileOption("發起紀錄", Icons.Default.Description) {
                navController.navigate("createHistory")
            }
            ProfileOption("設定", Icons.Default.Settings) {
                navController.navigate("editProfile")
            }
            ProfileOption("關於我們", Icons.Default.Info) { /* TODO */ }

            Spacer(modifier = Modifier.height(40.dp))

            // 🔹 登出按鈕
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

        // 🔹 登出確認對話框
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("確認登出") },
                text = { Text("確定要登出帳號嗎？") },
                confirmButton = {
                    TextButton(onClick = {
                        vm.logout(ctx)
                        Toast.makeText(ctx, "登出成功", Toast.LENGTH_SHORT).show()
                        navController.navigate("main") { popUpTo(0) { inclusive = true } }
                    }) { Text("確定") }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) { Text("取消") }
                }
            )
        }
    }
}

// 🔹 共用卡片選項元件
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
