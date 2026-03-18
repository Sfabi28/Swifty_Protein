package com.example.swifty_protein.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swifty_protein.data.DbHelper
import com.example.swifty_protein.data.SessionManager
import com.example.swifty_protein.ui.auth.AuthViewModel
import com.example.swifty_protein.ui.auth.AuthViewModelFactory
import com.example.swifty_protein.ui.screens.HomeScreen
import com.example.swifty_protein.ui.screens.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
}

@Composable
fun AppNavHost(logoutTrigger: MutableState<Boolean>? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dbHelper = remember { DbHelper(context) }
    val sessionManager = remember { SessionManager(context) }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(dbHelper, sessionManager)
    )

    LaunchedEffect(logoutTrigger?.value) {
        if (logoutTrigger?.value == true) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            logoutTrigger.value = false
        }
    }

    val startDestination = if (authViewModel.checkLogin()) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            val currentUser = authViewModel.getLastUser() ?: "User"

            HomeScreen(
                username = currentUser,
                onBack = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
