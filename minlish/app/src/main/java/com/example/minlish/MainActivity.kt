package com.example.minlish

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
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
import com.example.minlish.notification.InAppNotification
import com.example.minlish.notification.InAppNotificationService
import com.example.minlish.notification.ReminderScheduler
import com.example.minlish.ui.navigation.AppNavigation
import com.example.minlish.ui.navigation.Screen
import com.example.minlish.ui.theme.MinlishTheme
import com.example.minlish.util.LocaleHelper
import com.example.minlish.util.SessionManager

class MainActivity : ComponentActivity() {
    
    // State to track the latest deckId from intent
    private val pendingDeckId = mutableStateOf<String?>(null)
    private val pendingDueOnly = mutableStateOf(false)

    override fun attachBaseContext(newBase: android.content.Context) {
        val lang = SessionManager.getLanguage(newBase)
        val context = LocaleHelper.applyLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lang = SessionManager.getLanguage(this)
        LocaleHelper.updateResources(this, lang)

        ReminderScheduler.scheduleFromSavedSettings(this)
        
        // Check initial intent
        handleIntent(intent)

        setContent {
            MinlishTheme {
                val inAppNotification by InAppNotificationService.notificationEvents.collectAsState(initial = null)
                var showDialog by remember { mutableStateOf(false) }
                var dialogData by remember { mutableStateOf<InAppNotification?>(null) }
                
                val navController = rememberNavController()

                LaunchedEffect(inAppNotification) {
                    inAppNotification?.let {
                        dialogData = it
                        showDialog = true
                    }
                }

                if (showDialog && dialogData != null) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(dialogData!!.title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                        text = { Text(dialogData!!.body) },
                        confirmButton = {
                            TextButton(onClick = { 
                                showDialog = false
                                dialogData!!.deckId?.let { id ->
                                    // Notifications use SM2 logic (dueOnly = true)
                                    navController.navigate(Screen.Flashcard.createRoute(id, reviewOnly = false, dueOnly = true))
                                }
                            }) {
                                Text(getString(R.string.study_now), color = com.example.minlish.ui.theme.PrimaryPurple)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text(getString(R.string.maybe_later), color = androidx.compose.ui.graphics.Color.Gray)
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

                // Listen for deckId changes and navigate
                val deckIdToNavigate by pendingDeckId
                val dueOnlyToNavigate by pendingDueOnly
                LaunchedEffect(deckIdToNavigate, dueOnlyToNavigate) {
                    deckIdToNavigate?.let { id ->
                        navController.navigate(Screen.Flashcard.createRoute(id, reviewOnly = false, dueOnly = dueOnlyToNavigate))
                        pendingDeckId.value = null // Clear after navigation
                        pendingDueOnly.value = false
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra("deckId")?.let { id ->
            pendingDeckId.value = id
            pendingDueOnly.value = intent.getBooleanExtra("dueOnly", false)
        }
    }
}
