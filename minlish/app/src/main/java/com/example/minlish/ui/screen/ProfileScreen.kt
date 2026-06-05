package com.example.minlish.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.minlish.R
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.ui.navigation.Screen
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.AuthViewModel
import com.example.minlish.ui.viewmodel.DashboardViewModel
import com.example.minlish.util.LocaleHelper
import com.example.minlish.util.SessionManager

import androidx.compose.ui.res.stringResource

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
    val currentLang = SessionManager.getLanguage(context)
    val reminderEnabled = SessionManager.isReminderEnabled(context)
    val appTime = formatTime(SessionManager.getReminderHour(context), SessionManager.getReminderMinute(context))

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
                    Image(
                        painter = painterResource(R.drawable.applogo),
                        contentDescription = "MinLish",
                        modifier = Modifier.height(28.dp)
                    )
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
                                val goalLabel = when(goal.lowercase()) {
                                    "ielts" -> stringResource(R.string.goal_ielts)
                                    "toeic" -> stringResource(R.string.goal_toeic)
                                    "business" -> stringResource(R.string.goal_business)
                                    "travel" -> stringResource(R.string.goal_travel)
                                    else -> goal
                                }
                                BadgeTag(goalLabel, Color(0xFFE8EAF6), Color(0xFF4F6EF7))
                            }
                            val level = currentUser?.level
                            if (!level.isNullOrBlank()) {
                                BadgeTag(stringResource(R.string.level_label, level), Color(0xFFE8F5E9), Color(0xFF2E7D32))
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
                            label = stringResource(R.string.streak_days_label),
                            value = "${stats?.streak ?: currentUser?.streak ?: 0}",
                            icon = "🔥"
                        )
                        VerticalDivider(
                            modifier = Modifier.height(32.dp).padding(horizontal = 8.dp),
                            color = Color(0xFFE8EAF6)
                        )
                        ProfileStatItem(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.words_learned_label),
                            value = if (stats != null) "${stats!!.learnedWords}" else "--",
                            icon = null
                        )
                        VerticalDivider(
                            modifier = Modifier.height(32.dp).padding(horizontal = 8.dp),
                            color = Color(0xFFE8EAF6)
                        )
                        ProfileStatItem(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.decks_active_label),
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
                    ProfileMenuItem(icon = Icons.Default.Person, label = stringResource(R.string.edit_profile), onClick = {
                        navController.navigate(Screen.EditProfile.route)
                    })

                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        label = stringResource(R.string.daily_reminders),
                        subLabel = if (reminderEnabled) appTime else "Off",
                        onClick = { navController.navigate(Screen.DailyReminder.route) }
                    )
                    ProfileMenuItem(icon = Icons.Default.ImportExport, label = stringResource(R.string.import_export), onClick = {
                        val firstDeckId = "all"
                        navController.navigate(Screen.ImportExport.createRoute(firstDeckId))
                    })

                    ProfileMenuItem(
                        icon = Icons.Default.Language,
                        label = stringResource(R.string.language),
                        subLabel = if (currentLang == "vi") "Tiếng Việt" else "English",
                        onClick = {
                            val newLang = if (currentLang == "en") "vi" else "en"
                            SessionManager.saveLanguage(context, newLang)
                            LocaleHelper.updateResources(context, newLang)
                            // Restart activity to apply changes
                            LocaleHelper.findActivity(context)?.recreate()
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
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
                        Text(stringResource(R.string.sign_out), color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.made_by_group), fontSize = 12.sp, color = Color.LightGray)
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