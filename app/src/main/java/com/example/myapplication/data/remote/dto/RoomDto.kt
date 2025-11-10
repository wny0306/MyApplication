package com.example.myapplication.data.remote.dto


import com.example.myapplication.domain.model.Member

data class RoomDto(
    val id: Int,
    val owner_id: Int,          // ← String → Int
    val owner_name: String? = null,
    val intro: String? = null,
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
    val created_at: String? = null,
    val updated_at: String? = null
)

