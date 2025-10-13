package com.example.myapplication.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.App
import com.example.myapplication.domain.model.MahjongRoom
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(navController: NavController, vm: RoomListViewModel) {
    var title by remember { mutableStateOf("") }
    var peopleText by remember { mutableStateOf("") } //
    var flowerHas by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("建立房間") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            OutlinedTextField(value = title, onValueChange = { title = it },
                label = { Text("房間名稱") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(value = peopleText, onValueChange = { peopleText = it },
                label = { Text("人數（例如：4）") }, modifier = Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = flowerHas, onCheckedChange = { flowerHas = it })
                Spacer(Modifier.width(8.dp)); Text("有花")
            }

            OutlinedTextField(value = time, onValueChange = { time = it },
                label = { Text("時間（例如：21:30）") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(value = location, onValueChange = { location = it },
                label = { Text("地點（例如：台北市）") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val people = peopleText.filter { it.isDigit() }.toIntOrNull() ?: 4
                    scope.launch {
                        val currentUserId = App.supabase.auth.currentUserOrNull()?.id
                        vm.create(
                            MahjongRoom(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                ownerId = currentUserId.toString(),
                                people = people,
                                flower = flowerHas,
                                time = time,
                                location = location
                            )
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("建立") }
        }
    }
}
