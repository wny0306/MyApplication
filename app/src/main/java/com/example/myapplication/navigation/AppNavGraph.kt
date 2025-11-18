package com.example.myapplication.navigation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.feature.auth.LoginScreen
import com.example.myapplication.feature.auth.SignUpScreen
import com.example.myapplication.feature.auth.SplashScreen
import com.example.myapplication.feature.common.MainScreen
import com.example.myapplication.feature.home.CreateRoomScreen
import com.example.myapplication.feature.home.HomeScreen
import com.example.myapplication.feature.home.RoomListViewModel
import com.example.myapplication.feature.profile.AboutScreen
import com.example.myapplication.feature.profile.EditProfileScreen
import com.example.myapplication.feature.profile.ProfileScreen
import com.example.myapplication.feature.profile.MatchHistoryScreen
import com.example.myapplication.feature.profile.CreateHistoryScreen
import com.example.myapplication.feature.roomdetail.RoomDetailScreen
import com.example.myapplication.feature.profile.ProfileViewModel

// 簡單的 ViewModel Factory，注入 Context
private class RoomListVMFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RoomListViewModel(context) as T
    }
}

@SuppressLint("NewApi")
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // 原本 RoomListViewModel
    val roomListViewModel: RoomListViewModel =
        viewModel(factory = RoomListVMFactory(context))

    // ⭐ 新增這行：共用 ProfileViewModel（非常重要）
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") { SplashScreen(navController) }

        composable(Routes.Login.path) { LoginScreen(navController) }
        composable(Routes.SignUp.path) { SignUpScreen(navController) }

        composable(Routes.Main.path) { MainScreen(navController) }
        composable(Routes.Home.path) { HomeScreen(navController, roomListViewModel) }

        composable(Routes.CreateRoom.path) {
            CreateRoomScreen(navController, roomListViewModel)
        }

        // ⭐ 這兩個畫面共用同一個 ProfileViewModel
        composable(Routes.Profile.path) {
            ProfileScreen(navController, vm = profileViewModel)
        }

        composable(Routes.EditProfile.path) {
            EditProfileScreen(navController, viewModel = profileViewModel)
        }

        composable("matchHistory") { MatchHistoryScreen(navController) }
        composable("createHistory") { CreateHistoryScreen(navController) }
        composable("about") { AboutScreen(navController) }

        composable(
            route = Routes.RoomDetail.route,
            arguments = listOf(navArgument("roomId") { type = NavType.IntType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getInt("roomId")
            RoomDetailScreen(navController, roomId, roomListViewModel)
        }
    }
}

