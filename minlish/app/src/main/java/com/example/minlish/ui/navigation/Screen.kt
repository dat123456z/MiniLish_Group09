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
    object VerifyCode      : Screen("verify_code/{email}") {
        fun createRoute(email: String) = "verify_code/$email"
    }
    object NewPassword     : Screen("new_password/{email}") {
        fun createRoute(email: String) = "new_password/$email"
    }
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
    object EditWord : Screen("edit_word/{deckId}/{wordId}") {
        fun createRoute(deckId: String, wordId: String) = "edit_word/$deckId/$wordId"
    }
    object Flashcard : Screen("flashcard/{deckId}/{reviewOnly}/{dueOnly}") {
        fun createRoute(deckId: String, reviewOnly: Boolean = false, dueOnly: Boolean = false) = 
            "flashcard/$deckId/$reviewOnly/$dueOnly"
    }
    object DeckDetail : Screen("deck_detail/{deckId}") {
        fun createRoute(deckId: String) = "deck_detail/$deckId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, com.example.minlish.R.string.nav_home,      Icons.Filled.Home,     Icons.Outlined.Home),
    BottomNavItem(Screen.DeckList,  com.example.minlish.R.string.nav_decks,     Icons.Filled.Style,    Icons.Outlined.Style),
    BottomNavItem(Screen.Analytics, com.example.minlish.R.string.nav_analytics, Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(Screen.Profile,   com.example.minlish.R.string.nav_profile,   Icons.Filled.Person,   Icons.Outlined.Person),
)