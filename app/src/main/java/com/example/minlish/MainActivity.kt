package com.example.minlish

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.minlish.notification.InAppNotificationService
import com.example.minlish.notification.ReminderScheduler
import com.example.minlish.ui.navigation.AppNavigation
import com.example.minlish.ui.theme.MinlishTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ReminderScheduler.scheduleFromSavedSettings(this)

        setContent {
            MinlishTheme {
                val inAppNotification by InAppNotificationService.notificationEvents.collectAsState(initial = null)
                var showDialog by remember { mutableStateOf(false) }
                var dialogData by remember { mutableStateOf<Pair<String, String>?>(null) }

                LaunchedEffect(inAppNotification) {
                    inAppNotification?.let {
                        dialogData = it
                        showDialog = true
                    }
                }

                if (showDialog && dialogData != null) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(dialogData!!.first, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                        text = { Text(dialogData!!.second) },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Học ngay", color = com.example.minlish.ui.theme.PrimaryPurple)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Để sau", color = androidx.compose.ui.graphics.Color.Gray)
                            }
                        }
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {}

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
