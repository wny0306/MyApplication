package com.example.myapplication.domain.model

import com.example.myapplication.data.remote.dto.RoomDto

data class MahjongRoom(
    val id: Int,
    val ownerId: Int,
    val ownerName: String? = null,
    val people: Int,
    val flower: Boolean,
    val date: String,
    val time: String,
    val city: String,
    val location: String,
    val rounds: Int,
    val diceRule: Boolean,
    val ligu: Boolean,
    val basePoint: Int,
    val taiPoint: Int,
    val note: String? = "",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val members: List<Member> = emptyList(),
    val memberCount: Int = 0
)


// ✅ DTO → Domain
fun RoomDto.toDomain() = MahjongRoom(
    id = id,
    ownerId = owner_id,
    ownerName = owner_name,
    people = people,
    flower = flower,
    date = date,
    time = time,
    city = city,
    location = location,
    rounds = rounds,
    diceRule = dice_rule,
    ligu = ligu,
    basePoint = base_point,
    taiPoint = tai_point,
    note = note,
    members = members ?: emptyList(),
    createdAt = created_at,
    updatedAt = updated_at
)
// Domain → DTO
fun MahjongRoom.toDto() = RoomDto(
    id = id,
    owner_id = ownerId,
    people = people,
    flower = flower,
    date = date,
    time = time,
    city = city,
    location = location,
    rounds = rounds,
    dice_rule = diceRule,
    ligu = ligu,
    base_point = basePoint,
    tai_point = taiPoint,
    note = note,
    created_at = createdAt,
    updated_at = updatedAt
)

