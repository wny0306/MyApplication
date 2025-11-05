package com.example.myapplication.feature.roomdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.RoomRepository
import com.example.myapplication.domain.model.MahjongRoom
import com.example.myapplication.domain.model.Member
import com.example.myapplication.core.RoomRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 🧩 角色列舉
enum class RoomViewerRole { Owner, Member, Visitor }

class RoomDetailViewModel(
    private val repo: RoomRepository = RoomRepositoryImpl()
) : ViewModel() {

    // 🧩 房間資訊
    private val _room = MutableStateFlow<MahjongRoom?>(null)
    val room: StateFlow<MahjongRoom?> = _room

    // 🧩 房間成員
    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members

    // 🧩 使用者角色
    private val _role = MutableStateFlow(RoomViewerRole.Visitor)
    val role: StateFlow<RoomViewerRole> = _role

    // 🧩 是否已加入
    private val _isJoined = MutableStateFlow(false)
    val isJoined: StateFlow<Boolean> = _isJoined

    /**
     * 🧭 載入房間 & 成員，同時計算角色
     */
    fun loadRoom(roomId: String?) {
        if (roomId == null) return
        viewModelScope.launch {
            val r = repo.getRoom(roomId)
            _room.value = r

            if (r != null) {
                // 先抓所有成員
                val allMembers = repo.getRoomMembers(roomId)
                _members.value = allMembers

                // 取得當前使用者 ID
                val uid = repo.currentUserId()

                if (uid != null) {
                    _role.value = when {
                        uid == r.ownerId -> RoomViewerRole.Owner
                        allMembers.any { it.id == uid } -> RoomViewerRole.Member
                        else -> RoomViewerRole.Visitor
                    }
                    _isJoined.value = (_role.value != RoomViewerRole.Visitor)
                }
            }
        }
    }

    /**
     * 🗑️ 刪除房間
     */
    fun deleteRoom(callback: (Boolean, String?) -> Unit) {
        val id = _room.value?.id ?: return callback(false, "房間不存在")
        viewModelScope.launch {
            runCatching { repo.deleteRoom(id) }
                .onSuccess { ok -> callback(ok, null) }
                .onFailure { callback(false, it.message) }
        }
    }

    /**
     * 🚪 離開房間
     */
    fun leaveRoom(callback: (Boolean, String?) -> Unit) {
        val id = _room.value?.id ?: return callback(false, "房間不存在")
        viewModelScope.launch {
            val uid = repo.currentUserId()
                ?: return@launch callback(false, "未登入")
            runCatching { repo.leaveRoom(id, uid) }
                .onSuccess { ok -> callback(ok, null) }
                .onFailure { callback(false, it.message) }
        }
    }
}
