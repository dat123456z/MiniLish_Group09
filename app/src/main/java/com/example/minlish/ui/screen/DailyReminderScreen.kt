package com.example.minlish.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minlish.notification.ReminderScheduler
import com.example.minlish.ui.theme.BackgroundGray
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReminderScreen(navController: NavController) {
    val context = LocalContext.current

    var isEnabled by remember { mutableStateOf(SessionManager.isReminderEnabled(context)) }
    var hour by remember { mutableStateOf(SessionManager.getReminderHour(context)) }
    var minute by remember { mutableStateOf(SessionManager.getReminderMinute(context)) }
    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = false
    )

    val timeLabel = remember(hour, minute) {
        val h = if (hour % 12 == 0) 12 else hour % 12
        val m = minute.toString().padStart(2, '0')
        val period = if (hour < 12) "AM" else "PM"
        "$h:$m $period"
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = timePickerState.hour
                    minute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("OK", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF0F2FF),
                        selectorColor = PrimaryPurple,
                        containerColor = Color.White,
                        periodSelectorSelectedContainerColor = PrimaryPurple,
                        periodSelectorSelectedContentColor = Color.White
                    )
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0F2FF)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = PrimaryPurple
                                    )
                                }
                            }
                            Column {
                                Text("Study Reminder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    if (isEnabled) "Reminder is on" else "Reminder is off",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple
                            )
                        )
                    }

                    if (isEnabled) {
                        Spacer(Modifier.height(32.dp))
                        HorizontalDivider(color = Color(0xFFF0F2FF))
                        Spacer(Modifier.height(24.dp))

                        Text(
                            "Reminder time",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF8F9FF),
                            onClick = { showTimePicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    timeLabel,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                                Text(
                                    "Change",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    SessionManager.saveReminderSettings(context, isEnabled, hour, minute)
                    if (isEnabled) {
                        ReminderScheduler.scheduleDailyReminder(context, hour, minute)
                        ReminderScheduler.scheduleDueReminder(context, 9, 0)
                        Toast.makeText(context, "Reminder set for $timeLabel", Toast.LENGTH_SHORT).show()
                    } else {
                        ReminderScheduler.cancelAll(context)
                        Toast.makeText(context, "Reminder cancelled", Toast.LENGTH_SHORT).show()
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Set Reminder", fontWeight = FontWeight.Bold)
            }
        }
    }
}