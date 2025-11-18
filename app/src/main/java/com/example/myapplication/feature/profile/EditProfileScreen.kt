package com.example.myapplication.feature.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.data.datasource.local.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }

    val nickname by viewModel.nickname.collectAsState()
    val avatarUri by viewModel.avatarUri.collectAsState()

    var tempNickname by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    /* 自動載入 Profile */
    LaunchedEffect(Unit) { viewModel.load(context) }

    /* 將暱稱填入輸入框 */
    LaunchedEffect(nickname) {
        if (nickname.isNotBlank() && nickname != "暱稱") {
            tempNickname = nickname
        }
    }

    /* 相簿 */
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.saveAvatarUri(context, it) }
    }

    /* 拍照 */
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { viewModel.saveAvatarBitmap(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("編輯個人資料") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* 頭貼 */
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable { showDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(avatarUri),
                        contentDescription = "頭貼",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "預設頭貼",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("點擊更換頭貼", color = Color.Gray, fontSize = 14.sp)

            Spacer(Modifier.height(32.dp))

            /* 暱稱輸入框 */
            OutlinedTextField(
                value = tempNickname,
                onValueChange = { tempNickname = it },
                label = { Text("暱稱") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            /* 儲存按鈕 */
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val user = prefs.getUser() ?: run {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "尚未登入", Toast.LENGTH_SHORT).show()
                            }
                            return@launch
                        }

                        val userId = user.id
                        val provider = user.provider
                        val providerId = user.providerId   // ⭐ 必須讀取的欄位
                        val avatarUrl = avatarUri?.toString() ?: ""

                        try {
                            val url = URL("http://59.127.30.235:85/api/update_profile.php")

                            val postData =
                                "user_id=${Uri.encode(userId.toString())}" +
                                        "&provider=${Uri.encode(provider)}" +
                                        "&provider_id=${Uri.encode(providerId)}" +  // ⭐ 必須加入
                                        "&nickname=${Uri.encode(tempNickname)}" +
                                        "&avatar_url=${Uri.encode(avatarUrl)}"

                            val conn = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "POST"
                                doOutput = true
                                outputStream.use { it.write(postData.toByteArray()) }
                            }

                            val response = conn.inputStream.bufferedReader().readText()
                            val ok = runCatching {
                                JSONObject(response).optBoolean("success", true)
                            }.getOrDefault(true)

                            if (ok) {
                                viewModel.updateNicknameInMemory(tempNickname)

                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
                                    navController.navigate("profile") {
                                        popUpTo("editProfile") { inclusive = true }
                                    }
                                }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, "更新失敗", Toast.LENGTH_SHORT).show()
                                }
                            }

                        } catch (e: Exception) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "更新錯誤：${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F51B5),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("儲存", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    /* 選擇頭貼來源 Dialog */
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("選擇頭貼來源") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog = false
                                cameraLauncher.launch(null)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("拍照")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Photo, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("從相簿選擇")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("取消") }
            }
        )
    }
}
