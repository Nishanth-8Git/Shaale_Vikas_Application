package com.example.shaalevikas.ui.navigation

/**
 * Sealed class representing different screens in the application for navigation purposes.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
    object AddNeed : Screen("add_need")
    object HallOfFame : Screen("hall_of_fame")
}
