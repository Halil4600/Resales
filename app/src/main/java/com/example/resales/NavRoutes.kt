package com.example.resales

sealed class NavRoutes(val route: String) {
    data object SalesItemList : NavRoutes("Home")
    data object Login : NavRoutes("Login")
    data object Profile : NavRoutes("Profile")
}