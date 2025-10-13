package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.MahjongRoom
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun getRoom(id: String): MahjongRoom?
    suspend fun deleteRoom(roomId: String): Boolean
    suspend fun leaveRoom(roomId: String, userId: String): Boolean
    fun currentUserId(): String?   // 改成非 suspend
    suspend fun isJoined(roomId: String, userId: String): Boolean
    fun roomsFlow(): Flow<List<MahjongRoom>>
    suspend fun createRoom(room: MahjongRoom)
}
