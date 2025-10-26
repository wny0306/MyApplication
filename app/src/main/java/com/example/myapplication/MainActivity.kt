package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myapplication.navigation.AppNavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity

class MainActivity : ComponentActivity() {

    companion object {
        const val REQUEST_CODE_AUTOCOMPLETE = 1001
        const val PLACE_SELECTED_ACTION = "PLACE_SELECTED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 初始化 Google Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyA0n4j3Rq4FJPpGelyzMIKwPJwdu0qYJIg")
        }

        setContent {
            MyApplicationTheme {
                AppNavGraph()
            }
        }
    }

    // ✅ 接收 Autocomplete 回傳的搜尋結果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        android.util.Log.d("PlacesDebug", "✅ onActivityResult 被呼叫，requestCode=$requestCode resultCode=$resultCode")

        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    val name = place.name ?: ""
                    val address = place.address ?: ""

                    // ✅ 這裡才可以安全地印出 name / address
                    android.util.Log.d("PlacesDebug", "✅ 選擇結果：$name / $address")

                    // ✅ 使用廣播把結果送回 Compose 畫面
                    val resultIntent = Intent(PLACE_SELECTED_ACTION).apply {
                        putExtra("name", name)
                        putExtra("address", address)
                    }
                    sendBroadcast(resultIntent)
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    android.util.Log.e("PlacesDebug", "❌ 錯誤：${status.statusMessage}")
                }

                Activity.RESULT_CANCELED -> {
                    android.util.Log.d("PlacesDebug", "使用者取消選擇")
                }
            }
        }
    }
}
