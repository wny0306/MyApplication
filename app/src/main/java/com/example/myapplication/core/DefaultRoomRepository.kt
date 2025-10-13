package com.example.myapplication.core

import com.example.myapplication.data.repository.RoomRepository
import com.example.myapplication.domain.model.MahjongRoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultRoomRepository : RoomRepository {

    private val rooms = MutableStateFlow(
        listOf(
            MahjongRoom("r1", "測試房 1", "u1", 4, true, "20:00", "台北市"),
            MahjongRoom("r2", "測試房 2", "u2", 3, true, "21:30", "高雄市"),
        )
    )

    override fun roomsFlow(): Flow<List<MahjongRoom>> = rooms

    override suspend fun createRoom(room: MahjongRoom) {
        rooms.value = rooms.value + room
    }

    override suspend fun getRoom(id: String): MahjongRoom? {
        delay(100) // 模擬 IO
        return rooms.value.find { it.id == id }
    }

    override suspend fun deleteRoom(roomId: String): Boolean {
        delay(100)
        rooms.value = rooms.value.filterNot { it.id == roomId }
        return true
    }

    override suspend fun leaveRoom(roomId: String, userId: String): Boolean {
        delay(100)
        return true
    }

    override fun currentUserId(): String? {
        return "u1"  // 或 Supabase 的 auth.currentUserOrNull()?.id
    }

    override suspend fun isJoined(roomId: String, userId: String): Boolean {
        val r = rooms.value.find { it.id == roomId } ?: return false
        return r.ownerId == userId || userId != "visitor"
    }
}
