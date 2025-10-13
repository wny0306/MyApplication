package com.example.myapplication.domain.model

import java.util.UUID
import com.example.myapplication.data.remote.dto.RoomDto

data class MahjongRoom(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val ownerId: String,
    val people: Int,
    val flower: Boolean,
    val time: String,
    val location: String
)

// DTO → Domain
fun RoomDto.toDomain() = MahjongRoom(
    id = id,
    title = title,
    ownerId = owner_id,
    people = people,
    flower = flower,
    time = time,
    location = location
)

// Domain → DTO
fun MahjongRoom.toDto() = RoomDto(
    id = id,
    title = title,
    owner_id = ownerId,
    people = people,
    flower = flower,
    time = time,
    location = location
)