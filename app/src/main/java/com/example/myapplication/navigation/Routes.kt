package com.example.myapplication.navigation

sealed class Routes(val path: String) {
    data object Main : Routes("main")
    data object Login : Routes("login")
    data object SignUp : Routes("signup")
    data object Home : Routes("home")
    data object CreateRoom : Routes("createRoom")
    data object Profile : Routes("profile")
    data object EditProfile : Routes("editProfile")

    // ✅ 改為 Int 型別參數，並明確標註路由格式
    data object RoomDetail : Routes("roomDetail/{roomId}") {
        const val route = "roomDetail/{roomId}"

        // ✅ 以 Int 生成完整路徑
        fun create(roomId: Int): String = "roomDetail/$roomId"
    }
}
