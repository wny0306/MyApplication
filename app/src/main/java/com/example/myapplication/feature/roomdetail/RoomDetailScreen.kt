package com.example.myapplication.feature.roomdetail

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.domain.model.MahjongRoom
import com.example.myapplication.domain.model.Member
import com.example.myapplication.feature.home.RoomListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    navController: NavController,
    roomId: Int?,                       // ✅ 改成 Int
    vm: RoomListViewModel
) {
    var detail by remember { mutableStateOf<MahjongRoom?>(null) }

    // ✅ 從後端載入房間資料
    LaunchedEffect(roomId) {
        if (roomId != null && roomId > 0) {
            detail = vm.getRoom(roomId)
            Log.d("RoomDebug", "詳情載入完成，成員=${detail?.members?.size} / ${detail?.members}")
        }
    }

    if (detail == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("載入中...", color = Color.Gray)
        }
        return
    }

    val room = detail!!
    val safeRoom = room.copy(
        location = room.location.ifEmpty { "未設定地點" },
        date = room.date.ifEmpty { "未設定日期" },
        time = room.time.ifEmpty { "未設定時間" },
        basePoint = room.basePoint.takeIf { it > 0 } ?: 30,
        taiPoint = room.taiPoint.takeIf { it > 0 } ?: 10,
        rounds = room.rounds.takeIf { it > 0 } ?: 4,
        note = room.note ?: "無備註"
    )

    val members = safeRoom.members
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentUserId = vm.currentUserId()
    val grayDark = Color(0xFF424242)

    val role by remember(currentUserId, safeRoom.ownerId, members) {
        mutableStateOf(
            when {
                currentUserId == safeRoom.ownerId -> RoomViewerRole.Owner
                members.any { it.id == (currentUserId ?: -1) } -> RoomViewerRole.Member
                else -> RoomViewerRole.Visitor
            }
        )
    }

    var showRuleDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LINK UP", fontSize = 30.sp, color = grayDark)
                            Text(safeRoom.city, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.DarkGray)
                    }
                },
                actions = {
                    when (role) {
                        RoomViewerRole.Owner -> IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "刪除", tint = Color.DarkGray)
                        }
                        RoomViewerRole.Member -> IconButton(onClick = { showLeaveDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "離開", tint = Color.DarkGray)
                        }
                        else -> IconButton(onClick = { showJoinDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "加入", tint = Color(0xFF2196F3))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val buttonColors = Color.DarkGray
            val cardBg = Color.LightGray
            val cardShape = RoundedCornerShape(16.dp)

            // 🧩 房主卡片
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .background(cardBg, cardShape)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountBox, contentDescription = null, tint = buttonColors, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(24.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = safeRoom.ownerName ?: "未命名",
                            color = buttonColors,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.EmojiEvents,
                            contentDescription = "房主",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    val ownerIntro = members.firstOrNull { it.id == safeRoom.ownerId }?.intro
                    Text(
                        text = ownerIntro ?: "這位房主還沒有填寫自我介紹",
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                    )
                }
            }

            // 🧍‍♂️ 顯示其他已加入玩家
            members.filter { it.id != safeRoom.ownerId }.forEach { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(cardBg, cardShape)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = buttonColors, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(24.dp))
                    Column {
                        Text(member.name, color = buttonColors, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(member.intro.ifEmpty { "這位玩家還沒寫自我介紹" }, color = Color.DarkGray, fontSize = 14.sp)
                    }
                }
            }

            val nonOwnerMembers = members.filter { it.id != safeRoom.ownerId }
            val emptySlots = safeRoom.people - 1 - nonOwnerMembers.size
            repeat(emptySlots) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(cardBg, cardShape)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(24.dp))
                    Text("等待玩家加入", color = Color.Gray, fontSize = 25.sp)
                }
            }

            Spacer(Modifier.height(50.dp))

            // 🧩 下方資訊格
            val boxModifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                .padding(12.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(boxModifier.clickable { showRuleDialog = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Text("規則", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("${safeRoom.basePoint}/${safeRoom.taiPoint}", fontSize = 30.sp)
                    }

                    Column(boxModifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("時間", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(safeRoom.time, fontSize = 30.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(safeRoom.date, fontSize = 20.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(boxModifier.clickable { showNoteDialog = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Text("備註", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Icon(Icons.Default.MoreHoriz, contentDescription = null, modifier = Modifier.size(52.dp))
                    }

                    Column(boxModifier.clickable {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${safeRoom.location}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Text("地點", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(safeRoom.location.take(10), fontSize = 20.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    // ✅ 規則彈窗
    if (showRuleDialog) {
        AlertDialog(onDismissRequest = { showRuleDialog = false }, confirmButton = {}, text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("麻將設定", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("將數：${safeRoom.rounds}")
                Text("花牌：${if (safeRoom.flower) "有" else "無"}")
                Text("骰規：${if (safeRoom.diceRule) "有" else "無"}")
                Text("哩咕：${if (safeRoom.ligu) "有" else "無"}")
            }
        })
    }

    // ✅ 備註彈窗
    if (showNoteDialog) {
        AlertDialog(onDismissRequest = { showNoteDialog = false }, confirmButton = {}, text = {
            Column(Modifier.fillMaxWidth()) {
                Text("房主備註：", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                Text(safeRoom.note?.ifEmpty { "無" } ?: "無", fontSize = 18.sp, color = Color.DarkGray)
            }
        })
    }

    // ✅ 刪除房間
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        val ok = vm.deleteRoom(safeRoom.id)
                        if (ok) {
                            snackbarHostState.showSnackbar("房間已刪除")
                            navController.popBackStack()
                        } else {
                            snackbarHostState.showSnackbar("刪除失敗，請稍後再試")
                        }
                    }
                }) { Text("確定") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("否") }
            },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Text("你確定要刪除房間？", color = Color.Black, fontSize = 18.sp)
                }
            }
        )
    }

    // ✅ 離開房間
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            confirmButton = {
                Button(onClick = {
                    showLeaveDialog = false
                    scope.launch {
                        val uid = vm.currentUserId()
                        if (uid != null) {
                            val ok = vm.leaveRoom(safeRoom.id, uid)
                            if (ok) {
                                snackbarHostState.showSnackbar("已離開房間")
                                navController.popBackStack()
                            } else {
                                snackbarHostState.showSnackbar("離開失敗")
                            }
                        }
                    }
                }) { Text("確定") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLeaveDialog = false }) { Text("否") }
            },
            text = { Text("你確定要離開房間？", fontSize = 18.sp, color = Color.Black) }
        )
    }

    // ✅ 加入房間
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            confirmButton = {
                Button(onClick = {
                    showJoinDialog = false
                    scope.launch {
                        val uid = vm.currentUserId()
                        if (uid != null) {
                            val ok = vm.joinRoom(safeRoom.id, uid)
                            if (ok) snackbarHostState.showSnackbar("成功加入房間！")
                            else snackbarHostState.showSnackbar("加入失敗，請稍後再試")
                        } else {
                            // 這裡也可以導去登入
                        }
                    }
                }) { Text("確定") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showJoinDialog = false }) { Text("取消") }
            },
            text = { Text("你確定要加入這個房間？", fontSize = 18.sp, color = Color.Black) }
        )
    }
}
