package com.example.myapplication.feature.home

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.myapplication.navigation.Routes


@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, vm: RoomListViewModel) {
    val roomList by vm.rooms.collectAsState()

    var showPicker by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf("全台") }

    LaunchedEffect(selectedCity) { vm.onCitySelected(selectedCity) }

    val cityList = listOf(
        "全台", "台北市", "新北市", "基隆市", "桃園市", "新竹市", "新竹縣", "苗栗縣", "台中市",
        "彰化縣", "南投縣", "雲林縣", "嘉義市", "嘉義縣", "台南市", "高雄市", "屏東縣",
        "宜蘭縣", "花蓮縣", "台東縣", "澎湖縣", "金門縣", "連江縣"
    )

    val grayBackground = Color(0xFFF5F5F5)
    val grayPrimary = Color(0xFFBDBDBD)
    val grayDark = Color(0xFF424242)

    Scaffold(
        containerColor = grayBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = grayPrimary, titleContentColor = grayDark
                ),
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("LINK UP", fontSize = 40.sp, color = grayDark)
                    }
                },
                navigationIcon = {
                    Button(
                        onClick = { showPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(selectedCity, color = Color.DarkGray, fontSize = 20.sp)
                    }
                },
                actions = {
                    Button(
                        onClick = { /* TODO: 分類 */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("分類", color = Color.DarkGray, fontSize = 20.sp)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = grayPrimary, contentColor = grayDark) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { /* TODO */ }) {
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
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            items(roomList, key = { it.id }) { room ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        .clickable {
                            Log.d("Home", "navigate to ${room.id}")
                            navController.navigate(Routes.RoomDetail.create(room.id))
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person, null,
                            Modifier.size(48.dp).padding(end = 16.dp), tint = grayDark
                        )
                        Column {
                            Text(room.title, fontSize = 18.sp)
                            Text("${room.people}人　花:${if (room.flower) "有" else "無"}")
                            Text("時間：${room.time}", fontSize = 12.sp, color = Color.Gray)
                            Text("地點：${room.location}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // --- 城市選擇器 Dialog ---
    if (showPicker) {
        CityPickerDialog(
            cityList = cityList,
            selectedCity = selectedCity,
            onConfirm = { city ->
                selectedCity = city
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
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
                // --- NumberPicker 區 ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 灰色選取框
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFCCCCCC), RoundedCornerShape(12.dp))
                    )

                    // 原生 NumberPicker
                    AndroidView(
                        factory = { context ->
                            NumberPicker(context).apply {
                                minValue = 0
                                maxValue = cityList.size - 1
                                value = currentIndex
                                displayedValues = cityList.toTypedArray()
                                wrapSelectorWheel = false
                                setOnValueChangedListener { _, _, newVal ->
                                    currentIndex = newVal
                                }

                                // ⚙️ 樣式調整
                                textSize = 40f
                                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

                                // 改變選中文字顏色為黑，其餘灰色
                                try {
                                    val field = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
                                    field.isAccessible = true
                                    val paint = field.get(this) as android.graphics.Paint
                                    paint.color = android.graphics.Color.BLACK
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // --- 底部按鈕 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onConfirm(cityList[currentIndex]) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D5AFE)),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.width(100.dp).height(40.dp)
                    ) { Text("確定", color = Color.White, fontSize = 18.sp) }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.width(100.dp).height(40.dp)
                    ) { Text("取消", color = Color.White, fontSize = 18.sp) }
                }
            }
        },
        containerColor = Color.Transparent
    )
}
