package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.MahjongRoom
import com.example.myapplication.domain.model.Member
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun roomsFlow(): Flow<List<MahjongRoom>>
    suspend fun getRooms(): List<MahjongRoom>
    suspend fun getRoom(roomId: Int): MahjongRoom?
    suspend fun getRoomMembers(roomId: Int): List<Member>
    suspend fun createRoom(room: MahjongRoom): Boolean
    suspend fun deleteRoom(roomId: Int): Boolean
    suspend fun leaveRoom(roomId: Int, userId: Int): Boolean
    suspend fun isJoined(roomId: Int, userId: Int): Boolean
    suspend fun joinRoom(roomId: Int, userId: Int): Boolean
    fun currentUserId(): Int?
}
