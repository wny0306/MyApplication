package com.example.myapplication.feature.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, vm: ProfileViewModel = viewModel()) {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.load(ctx) }

    var nickname by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    // 初始化輸入框的值（只做一次）
    val loaded by remember { mutableStateOf(true) }
    if (loaded) {
        nickname = vm.nickname.value
        bio = vm.bio.value
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("編輯個人資料") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("暱稱") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("自我介紹") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )
            Button(
                onClick = {
                    vm.save(ctx, nickname, bio)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("儲存") }
        }
    }
}
