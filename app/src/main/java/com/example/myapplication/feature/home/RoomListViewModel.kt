package com.example.myapplication.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.RoomRepositoryImpl
import com.example.myapplication.domain.model.MahjongRoom
import com.example.myapplication.domain.model.Member
import com.example.myapplication.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RoomListViewModel(
    private val repo: RoomRepository = RoomRepositoryImpl()
) : ViewModel() {

    // 🔹 城市選擇
    private val selectedCity = MutableStateFlow("全台")

    // 🔹 所有房間資料（從後端抓）
    private val _allRooms = MutableStateFlow<List<MahjongRoom>>(emptyList())
    val allRooms: StateFlow<List<MahjongRoom>> = _allRooms

    // 🔹 篩選後的房間列表
    val rooms: StateFlow<List<MahjongRoom>> =
        combine(_allRooms, selectedCity) { list, city ->
            if (city == "全台") list else list.filter { it.city == city }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        loadRooms()
    }

    fun onCitySelected(city: String) {
        selectedCity.value = city
    }

    // 🔹 從後端載入房間資料
    fun loadRooms() {
        viewModelScope.launch {
            try {
                val result = repo.getRooms() // ✅ 後端 API：get_rooms.php
                _allRooms.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** ========= 下面是提供給畫面的中介方法（重要） ========= */
    suspend fun getRoom(roomId: String): MahjongRoom? = repo.getRoom(roomId)


    suspend fun getRoomMembers(roomId: String): List<Member> = repo.getRoomMembers(roomId)

    suspend fun deleteRoom(roomId: String): Boolean {
        val ok = repo.deleteRoom(roomId)
        if (ok) loadRooms()
        return ok
    }

    suspend fun leaveRoom(roomId: String, userId: String): Boolean {
        val ok = repo.leaveRoom(roomId, userId)
        if (ok) loadRooms()
        return ok
    }

    suspend fun isJoined(roomId: String, userId: String): Boolean =
        repo.isJoined(roomId, userId)

    suspend fun joinRoom(roomId: String, userId: String): Boolean =
        repo.joinRoom(roomId, userId)

    suspend fun createRoom(room: MahjongRoom): Boolean {
        val ok = repo.createRoom(room)
        if (ok) loadRooms()
        return ok
    }

    // ✅ 用 StateFlow 管理房間清單
    private val _roomList = MutableStateFlow<List<MahjongRoom>>(emptyList())
    val roomList = _roomList.asStateFlow()

    fun fetchRooms() {
        viewModelScope.launch {
            try {
                val result = repo.getRooms()
                _roomList.value = result
                Log.d("RoomListVM", "房間更新成功，共 ${result.size} 筆")
            } catch (e: Exception) {
                Log.e("RoomListVM", "fetchRooms 錯誤: ${e.message}", e)
            }
        }
    }


    fun currentUserId(): String? = repo.currentUserId()
}
