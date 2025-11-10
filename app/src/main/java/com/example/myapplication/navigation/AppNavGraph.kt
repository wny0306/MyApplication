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

// 簡單的 ViewModel Factory，注入 Context
private class RoomListVMFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RoomListViewModel(context) as T
    }
}

@SuppressLint("NewApi") // 屏蔽因 @RequiresApi 造成的呼叫警告（LoginScreen/HomeScreen）
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val roomListViewModel: RoomListViewModel =
        viewModel(factory = RoomListVMFactory(context))

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") { SplashScreen(navController) }

        composable(Routes.Login.path) { LoginScreen(navController) }
        composable(Routes.SignUp.path) { SignUpScreen(navController) }
        composable(Routes.Main.path) { MainScreen(navController) }
        composable(Routes.Home.path) { HomeScreen(navController, roomListViewModel) }
        composable(Routes.CreateRoom.path) { CreateRoomScreen(navController, roomListViewModel) }
        composable(Routes.Profile.path) { ProfileScreen(navController) }
        composable(Routes.EditProfile.path) { EditProfileScreen(navController) }
        composable("matchHistory") { MatchHistoryScreen(navController) }
        composable("createHistory") { CreateHistoryScreen(navController) }
        composable("about") { AboutScreen(navController) }

        // ✅ RoomDetail 改為 Int 參數
        composable(
            route = Routes.RoomDetail.route, // "roomDetail/{roomId}"
            arguments = listOf(navArgument("roomId") { type = NavType.IntType })
        ) { backStackEntry ->
            val roomId: Int? = backStackEntry.arguments?.getInt("roomId")
            RoomDetailScreen(navController, roomId, roomListViewModel)
        }
    }
}
