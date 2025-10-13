package com.example.myapplication.data.remote.dto

data class RoomDto(
    val id: String,
    val title: String,
    val owner_id: String,
    val people: Int,
    val flower: Boolean,
    val time: String,
    val location: String
)
