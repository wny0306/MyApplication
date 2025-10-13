package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.feature.auth.LoginScreen
import com.example.myapplication.feature.auth.SignUpScreen
import com.example.myapplication.feature.common.MainScreen
import com.example.myapplication.feature.home.CreateRoomScreen
import com.example.myapplication.feature.home.HomeScreen
import com.example.myapplication.feature.home.RoomListViewModel
import com.example.myapplication.feature.profile.EditProfileScreen
import com.example.myapplication.feature.profile.ProfileScreen
import com.example.myapplication.feature.roomdetail.RoomDetailScreen


@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    // 建立一次共用的 ViewModel
    val roomListViewModel: RoomListViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    NavHost(navController = navController, startDestination = Routes.Main.path) {
        composable(Routes.Main.path) { MainScreen(navController) }
        composable(Routes.Login.path) { LoginScreen(navController) }
        composable(Routes.SignUp.path) { SignUpScreen(navController) }
        composable(Routes.Home.path) { HomeScreen(navController, roomListViewModel) }
        composable(Routes.CreateRoom.path) { CreateRoomScreen(navController, roomListViewModel) }
        composable(Routes.Profile.path) { ProfileScreen(navController) }
        composable(Routes.EditProfile.path) { EditProfileScreen(navController) }

        composable(
            route = Routes.RoomDetail.path,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId")
            RoomDetailScreen(navController, roomId, roomListViewModel)
        }
    }
}
