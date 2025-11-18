package com.example.myapplication.feature.roomdetail

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R
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

    // 角色
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
                                    tint = if (isFull) Color.LightGray else Color(0xFF424242) // 深灰主題色
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // 讓下面的背景圖可以露出來
        containerColor = Color.Transparent
    ) { padding ->

        // ===== 背景圖 + 內容 =====
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 背景圖片
            Image(
                painter = painterResource(R.drawable.bg_room_detail),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.25f
            )

            // 前景原本內容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttonColors = Color.DarkGray
                val cardBg = Color(0xFFE8E8E8).copy(alpha = 0.9f)
                val cardShape = RoundedCornerShape(16.dp)

                // 房主卡片
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(cardShape)
                        .background(cardBg)
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
                        val ownerIntro =
                            members.firstOrNull { it.id == safeRoom.ownerId }?.intro
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
                                .clip(cardShape)
                                .background(cardBg)
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
                val emptySlots =
                    (safeRoom.people - 1 - nonOwnerMembers.size).coerceAtLeast(0)
                repeat(emptySlots) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(cardShape)
                            .background(cardBg)
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

                // 下方資訊格：深灰透明底 + 白字
                val bottomBoxBg = Color(0xFF333333).copy(alpha = 0.7f)

                val boxModifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(
                        bottomBoxBg,
                        RoundedCornerShape(16.dp)
                    )
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
                        // 規則
                        Column(
                            boxModifier.clickable { showRuleDialog = true },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "規則",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Text(
                                "${safeRoom.basePoint}/${safeRoom.taiPoint}",
                                fontSize = 30.sp,
                                color = Color.White
                            )
                        }
                        // 時間
                        Column(
                            boxModifier,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "時間",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Text(
                                safeRoom.time,
                                fontSize = 30.sp,
                                color = Color.White
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                safeRoom.date,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 備註
                        Column(
                            boxModifier.clickable { showNoteDialog = true },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "備註",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Filled.MoreHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = Color.White
                            )
                        }
                        // 地點
                        Column(
                            boxModifier.clickable {
                                val gmmIntentUri =
                                    Uri.parse("geo:0,0?q=${safeRoom.location}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "地點",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Text(
                                safeRoom.location.take(10),
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
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
                Column(Modifier.padding(12.dp)) {
                    StandardDialogTitle("麻將設定")
                    Spacer(Modifier.height(12.dp))
                    StandardDialogContent("將數：${safeRoom.rounds}")
                    StandardDialogContent("花牌：${if (safeRoom.flower) "有" else "無"}")
                    StandardDialogContent("骰規：${if (safeRoom.diceRule) "有" else "無"}")
                    StandardDialogContent("哩咕：${if (safeRoom.ligu) "有" else "無"}")
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
                Column(Modifier.padding(12.dp)) {
                    StandardDialogTitle("房主備註")
                    Spacer(Modifier.height(12.dp))
                    StandardDialogContent(safeRoom.note?.ifEmpty { "無" } ?: "無")
                }
            }
        )
    }

    // 刪除房間（房主）
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Button(
                    onClick = {
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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF616161),
                        contentColor = Color.White
                    )
                ) { Text("確定") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF616161)
                    )
                ) { Text("取消") }
            },
            text = {
                Column(Modifier.padding(12.dp)) {
                    StandardDialogTitle("你確定要刪除房間？")
                }
            }
        )
    }

    // 離開房間（成員）
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            confirmButton = {
                Button(
                    onClick = {
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
                            } else {
                                snackbarHostState.showSnackbar("請先登入")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF616161),
                        contentColor = Color.White
                    )
                ) { Text("確定") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLeaveDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF616161)
                    )
                ) { Text("取消") }
            },
            text = {
                Column(Modifier.padding(12.dp)) {
                    StandardDialogTitle("你確定要離開房間？")
                }
            }
        )
    }

    // 加入房間（訪客）
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            confirmButton = {
                Button(
                    onClick = {
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
                                refresh()
                            } else {
                                snackbarHostState.showSnackbar("加入失敗，請稍後再試")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF616161),
                        contentColor = Color.White
                    )
                ) { Text("確定") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showJoinDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF616161)
                    )
                ) { Text("取消") }
            },
            text = {
                Column(Modifier.padding(12.dp)) {
                    StandardDialogTitle("你確定要加入這個房間？")
                }
            }
        )
    }
}

// 共用 Dialog 樣式
@Composable
fun StandardDialogTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = Color.Black
    )
}

@Composable
fun StandardDialogContent(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        color = Color.DarkGray
    )
}
