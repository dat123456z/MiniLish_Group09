package com.example.minlish.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.minlish.R
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.AuthViewModel
import com.example.minlish.ui.viewmodel.DeckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeckScreen(
    navController: NavController, 
    authViewModel: AuthViewModel,
    deckViewModel: DeckViewModel
) {
    val currentUser by authViewModel.currentUser.observeAsState()

    var deckName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    
    var newWordsPerDay by remember { mutableStateOf(10) }
    var reviewsPerDay by remember { mutableStateOf(40) }
    
    var selectedColor by remember { mutableStateOf(PrimaryPurple) }
    val availableTags = listOf("IELTS", "Academic", "Business", "Travel")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_deck_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel), tint = Color(0xFF1A1C2E))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (deckName.isNotBlank() && currentUser != null) {
                                deckViewModel.addDeck(deckName, description, tags, currentUser!!.id, newWordsPerDay, reviewsPerDay)
                                navController.popBackStack()
                            }
                        },
                        enabled = deckName.isNotBlank() && currentUser != null
                    ) {
                        Text(
                            stringResource(R.string.save_changes),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (deckName.isNotBlank() && currentUser != null) PrimaryPurple else Color(0xFFBDBFC9)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FF))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FF))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = selectedColor
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    deckName.firstOrNull()?.toString()?.uppercase() ?: "A",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                        Column {
                            Text(stringResource(R.string.cover_label), style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF1A1C2E), fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.tap_to_choose_cover),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF44464F))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.deck_name_label), style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF44464F), fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    placeholder = { Text(stringResource(R.string.deck_name_placeholder), color = Color(0xFFBDBFC9)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = PrimaryPurple.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.description_optional), style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF44464F), fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text(stringResource(R.string.description_placeholder), color = Color(0xFFBDBFC9)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = PrimaryPurple.copy(alpha = 0.5f)
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.tags_label), style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF44464F), fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableTags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (tags.contains(tag)) PrimaryPurple else Color(0xFFE8EAF6),
                            modifier = Modifier.clickable { 
                                tags = if (tags.contains(tag)) {
                                    tags.split(",").filter { it != tag }.joinToString(",")
                                } else {
                                    if (tags.isBlank()) tag else "$tags,$tag"
                                }
                            }
                        ) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (tags.contains(tag)) Color.White else Color(0xFF44464F)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color(0xFFE8EAF6))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text(stringResource(R.string.add_tag), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.daily_learning_plan), style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF44464F), fontWeight = FontWeight.SemiBold)

                PlanCounter(stringResource(R.string.new_words_per_day), newWordsPerDay) { newWordsPerDay = it }
                PlanCounter(stringResource(R.string.rev_words_per_day), reviewsPerDay) { reviewsPerDay = it }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (deckName.isNotBlank() && currentUser != null) {
                        deckViewModel.addDeck(deckName, description, tags, currentUser!!.id, newWordsPerDay, reviewsPerDay)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                enabled = deckName.isNotBlank() && currentUser != null
            ) {
                Text(
                    stringResource(R.string.create_deck_btn),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlanCounter(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8F9FF),
                    onClick = { if (value > 1) onValueChange(value - 1) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("−", fontSize = 18.sp, color = Color(0xFF1A1C2E))
                    }
                }

                Text(
                    "$value",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8F9FF),
                    onClick = { onValueChange(value + 1) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", fontSize = 18.sp, color = Color(0xFF1A1C2E))
                    }
                }
            }
        }
    }
}
