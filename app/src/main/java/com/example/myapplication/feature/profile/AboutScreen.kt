package com.example.myapplication.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("關於我們") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "LinkUp 麻將揪團平台",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = """
                本應用由大學專題團隊開發，
                旨在讓麻將愛好者能更輕鬆地建立與加入遊戲房間，
                並結合 Google 登入與地圖功能，
                打造更便利的線上組局體驗。
                """.trimIndent(),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            Text("版本：1.0.0", fontSize = 15.sp)
            Text("開發團隊：LinkUp Team", fontSize = 15.sp)
            Text("資工四A學生 林煒甯 梁宜姍 王宏恩", fontSize = 15.sp)
            Text("指導老師：林耀鈴 教授", fontSize = 15.sp)
            Text("聯絡信箱：linkup.team@gmail.com", fontSize = 15.sp)
        }
    }
}
