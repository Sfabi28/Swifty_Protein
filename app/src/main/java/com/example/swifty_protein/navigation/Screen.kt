package com.example.swifty_protein.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swifty_protein.HomeScreen
import com.example.swifty_protein.LoginScreen

sealed class Screen(val route: String) {     //per navigare tra le schermate possibili route
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController,
        startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(onNavigateToHome = {
                navController.navigate(Screen.Home.route)
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(onBack = {
                navController.popBackStack()
            })
        }

    }
}