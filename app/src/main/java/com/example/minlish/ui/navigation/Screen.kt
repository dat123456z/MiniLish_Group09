package com.example.minlish.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Style
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login          : Screen("login")
    object Register       : Screen("register")
    object DeckList       : Screen("deck_list")
    object Study          : Screen("study")
    object Dashboard      : Screen("dashboard")
    object Profile        : Screen("profile")
    object ForgotPassword  : Screen("forgot_password")
    object VerifyCode      : Screen("verify_code")
    object NewPassword     : Screen("new_password")
    object ResetSuccess    : Screen("reset_success")
    object CreateDeck      : Screen("create_deck")
    object Analytics      : Screen("analytics")
    object EditProfile    : Screen("edit_profile")
    object DailyReminder  : Screen("daily_reminder")

    object ImportExport : Screen("import_export/{deckId}") {
        fun createRoute(deckId: String) = "import_export/$deckId"
    }
    object AddWord : Screen("add_word/{deckId}") {
        fun createRoute(deckId: String) = "add_word/$deckId"
    }
    object Flashcard : Screen("flashcard/{deckId}") {
        fun createRoute(deckId: String) = "flashcard/$deckId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home",      Icons.Filled.Home,     Icons.Outlined.Home),
    BottomNavItem(Screen.DeckList,  "Decks",     Icons.Filled.Style,    Icons.Outlined.Style),
    BottomNavItem(Screen.Analytics, "Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(Screen.Profile,   "Profile",   Icons.Filled.Person,   Icons.Outlined.Person),
)