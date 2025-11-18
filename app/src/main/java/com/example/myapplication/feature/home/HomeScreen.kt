@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.feature.home

import android.annotation.SuppressLint
import android.os.Build
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.myapplication.domain.model.MahjongRoom
import com.example.myapplication.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomeScreen(navController: NavController, vm: RoomListViewModel) {
    val roomList by vm.rooms.collectAsState()

    // 城市選擇
    var showCityPicker by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf("全台") }
    LaunchedEffect(selectedCity) { vm.onCitySelected(selectedCity) }

    // 分類 Dialog 顯示
    var showFilters by remember { mutableStateOf(false) }
    val currentFilters by vm.filters.collectAsState()

    // 下拉刷新
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Snackbar 用來顯示錯誤 / 提示訊息
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFBDBDBD),
                    titleContentColor = Color(0xFF424242)
                ),
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("LINK UP", fontSize = 40.sp, color = Color(0xFF424242))
                    }
                },
                navigationIcon = {
                    Button(
                        onClick = { showCityPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) { Text(selectedCity, color = Color.DarkGray, fontSize = 20.sp) }
                },
                actions = {
                    Button(
                        onClick = { showFilters = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) { Text("分類", color = Color.DarkGray, fontSize = 20.sp) }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color(0xFFBDBDBD), contentColor = Color(0xFF424242)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // 🔽 這顆是「跳轉到自己所在房間」的按鈕
                    IconButton(
                        onClick = {
                            scope.launch {
                                // 在 ViewModel 中實作 suspend fun getMyCurrentRoomId(): String?
                                val roomId = vm.getMyCurrentRoomId()

                                if (roomId != null) {
                                    navController.navigate(Routes.RoomDetail.create(roomId))
                                } else {
                                    snackbarHostState.showSnackbar("你目前沒有加入任何房間")
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.DoorFront,
                            contentDescription = "跳轉所在的房間",
                            Modifier.size(48.dp)
                        )
                    }

                    IconButton(onClick = { navController.navigate(Routes.CreateRoom.path) }) {
                        Icon(Icons.Default.Add, null, Modifier.size(48.dp))
                    }
                    IconButton(onClick = { navController.navigate(Routes.Profile.path) }) {
                        Icon(Icons.Default.Person, null, Modifier.size(48.dp))
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    vm.loadRooms()
                    delay(800)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(roomList, key = { it.id }) { room ->
                    RoomCard(
                        room = room,
                        navController = navController,
                        grayDark = Color(0xFF424242)
                    )
                }
            }
        }
    }

    // 城市選擇 Dialog
    if (showCityPicker) {
        CityPickerDialog(
            cityList = listOf(
                "全台", "台北市", "新北市", "基隆市", "桃園市", "新竹市", "新竹縣", "苗栗縣",
                "台中市", "彰化縣", "南投縣", "雲林縣", "嘉義市", "嘉義縣", "台南市",
                "高雄市", "屏東縣", "宜蘭縣", "花蓮縣", "台東縣", "澎湖縣", "金門縣", "連江縣"
            ),
            selectedCity = selectedCity,
            onConfirm = { city ->
                selectedCity = city
                showCityPicker = false
            },
            onDismiss = { showCityPicker = false }
        )
    }

    // 分類 Dialog
    RoomFiltersDialog(
        visible = showFilters,
        initial = currentFilters,
        onApply = { vm.applyFilters(it) },
        onDismiss = { showFilters = false },
        roundsOptions = remember(roomList) { roomList.map { it.rounds }.distinct().sorted() }
            .ifEmpty { listOf(8, 16, 32) }
    )
}

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("SoonBlockedPrivateApi")
@Composable
fun CityPickerDialog(
    cityList: List<String>,
    selectedCity: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(cityList.indexOf(selectedCity).coerceAtLeast(0)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(vertical = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 32.dp),
                ) {
                    AndroidView(
                        factory = { context ->
                            NumberPicker(context).apply {
                                minValue = 0
                                maxValue = cityList.size - 1
                                value = currentIndex
                                displayedValues = cityList.toTypedArray()
                                wrapSelectorWheel = false
                                setOnValueChangedListener { _, _, newVal -> currentIndex = newVal }
                                textSize = 40f
                                descendantFocusability =
                                    NumberPicker.FOCUS_BLOCK_DESCENDANTS
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = onDismiss) { Text("取消", color = Color.White) }
                    Button(onClick = { onConfirm(cityList[currentIndex]) }) {
                        Text("確定", color = Color.White)
                    }
                }
            }
        },
        containerColor = Color.Transparent
    )
}

@Composable
fun RoomCard(
    room: MahjongRoom,
    navController: NavController,
    grayDark: Color = Color(0xFF424242)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate(Routes.RoomDetail.create(room.id)) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person, null,
                Modifier
                    .size(48.dp)
                    .padding(end = 16.dp),
                tint = grayDark
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = room.location.ifEmpty { "未知地點" },
                        fontSize = 18.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp)
                    )
                    Text(
                        text = "${room.date.ifEmpty { "未設定" }} ${room.time.ifEmpty { "" }}",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "目前 ${room.memberCount}/${room.people} 人",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${room.basePoint}/${room.taiPoint}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "${room.rounds}將", fontSize = 22.sp)
                }
            }
        }
    }
}
