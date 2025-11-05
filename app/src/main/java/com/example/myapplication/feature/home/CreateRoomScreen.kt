package com.example.myapplication.feature.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(navController: NavController, vm: RoomListViewModel = viewModel()) {
    // ▼ 麻將規則 ▼
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var showRuleDialog by remember { mutableStateOf(false) }
    var mahjongRounds by remember { mutableStateOf("") }
    var flower by remember { mutableStateOf(false) }
    var ligu by remember { mutableStateOf(false) }
    var diceRule by remember { mutableStateOf(false) }
    var basePoint by remember { mutableStateOf(30) }
    var taiPoint by remember { mutableStateOf(10) }
    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val calendar = remember { java.util.Calendar.getInstance() }

    // ▼ 城市選擇 ▼
    var expanded by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf("選擇城市") }
    val cityList = listOf(
        "台北市", "新北市", "基隆市", "桃園市", "新竹市", "新竹縣",
        "苗栗縣", "台中市", "彰化縣", "南投縣", "雲林縣", "嘉義市",
        "嘉義縣", "台南市", "高雄市", "屏東縣", "宜蘭縣", "花蓮縣",
        "台東縣", "澎湖縣", "金門縣", "連江縣"
    )

    //顏色設定
    val scope = rememberCoroutineScope()
    val grayBackground = Color(0xFFF5F5F5)
    val grayPrimary = Color(0xFFBDBDBD)
    val grayDark = Color(0xFF424242)

    // ▼ Google Places 選擇麻將館 ▼
    val context = LocalContext.current
    var selectedPlaceName by remember { mutableStateOf("") }
    var selectedPlaceAddress by remember { mutableStateOf("") }

    // 監聽 MainActivity 廣播，接收地點結果
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                selectedPlaceName = intent?.getStringExtra("name") ?: ""
                selectedPlaceAddress = intent?.getStringExtra("address") ?: ""
                location = selectedPlaceName
                println("✅ Compose接收到廣播：$selectedPlaceName / $selectedPlaceAddress")
                android.util.Log.d("PlacesDebug", "✅ Compose 接收到廣播：$selectedPlaceName / $selectedPlaceAddress")
            }
        }

        val filter = IntentFilter(MainActivity.PLACE_SELECTED_ACTION)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        // ✅ 使用 ContextCompat，支援所有版本
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }



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
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val currentUserId = App.supabase.auth.currentUserOrNull()?.id
                            vm.createRoom(
                                MahjongRoom(
                                    id = UUID.randomUUID().toString(),
                                    title = title.ifEmpty { "未命名房間" },
                                    ownerId = currentUserId ?: "1",
                                    people = 4,
                                    flower = flower,
                                    date = date.ifEmpty { "未設定日期" },
                                    time = time.ifEmpty { "未設定時間" },
                                    city = selectedCity,
                                    location = if (selectedPlaceName.isNotEmpty()) selectedPlaceName else selectedCity,
                                    rounds = mahjongRounds.toIntOrNull() ?: 4,
                                    diceRule = diceRule,
                                    ligu = ligu,
                                    basePoint = basePoint,
                                    taiPoint = taiPoint,
                                    note = note,
                                    createdAt = null,
                                    updatedAt = null
                                )
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = grayPrimary,
                        contentColor = grayDark
                    )
                ) {
                    Text("建立房間", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
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

            // ------------------------
            // ▼ 選擇麻將館（Google Maps）
            // ------------------------
            Button(
                onClick = {
                    // 啟動 Google Places 自動完成搜尋
                    val fields = listOf(
                        com.google.android.libraries.places.api.model.Place.Field.NAME,
                        com.google.android.libraries.places.api.model.Place.Field.ADDRESS
                    )

                    val intent = com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
                        com.google.android.libraries.places.widget.model.AutocompleteActivityMode.OVERLAY,
                        fields
                    )
                        .setCountries(listOf("TW")) // 限定台灣
                        .setHint("搜尋麻將館、娛樂館、場所")
                        .build(context)

                    (context as? Activity)?.startActivityForResult(
                        intent,
                        MainActivity.REQUEST_CODE_AUTOCOMPLETE
                    )
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
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "選擇麻將館",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("選擇麻將館", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // 顯示結果
            if (selectedPlaceName.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFBDBDBD))
                ) {
                    Text(
                        text = "$selectedPlaceName（$selectedPlaceAddress）",
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFBDBDBD))
                ) {
                    Text(
                        text = "尚未選擇麻將館",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

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

            // 🗓️ 選擇日期
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (date.isNotEmpty()) date else "選擇日期",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "選擇日期", tint = Color.DarkGray)
                    }
                )

                // ✅ 透明點擊層
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            val year = calendar.get(java.util.Calendar.YEAR)
                            val month = calendar.get(java.util.Calendar.MONTH)
                            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                            android.app.DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    date = String.format("%04d-%02d-%02d", y, m + 1, d)
                                },
                                year, month, day
                            ).apply {
                                datePicker.minDate = System.currentTimeMillis()
                            }.show()
                        }
                )
            }

            // 🕒 選擇時間
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (time.isNotEmpty()) time else "選擇時間",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Filled.Schedule, contentDescription = "選擇時間", tint = Color.DarkGray)
                    }
                )

                // ✅ 透明點擊層
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                            val minute = calendar.get(java.util.Calendar.MINUTE)

                            android.app.TimePickerDialog(
                                context,
                                { _, h, m ->
                                    time = String.format("%02d:%02d", h, m)
                                },
                                hour, minute, true
                            ).show()
                        }
                )
            }


            // ▼ 房主備註 ▼
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("房主備註（選填）") },
                placeholder = { Text("例如：記得帶麻將牌 / 有吃飯再開打") },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color.Gray
                )
            )

        }
    }
}
