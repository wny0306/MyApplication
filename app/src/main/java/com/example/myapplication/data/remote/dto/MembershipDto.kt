package com.example.myapplication.data.remote.dto

data class MembershipDto(
    val room_id: String,
    val user_id: String,
    val status: String // "joined" / "left"
)
