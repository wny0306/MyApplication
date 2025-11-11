package com.example.myapplication.feature.roomdetail

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.myapplication.feature.home.RoomListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    navController: NavController,
    roomId: Int?,
    vm: RoomListViewModel
) {
    var detail by remember { mutableStateOf<MahjongRoom?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showRuleDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    // 重新整理房間詳情
    fun refresh() {
        if (roomId != null && roomId > 0) {
            scope.launch {
                detail = vm.getRoom(roomId)
                Log.d(
                    "RoomDebug",
                    "刷新完成，成員=${detail?.members?.size} / ${detail?.members}"
                )
            }
        }
    }

    // 初次載入
    LaunchedEffect(roomId) {
        refresh()
    }

    // 載入中
    if (detail == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("載入中...", color = Color.Gray)
        }
        return
    }

    // ---------- 衍生資料 ----------

    val room = detail!!
    val grayDark = Color(0xFF424242)

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
    val currentUserId = vm.currentUserId()

    // 角色以目前 detail 直接計算（避免 remember 卡住）
    val role = when {
        currentUserId == safeRoom.ownerId -> RoomViewerRole.Owner
        members.any { it.id == (currentUserId ?: -1) } -> RoomViewerRole.Member
        else -> RoomViewerRole.Visitor
    }

    // 房間是否已滿（含房主）
    val isFull = members.size >= safeRoom.people

    // ---------- UI ----------

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LINK UP", fontSize = 30.sp, color = grayDark)
                            Text(safeRoom.city, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.DarkGray
                        )
                    }
                },
                actions = {
                    when (role) {
                        RoomViewerRole.Owner -> {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "刪除",
                                    tint = Color.DarkGray
                                )
                            }
                        }

                        RoomViewerRole.Member -> {
                            IconButton(onClick = { showLeaveDialog = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "離開",
                                    tint = Color.DarkGray
                                )
                            }
                        }

                        RoomViewerRole.Visitor -> {
                            IconButton(
                                onClick = { showJoinDialog = true },
                                enabled = !isFull
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = if (isFull) "房間已滿" else "加入",
                                    tint = if (isFull) Color.LightGray else Color(0xFF2196F3)
                                )
                            }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val buttonColors = Color.DarkGray
            val cardBg = Color(0xFFE8E8E8)
            val cardShape = RoundedCornerShape(16.dp)

            // 房主卡片
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(2.dp, cardShape)
                    .background(cardBg, cardShape)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBox,
                    contentDescription = null,
                    tint = buttonColors,
                    modifier = Modifier.size(30.dp)
                )
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
                        fontSize = 14.sp
                    )
                }
            }

            // 其他成員
            members
                .filter { it.id != safeRoom.ownerId }
                .forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(cardBg, cardShape)
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = buttonColors,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.width(24.dp))
                        Column {
                            Text(
                                member.name,
                                color = buttonColors,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                member.intro.ifEmpty { "這位玩家還沒寫自我介紹" },
                                color = Color.DarkGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

            // 空位
            val nonOwnerMembers = members.filter { it.id != safeRoom.ownerId }
            val emptySlots = (safeRoom.people - 1 - nonOwnerMembers.size).coerceAtLeast(0)
            repeat(emptySlots) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(cardBg, cardShape)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(24.dp))
                    Text("等待玩家加入", color = Color.Gray, fontSize = 25.sp)
                }
            }

            Spacer(Modifier.height(50.dp))

            // 下方資訊格
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        boxModifier.clickable { showRuleDialog = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("規則", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("${safeRoom.basePoint}/${safeRoom.taiPoint}", fontSize = 30.sp)
                    }
                    Column(
                        boxModifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("時間", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(safeRoom.time, fontSize = 30.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(safeRoom.date, fontSize = 20.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        boxModifier.clickable { showNoteDialog = true },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("備註", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    Column(
                        boxModifier.clickable {
                            val gmmIntentUri = Uri.parse("geo:0,0?q=${safeRoom.location}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("地點", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            safeRoom.location.take(10),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // ---------- 彈窗 ----------

    // 規則
    if (showRuleDialog) {
        AlertDialog(
            onDismissRequest = { showRuleDialog = false },
            confirmButton = {},
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("麻將設定", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("將數：${safeRoom.rounds}")
                    Text("花牌：${if (safeRoom.flower) "有" else "無"}")
                    Text("骰規：${if (safeRoom.diceRule) "有" else "無"}")
                    Text("哩咕：${if (safeRoom.ligu) "有" else "無"}")
                }
            }
        )
    }

    // 備註
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            confirmButton = {},
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        "房主備註：",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        safeRoom.note?.ifEmpty { "無" } ?: "無",
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    )
                }
            }
        )
    }

    // 刪除房間（房主）
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

    // 離開房間（成員）
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
                                // 留在本頁可改為 refresh()
                                navController.popBackStack()
                            } else {
                                snackbarHostState.showSnackbar("離開失敗")
                            }
                        } else {
                            snackbarHostState.showSnackbar("請先登入")
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

    // 加入房間（訪客）
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            confirmButton = {
                Button(onClick = {
                    showJoinDialog = false
                    scope.launch {
                        val uid = vm.currentUserId()
                        if (uid == null) {
                            snackbarHostState.showSnackbar("請先登入")
                            return@launch
                        }
                        if (role != RoomViewerRole.Visitor) {
                            snackbarHostState.showSnackbar("你已在房間中")
                            return@launch
                        }
                        if (isFull) {
                            snackbarHostState.showSnackbar("房間已滿，無法加入")
                            return@launch
                        }

                        val ok = vm.joinRoom(safeRoom.id, uid)
                        if (ok) {
                            snackbarHostState.showSnackbar("成功加入房間！")
                            refresh() // 立即刷新畫面：成員 + 角色變化
                        } else {
                            snackbarHostState.showSnackbar("加入失敗，請稍後再試")
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
