package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.MahjongRoom
import com.example.myapplication.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun roomsFlow(): Flow<List<MahjongRoom>>
    suspend fun getRooms(): List<MahjongRoom>
    suspend fun createRoom(room: MahjongRoom): Boolean
    suspend fun getRoom(roomId: String): MahjongRoom?
    suspend fun deleteRoom(roomId: String): Boolean
    suspend fun leaveRoom(roomId: String, userId: String): Boolean
    suspend fun isJoined(roomId: String, userId: String): Boolean
    suspend fun getRoomMembers(roomId: String): List<Member>
    suspend fun joinRoom(roomId: String, userId: String): Boolean
    fun currentUserId(): String?
}
