package com.example.myapplication.feature.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, vm: ProfileViewModel = viewModel()) {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.load(ctx) }

    val username by vm.username.collectAsState()
    val nickname by vm.nickname.collectAsState()
    val bio by vm.bio.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("個人資料") },
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
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = nickname, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = "帳號：$username", fontSize = 14.sp, color = Color.Gray)
                    Text(text = "自我介紹：", fontSize = 14.sp)
                    Text(text = bio, fontSize = 14.sp, color = Color.Gray)
                }
                Image(
                    painter = painterResource(id = R.drawable.myself),
                    contentDescription = "頭像",
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.LightGray)
                )
            }

            ProfileOptionItem("對局紀錄") { /* TODO */ }
            ProfileOptionItem("發起紀錄") { /* TODO */ }
            ProfileOptionItem("設定") { navController.navigate("editProfile") }
            ProfileOptionItem("關於我們") { /* TODO */ }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    vm.logout(ctx)
                    Toast.makeText(ctx, "登出成功", Toast.LENGTH_SHORT).show()
                    navController.navigate("main") { popUpTo(0) { inclusive = true } }
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("登出", fontSize = 18.sp) }
        }
    }
}

@Composable
private fun ProfileOptionItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(56.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 16.sp)
            Icon(Icons.Default.ChevronRight, contentDescription = "前往")
        }
    }
}
