package com.example.myapplication.feature.home


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.RoomRepositoryImpl
import com.example.myapplication.data.repository.RoomRepository
import com.example.myapplication.domain.model.MahjongRoom
import com.example.myapplication.domain.model.Member
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class RoomListViewModel(
    context: Context,
    private val repo: RoomRepository = RoomRepositoryImpl(context)
) : ViewModel() {


    // 🔹 城市選擇
    private val selectedCity = MutableStateFlow("全台")


    // 🔹 分類篩選狀態
    private val _filters = MutableStateFlow(Filters())
    val filters: StateFlow<Filters> = _filters.asStateFlow()
    fun applyFilters(newFilters: Filters) { _filters.value = newFilters }


    // 🔹 所有房間資料（從後端抓）
    private val _allRooms = MutableStateFlow<List<MahjongRoom>>(emptyList())
    val allRooms: StateFlow<List<MahjongRoom>> = _allRooms


    // 🔹 篩選後的房間列表（城市 × 分類）
    val rooms: StateFlow<List<MahjongRoom>> =
        combine(_allRooms, selectedCity, _filters) { list, city, f ->
            val byCity = if (city == "全台") list else list.filter { it.city == city }
            applyFilters(byCity, f)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    init { loadRooms() }


    fun onCitySelected(city: String) { selectedCity.value = city }


    // 🔹 從後端載入房間資料
    fun loadRooms() {
        viewModelScope.launch {
            try {
                val result = repo.getRooms() // 後端 API：get_rooms.php
                _allRooms.value = result
            } catch (e: Exception) {
                Log.e("RoomListVM", "loadRooms error: ${e.message}", e)
            }
        }
    }
    suspend fun getRoom(roomId: Int): MahjongRoom? = repo.getRoom(roomId)
    suspend fun getRoomMembers(roomId: Int): List<Member> = repo.getRoomMembers(roomId)


    suspend fun deleteRoom(roomId: Int): Boolean {
        val ok = repo.deleteRoom(roomId)
        if (ok) loadRooms()
        return ok
    }


    suspend fun leaveRoom(roomId: Int, userId: Int): Boolean {
        val ok = repo.leaveRoom(roomId, userId)
        if (ok) loadRooms()
        return ok
    }


    suspend fun isJoined(roomId: Int, userId: Int): Boolean = repo.isJoined(roomId, userId)
    suspend fun joinRoom(roomId: Int, userId: Int): Boolean = repo.joinRoom(roomId, userId)


    suspend fun createRoom(room: MahjongRoom): Boolean {
        Log.d("CreateRoom", "currentUserId() -> ${repo.currentUserId()}")
        Log.d("CreateRoom", "room.ownerId -> ${room.ownerId} (${room.ownerId::class.simpleName})")
        val ok = repo.createRoom(room)
        if (ok) loadRooms()
        return ok
    }


    // 若其他頁沿用可保留
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
    fun currentUserId(): Int? = repo.currentUserId()
}