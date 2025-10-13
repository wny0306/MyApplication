package com.example.myapplication.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, vm: AuthViewModel = viewModel()) {
    val ctx = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPwd by remember { mutableStateOf(false) }
    val msg by vm.message.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("註冊") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 32.dp).padding(top = 130.dp),horizontalAlignment = Alignment.CenterHorizontally) {
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
                value = username, onValueChange = { username = it },
                label = { Text("帳號") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("密碼") },
                visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPwd = !showPwd }) {
                        Icon(if (showPwd) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
            Button(
                onClick = {
                    vm.signUp(ctx, username, password) {
                        // 註冊成功導回登入
                        navController.navigate("login")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant,contentColor = Color.DarkGray)
            ) { Text("註冊") }

            if (!msg.isNullOrBlank()) Text(msg!!, color = MaterialTheme.colorScheme.error)
        }
    }
}
