package com.example.myapplication.feature.roomdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.feature.common.ErrorScreen
import com.example.myapplication.feature.home.RoomListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    navController: NavController,
    roomId: String?,
    vm: RoomListViewModel
) {
    val allRooms by vm.allRooms.collectAsState()
    val room = allRooms.find { it.id == roomId }

    if (room == null) {
        ErrorScreen("找不到房間", navController)
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 角色判斷
    val currentUserId = vm.repo.currentUserId()  // 需要在 RoomRepository 定義成非 suspend 才能這樣用
    val role = when {
        room.ownerId == currentUserId -> RoomViewerRole.Owner
        else -> RoomViewerRole.Member // 這裡可以再強化成判斷 isJoined
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(room.title, color = Color.DarkGray) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.DarkGray)
                    }
                },
                actions = {
                    when (role) {
                        RoomViewerRole.Owner -> {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "刪除房間", tint = Color.DarkGray)
                            }
                        }
                        RoomViewerRole.Member -> {
                            IconButton(onClick = { showLeaveDialog = true }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "離開房間", tint = Color.DarkGray)
                            }
                        }
                        RoomViewerRole.Visitor -> {}
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF2F2F2)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(room.location, color = Color.Gray, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            val buttonModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(48.dp)
            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray,
                contentColor = Color.DarkGray
            )

            // 房主 / 我
            Button(
                onClick = { /* TODO: show profile */ },
                modifier = buttonModifier,
                colors = buttonColors,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AccountBox, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (role == RoomViewerRole.Owner) "我" else "房主資訊")
            }

            // 其他成員（示意）
            repeat((room.people - 1).coerceAtLeast(0)) {
                Button(
                    onClick = { /* TODO: 房客資訊 */ },
                    modifier = buttonModifier,
                    colors = buttonColors,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AccountBox, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("房客資訊")
                }
            }

            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("【麻將規定】", color = Color.DarkGray)
                    Text("人數：${room.people}", color = Color.DarkGray)
                    Text("花：${if (room.flower) "有" else "無"}", color = Color.DarkGray)
                }
                Column(Modifier.weight(1f)) {
                    Text("【備註】", color = Color.DarkGray)
                    Text("時間：${room.time}", color = Color.DarkGray)
                    Text("地點：${room.location}", color = Color.DarkGray)
                }
            }
        }
    }

    // 刪除房間確認
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {}, // 移除預設按鈕，改自訂
            dismissButton = {},
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFCDD2), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "你確定要刪除房間？",
                        color = Color.Black,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            scope.launch {
                                val ok = vm.repo.deleteRoom(room.id)
                                if (ok) {
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar("刪除失敗")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        Text("確定")
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        Text("否")
                    }
                }
            },
            containerColor = Color.Transparent // 讓紅底生效
        )
    }


    // 離開房間確認
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            confirmButton = {}, // 改成自訂布局
            dismissButton = {},
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFCDD2), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "你確定要離開房間？",
                        color = Color.Black,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showLeaveDialog = false
                            scope.launch {
                                val uid = vm.repo.currentUserId()
                                if (uid != null) {
                                    val ok = vm.repo.leaveRoom(room.id, uid)
                                    if (ok) {
                                        navController.popBackStack()
                                    } else {
                                        snackbarHostState.showSnackbar("離開失敗")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("未登入")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        Text("確定")
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { showLeaveDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        Text("否")
                    }
                }
            },
            containerColor = Color.Transparent // 透明背景，露出自訂紅底
        )
    }
}
