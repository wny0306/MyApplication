package com.example.myapplication.feature.roomdetail


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.RoomRepository
import com.example.myapplication.domain.model.MahjongRoom
import com.example.myapplication.core.DefaultRoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



enum class RoomViewerRole { Owner, Member, Visitor }

class RoomDetailViewModel(
    private val repo: RoomRepository = DefaultRoomRepository()
) : ViewModel() {

    private val _room = MutableStateFlow<MahjongRoom?>(null)
    val room: StateFlow<MahjongRoom?> = _room

    private val _role = MutableStateFlow(RoomViewerRole.Visitor)
    val role: StateFlow<RoomViewerRole> = _role

    private val _isJoined = MutableStateFlow(false)
    val isJoined: StateFlow<Boolean> = _isJoined

    fun loadRoom(roomId: String?) {
        if (roomId == null) return
        viewModelScope.launch {
            val r = repo.getRoom(roomId)
            _room.value = r

            val uid = repo.currentUserId()
            if (r != null && uid != null) {
                if (r.ownerId == uid) {
                    _role.value = RoomViewerRole.Owner
                    _isJoined.value = true
                } else {
                    val joined = repo.isJoined(r.id, uid)
                    _isJoined.value = joined
                    _role.value = if (joined) RoomViewerRole.Member else RoomViewerRole.Visitor
                }
            }
        }
    }

    fun deleteRoom(callback: (Boolean, String?) -> Unit) {
        val id = _room.value?.id ?: return callback(false, "房間不存在")
        viewModelScope.launch {
            runCatching { repo.deleteRoom(id) }
                .onSuccess { callback(true, null) }
                .onFailure { callback(false, it.message) }
        }
    }

    fun leaveRoom(callback: (Boolean, String?) -> Unit) {
        val id = _room.value?.id ?: return callback(false, "房間不存在")
        viewModelScope.launch {
            val uid = repo.currentUserId()
                ?: return@launch callback(false, "未登入")
            runCatching { repo.leaveRoom(id, uid) }
                .onSuccess { callback(true, null) }
                .onFailure { callback(false, it.message) }
        }
    }
}
