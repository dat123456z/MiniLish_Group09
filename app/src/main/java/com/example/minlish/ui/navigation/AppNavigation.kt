package com.example.minlish.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.minlish.ui.components.AppBottomBar
import com.example.minlish.ui.screen.*
import com.example.minlish.ui.viewmodel.AuthViewModel
import com.example.minlish.ui.viewmodel.DashboardViewModel
import com.example.minlish.ui.viewmodel.DeckViewModel
import com.example.minlish.ui.viewmodel.FlashcardViewModel

private val bottomBarRoutes = setOf(
    Screen.DeckList.route,
    Screen.Analytics.route,
    Screen.Dashboard.route,
    Screen.Profile.route
)

@Composable
fun AppNavigation(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val deckViewModel: DeckViewModel = viewModel()
    val flashcardViewModel: FlashcardViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()

    val currentUser by authViewModel.currentUser.observeAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            if (currentRoute == Screen.Login.route) {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateToVerifyCode = { navController.navigate(Screen.VerifyCode.route) },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.VerifyCode.route) {
                VerifyCodeScreen(
                    onNavigateToNewPassword = { navController.navigate(Screen.NewPassword.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.NewPassword.route) {
                NewPasswordScreen(
                    onNavigateToResetSuccess = {
                        navController.navigate(Screen.ResetSuccess.route) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ResetSuccess.route) {
                ResetSuccessScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.CreateDeck.route) {
                CreateDeckScreen(navController, authViewModel, deckViewModel)
            }
            composable(Screen.DeckList.route) {
                DeckListScreen(navController, authViewModel, deckViewModel)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(authViewModel, dashboardViewModel)
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController, authViewModel, deckViewModel, dashboardViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController, authViewModel, dashboardViewModel)
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(navController, authViewModel)
            }
            composable(Screen.DailyReminder.route) {
                DailyReminderScreen(navController)
            }
            composable(
                route = Screen.ImportExport.route,
                arguments = listOf(navArgument("deckId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
                ImportExportScreen(navController, deckId, currentUser?.id ?: "", deckViewModel)
            }
            composable(
                route = Screen.AddWord.route,
                arguments = listOf(navArgument("deckId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
                AddWordScreen(navController, deckId, deckViewModel)
            }
            composable(
                route = Screen.Flashcard.route,
                arguments = listOf(navArgument("deckId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
                FlashcardScreen(navController, deckId, currentUser?.id ?: "", flashcardViewModel)
            }
        }
    }
}