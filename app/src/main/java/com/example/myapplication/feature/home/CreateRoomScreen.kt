package com.example.myapplication.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.App
import com.example.myapplication.domain.model.MahjongRoom
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(navController: NavController, vm: RoomListViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var flowerHas by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf("") }

    // ▼ 城市選擇 ▼
    var expanded by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf("選擇城市") }
    val cityList = listOf(
        "台北市", "新北市", "基隆市", "桃園市", "新竹市", "新竹縣",
        "苗栗縣", "台中市", "彰化縣", "南投縣", "雲林縣", "嘉義市",
        "嘉義縣", "台南市", "高雄市", "屏東縣", "宜蘭縣", "花蓮縣",
        "台東縣", "澎湖縣", "金門縣", "連江縣"
    )

    val scope = rememberCoroutineScope()

    val grayBackground = Color(0xFFF5F5F5)
    val grayPrimary = Color(0xFFBDBDBD)
    val grayDark = Color(0xFF424242)

    // ▼ 麻將規則 ▼
    var showRuleDialog by remember { mutableStateOf(false) }
    var mahjongRounds by remember { mutableStateOf("") }
    var flower by remember { mutableStateOf(false) }
    var ligu by remember { mutableStateOf(false) }
    var diceRule by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = grayBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "建立房間",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = grayDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = grayDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = grayPrimary,
                    titleContentColor = grayDark
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ▼ 城市選擇 ▼
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCity,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "選擇城市",
                                tint = grayDark
                            )
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true }
                )
            }

            if (expanded) {
                var currentIndex by remember {
                    mutableStateOf(cityList.indexOf(selectedCity).coerceAtLeast(0))
                }

                AlertDialog(
                    onDismissRequest = { expanded = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedCity = cityList[currentIndex]
                                expanded = false
                            }
                        ) {
                            Text("確定")
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("選擇地點", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(12.dp))

                            AndroidView(
                                factory = { context ->
                                    android.widget.NumberPicker(context).apply {
                                        minValue = 0
                                        maxValue = cityList.size - 1
                                        displayedValues = cityList.toTypedArray()
                                        value = currentIndex
                                        wrapSelectorWheel = false
                                        setOnValueChangedListener { _, _, newVal ->
                                            currentIndex = newVal
                                        }
                                    }
                                },
                                update = { picker -> picker.value = currentIndex }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                )

                LaunchedEffect(expanded) {
                    if (!expanded) selectedCity = cityList.getOrNull(
                        cityList.indexOf(selectedCity).coerceAtLeast(0)
                    ) ?: cityList.first()
                }
            }

            var selectedPlace by remember { mutableStateOf("尚未選擇麻將館") }

            Button(
                onClick = {
                    // TODO: 啟動 Google 地圖地點選擇
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = "地圖",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("選擇麻將館", fontSize = 18.sp, fontWeight = FontWeight.Bold,)
            }

            // 顯示選取結果
            Text(
                text = "目前選擇：$selectedPlace",
                fontSize = 20.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp)
            )


            // 麻將規則：底分／台分
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var basePoint by remember { mutableStateOf(50) }
                var taiPoint by remember { mutableStateOf(20) }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("底分", style = MaterialTheme.typography.bodyMedium)
                    AndroidView(
                        factory = { context ->
                            val picker = android.widget.NumberPicker(context)
                            val values = (10..100 step 10).toList()
                            picker.minValue = 0
                            picker.maxValue = values.size - 1
                            picker.displayedValues = values.map { it.toString() }.toTypedArray()
                            picker.value = values.indexOf(basePoint)
                            picker.setOnValueChangedListener { _, _, newVal ->
                                basePoint = values[newVal]
                            }
                            picker
                        },
                        update = { picker ->
                            val values = (10..100 step 10).toList()
                            val index = values.indexOf(basePoint)
                            if (index != -1) picker.value = index
                        }
                    )
                }

                Text("/", style = MaterialTheme.typography.titleLarge)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("台分", style = MaterialTheme.typography.bodyMedium)
                    AndroidView(
                        factory = { context ->
                            val picker = android.widget.NumberPicker(context)
                            val values = (5..50 step 5).toList()
                            picker.minValue = 0
                            picker.maxValue = values.size - 1
                            picker.displayedValues = values.map { it.toString() }.toTypedArray()
                            picker.value = values.indexOf(taiPoint)
                            picker.setOnValueChangedListener { _, _, newVal ->
                                taiPoint = values[newVal]
                            }
                            picker
                        },
                        update = { picker ->
                            val values = (5..50 step 5).toList()
                            val index = values.indexOf(taiPoint)
                            if (index != -1) picker.value = index
                        }
                    )
                }
            }

            // ▼ 麻將規則設定 ▼
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = "設定麻將規則",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showRuleDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "設定麻將規則",
                                tint = Color.DarkGray
                            )
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showRuleDialog = true }
                )
            }

            // 彈出 Dialog
            if (showRuleDialog) {
                AlertDialog(
                    onDismissRequest = { showRuleDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showRuleDialog = false }) {
                            Text("確定")
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "麻將規則設定",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = mahjongRounds,
                                onValueChange = { mahjongRounds = it.filter { c -> c.isDigit() } },
                                label = { Text("麻將將數（例如：3）") },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            )

                            Spacer(Modifier.height(16.dp))

                            val checkItems = listOf(
                                "補花" to flower,
                                "哩咕" to ligu,
                                "骰規" to diceRule
                            )

                            checkItems.forEach { (label, _) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Checkbox(
                                        checked = when (label) {
                                            "補花" -> flower
                                            "哩咕" -> ligu
                                            else -> diceRule
                                        },
                                        onCheckedChange = {
                                            when (label) {
                                                "補花" -> flower = it
                                                "哩咕" -> ligu = it
                                                else -> diceRule = it
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF00E676)
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // ▼ 時間輸入與建立按鈕 ▼
            val context = LocalContext.current
            val calendar = remember { java.util.Calendar.getInstance() }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (time.isNotEmpty()) time else "選擇時間",
                    onValueChange = {},
                    label = { Text("時間") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                            val minute = calendar.get(java.util.Calendar.MINUTE)

                            android.app.TimePickerDialog(
                                context,
                                { _, selectedHour, selectedMinute ->
                                    val formatted = String.format("%02d:%02d", selectedHour, selectedMinute)
                                    time = formatted
                                },
                                hour,
                                minute,
                                true
                            ).show()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "選擇時間",
                                tint = Color.DarkGray
                            )
                        }
                    }
                )

                // 🟢 透明可點擊層，覆蓋整個 TextField
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                            val minute = calendar.get(java.util.Calendar.MINUTE)

                            android.app.TimePickerDialog(
                                context,
                                { _, selectedHour, selectedMinute ->
                                    val formatted = String.format("%02d:%02d", selectedHour, selectedMinute)
                                    time = formatted
                                },
                                hour,
                                minute,
                                true
                            ).show()
                        }
                )
            }



            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        val currentUserId = App.supabase.auth.currentUserOrNull()?.id
                        vm.create(
                            MahjongRoom(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                ownerId = currentUserId.toString(),
                                people = 4,
                                flower = flowerHas,
                                time = time,
                                location = selectedCity
                            )
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = grayPrimary,
                    contentColor = grayDark
                )
            ) {
                Text("建立", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
