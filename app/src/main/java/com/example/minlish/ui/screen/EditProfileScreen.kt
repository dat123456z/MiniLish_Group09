package com.example.minlish.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minlish.ui.component.AuthTextField
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val currentUser by authViewModel.currentUser.observeAsState()
    val error by authViewModel.error.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(false)

    var name by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var selectedGoal by remember(currentUser) { mutableStateOf(currentUser?.goal ?: "IELTS") }
    var selectedLevel by remember(currentUser) { mutableStateOf(currentUser?.level ?: "B2") }

    val learningGoals = listOf("IELTS", "TOEIC", "Business", "Travel")
    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")

    var savedSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(savedSuccess, error, isLoading) {
        if (savedSuccess && error == null && !isLoading) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with Edit Button
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = Color(0xFFF0F2FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            name.firstOrNull()?.toString()?.uppercase() ?: "A",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = PrimaryPurple,
                    shadowElevation = 2.dp,
                    onClick = { /* Image Picker */ }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Form Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Name
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Full Name", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    AuthTextField(
                        value = name,
                        onValueChange = { name = it; authViewModel.clearError() },
                        placeholder = "Your name",
                        leadingIcon = Icons.Outlined.Person
                    )
                }

                // Email
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Email Address", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; authViewModel.clearError() },
                        placeholder = "Your email",
                        leadingIcon = Icons.Outlined.Email
                    )
                }

                // Learning Goal
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Learning goal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        learningGoals.forEach { goal ->
                            val isSelected = selectedGoal.equals(goal, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clickable { selectedGoal = goal },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) PrimaryPurple else Color(0xFFF0F2FF),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        goal,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else PrimaryPurple
                                    )
                                }
                            }
                        }
                    }
                }

                // Current Level
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Current level", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        levels.forEach { level ->
                            val isSelected = selectedLevel == level
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable { selectedLevel = level },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryPurple else Color.White,
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE8EAF6)),
                                shadowElevation = if (isSelected) 4.dp else 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        level,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    savedSuccess = true
                    authViewModel.updateUser(name, email, selectedGoal, selectedLevel)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                enabled = !isLoading && name.isNotBlank() && email.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
