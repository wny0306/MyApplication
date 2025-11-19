package com.example.myapplication.domain.model

data class MahjongRoom(
    val id: Int,
    val ownerId: Int,
    val ownerName: String? = null,
    val avatarUrl: String? = null,
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

data class RoomDto(
    val id: Int,
    val owner_id: Int,
    val owner_name: String? = null,
    val avatar_url: String? = null,
    val people: Int,
    val flower: Boolean,
    val date: String,
    val time: String,
    val city: String,
    val location: String,
    val rounds: Int,
    val dice_rule: Boolean,
    val ligu: Boolean,
    val base_point: Int,
    val tai_point: Int,
    val note: String? = null,
    val members: List<Member>? = null,
    val member_count: Int = 0,
    val created_at: String? = null,
    val updated_at: String? = null
)


// ----------------------
// DTO → Domain
// ----------------------
fun RoomDto.toDomain() = MahjongRoom(
    id = id,
    ownerId = owner_id,
    ownerName = owner_name,
    avatarUrl = avatar_url,
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
    createdAt = created_at,
    updatedAt = updated_at,
    members = members ?: emptyList(),
    memberCount = member_count
)


// ----------------------
// Domain → DTO
// ----------------------
fun MahjongRoom.toDto() = RoomDto(
    id = id,
    owner_id = ownerId,
    owner_name = ownerName,
    avatar_url = avatarUrl,
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
    members = members,
    member_count = memberCount,
    created_at = createdAt,
    updated_at = updatedAt
)
