package com.example.myapplication.navigation

sealed class Routes(val path: String) {
    data object Main : Routes("main")
    data object Login : Routes("login")
    data object SignUp : Routes("signup")
    data object Home : Routes("home")
    data object CreateRoom : Routes("createRoom")
    data object Profile : Routes("profile")
    data object EditProfile : Routes("editProfile")
    data object RoomDetail {
        const val route = "roomDetail/{roomId}"
        fun create(roomId: String) = "roomDetail/$roomId"
    }
}