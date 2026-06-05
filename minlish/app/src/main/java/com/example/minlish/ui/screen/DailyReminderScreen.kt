package com.example.minlish.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
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

import androidx.compose.ui.res.stringResource
import com.example.minlish.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReminderScreen(navController: NavController) {
    val context = LocalContext.current

    var appEnabled by remember { mutableStateOf(SessionManager.isReminderEnabled(context)) }
    var appHour by remember { mutableStateOf(SessionManager.getReminderHour(context)) }
    var appMinute by remember { mutableStateOf(SessionManager.getReminderMinute(context)) }

    var emailEnabled by remember { mutableStateOf(SessionManager.isEmailReminderEnabled(context)) }
    var emailHour by remember { mutableStateOf(SessionManager.getEmailReminderHour(context)) }
    var emailMinute by remember { mutableStateOf(SessionManager.getEmailReminderMinute(context)) }

    var showAppTimePicker by remember { mutableStateOf(false) }
    var showEmailTimePicker by remember { mutableStateOf(false) }

    val appTimeLabel = formatTime(appHour, appMinute)
    val emailTimeLabel = formatTime(emailHour, emailMinute)

    if (showAppTimePicker) {
        TimePickerDialog(
            initialHour = appHour,
            initialMinute = appMinute,
            onDismiss = { showAppTimePicker = false },
            onConfirm = { h, m -> appHour = h; appMinute = m; showAppTimePicker = false }
        )
    }

    if (showEmailTimePicker) {
        TimePickerDialog(
            initialHour = emailHour,
            initialMinute = emailMinute,
            onDismiss = { showEmailTimePicker = false },
            onConfirm = { h, m -> emailHour = h; emailMinute = m; showEmailTimePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.automation_settings), fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryPurple),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Column {
                        Text(stringResource(R.string.smart_automation), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(stringResource(R.string.automation_desc), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }

            ReminderCard(
                title = stringResource(R.string.app_notifications),
                subtitle = if (appEnabled) stringResource(R.string.active_device) else stringResource(R.string.disabled),
                icon = Icons.Default.NotificationsActive,
                iconBg = Color(0xFFF0F2FF),
                iconTint = PrimaryPurple,
                enabled = appEnabled,
                timeLabel = appTimeLabel,
                onEnabledChange = { appEnabled = it },
                onTimeClick = { showAppTimePicker = true }
            )

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
                                color = Color(0xFFE8F5E9)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF2E7D32))
                                }
                            }
                            Column {
                                Text(stringResource(R.string.automated_email), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(if (emailEnabled) stringResource(R.string.sync_account) else stringResource(R.string.disabled), fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = emailEnabled,
                            onCheckedChange = { emailEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2E7D32)
                            )
                        )
                    }

                    if (emailEnabled) {
                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFFF0F2FF))
                        Spacer(Modifier.height(24.dp))

                        Text(stringResource(R.string.email_reminder_time), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF8F9FF),
                            onClick = { showEmailTimePicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(emailTimeLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text(stringResource(R.string.change), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            val successMsg = stringResource(R.string.automation_sync_toast)
            Button(
                onClick = {
                    SessionManager.saveReminderSettings(context, appEnabled, appHour, appMinute)
                    SessionManager.saveEmailReminderSettings(context, emailEnabled, emailHour, emailMinute)

                    ReminderScheduler.scheduleFromSavedSettings(context)

                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(stringResource(R.string.update_automation), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    enabled: Boolean,
    timeLabel: String,
    onEnabledChange: (Boolean) -> Unit,
    onTimeClick: () -> Unit
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
                        color = iconBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = iconTint)
                        }
                    }
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(subtitle, fontSize = 13.sp, color = Color.Gray)
                    }
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = iconTint
                    )
                )
            }

            if (enabled) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFF0F2FF))
                Spacer(Modifier.height(24.dp))

                Text(stringResource(R.string.reminder_time), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8F9FF),
                    onClick = onTimeClick
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(timeLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = iconTint)
                        Text(stringResource(R.string.change), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = iconTint)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("OK", color = PrimaryPurple, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        },
        text = {
            TimePicker(
                state = state,
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

fun formatTime(hour: Int, minute: Int): String {
    val h = if (hour % 12 == 0) 12 else hour % 12
    val m = minute.toString().padStart(2, '0')
    val period = if (hour < 12) "AM" else "PM"
    return "$h:$m $period"
}