package com.example.minlish.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.minlish.ui.navigation.Screen
import com.example.minlish.ui.navigation.bottomNavItems
import com.example.minlish.ui.theme.PrimaryPurple

import androidx.compose.ui.res.stringResource

@Composable
fun AppBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = label,
                        tint = if (selected) PrimaryPurple else Color.LightGray
                    )
                },
                label = { 
                    Text(
                        label,
                        color = if (selected) PrimaryPurple else Color.LightGray
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFF0F2FF)
                )
            )
        }
    }
}
