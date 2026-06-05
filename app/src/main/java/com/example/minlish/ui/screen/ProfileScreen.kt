package com.example.minlish.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.ui.navigation.Screen
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.AuthViewModel
import com.example.minlish.ui.viewmodel.DashboardViewModel
import com.example.minlish.util.SessionManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.observeAsState()
    val stats by dashboardViewModel.stats.observeAsState()

    LaunchedEffect(currentUser?.id) {
        currentUser?.let { dashboardViewModel.load(it.id) }
    }

    val context = LocalContext.current
    // Read values directly to ensure they are fresh when the screen is recomposed (e.g. after returning from DailyReminderScreen)
    val reminderEnabled = SessionManager.isReminderEnabled(context)
    val reminderHour = SessionManager.getReminderHour(context)
    val reminderMinute = SessionManager.getReminderMinute(context)

    val reminderLabel = remember(reminderEnabled, reminderHour, reminderMinute) {
        if (!reminderEnabled) "Off"
        else {
            val h = if (reminderHour % 12 == 0) 12 else reminderHour % 12
            val m = reminderMinute.toString().padStart(2, '0')
            val period = if (reminderHour < 12) "AM" else "PM"
            "$h:$m $period"
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FF)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null, tint = PrimaryPurple)
                    Text("MinLish", fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 20.sp)
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = PrimaryPurple) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                currentUser?.name?.firstOrNull()?.toString()?.uppercase() ?: "A",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column {
                        Text(currentUser?.name ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(currentUser?.email ?: "", color = Color.Gray, fontSize = 14.sp)
                        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val goal = currentUser?.goal
                            if (!goal.isNullOrBlank()) {
                                BadgeTag(goal, Color(0xFFE8EAF6), Color(0xFF4F6EF7))
                            }
                            val level = currentUser?.level
                            if (!level.isNullOrBlank()) {
                                BadgeTag("Level $level", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStatItem(
                            modifier = Modifier.weight(1f),
                            label = "Streak days",
                            value = "${stats?.streak ?: currentUser?.streak ?: 0}",
                            icon = "🔥"
                        )
                        VerticalDivider(
                            modifier = Modifier.height(32.dp).padding(horizontal = 8.dp),
                            color = Color(0xFFE8EAF6)
                        )
                        ProfileStatItem(
                            modifier = Modifier.weight(1f),
                            label = "Words learned",
                            value = if (stats != null) "${stats!!.learnedWords}" else "--",
                            icon = null
                        )
                        VerticalDivider(
                            modifier = Modifier.height(32.dp).padding(horizontal = 8.dp),
                            color = Color(0xFFE8EAF6)
                        )
                        ProfileStatItem(
                            modifier = Modifier.weight(1f),
                            label = "Decks active",
                            value = if (stats != null) "${stats!!.totalDecks}" else "--",
                            icon = null
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProfileMenuItem(icon = Icons.Default.Person, label = "Edit profile", onClick = {
                        navController.navigate(Screen.EditProfile.route)
                    })

                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        label = "Daily reminders",
                        subLabel = reminderLabel,
                        onClick = { navController.navigate(Screen.DailyReminder.route) }
                    )
                    ProfileMenuItem(icon = Icons.Default.ImportExport, label = "Import / Export", onClick = {
                        navController.navigate(Screen.ImportExport.route)
                    })

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { authViewModel.logout() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Sign out", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("MinLish v1.0.0 · Made with ❤", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun BadgeTag(text: String, bg: Color, fg: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            color = fg,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileStatItem(modifier: Modifier, label: String, value: String, icon: String?) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            if (icon != null) Text(icon, fontSize = 14.sp)
        }
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, subLabel: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1A1C2E), modifier = Modifier.size(22.dp))
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (subLabel != null) Text(subLabel, fontSize = 13.sp, color = Color.LightGray)
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}