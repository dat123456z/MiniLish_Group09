package com.example.minlish.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.minlish.R
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minlish.ui.navigation.Screen
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.AuthViewModel
import com.example.minlish.ui.viewmodel.DeckStats
import com.example.minlish.ui.viewmodel.DeckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    deckViewModel: DeckViewModel
) {
    val currentUser by authViewModel.currentUser.observeAsState()
    val deckList by deckViewModel.deckList.observeAsState(emptyList())
    val deckStats by deckViewModel.deckStats.observeAsState(emptyMap())

    val dynamicCategories = remember(deckList) {
        val tags = deckList
            .flatMap { it.tags.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        listOf("All Decks") + tags
    }
    var selectedCategory by remember { mutableStateOf("All Decks") }
    var searchQuery by remember { mutableStateOf("") }
    var deckToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.id) {
        currentUser?.let { deckViewModel.loadDecks(it.id) }
    }

    if (deckToDelete != null) {
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            title = { Text(stringResource(R.string.delete_deck_confirm_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_deck_confirm_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentUser?.let { deckViewModel.deleteDeck(deckToDelete!!, it.id) }
                        deckToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete_deck), color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deckToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier.size(24.dp),
                        tint = PrimaryPurple
                    )
                    Spacer(Modifier.width(12.dp))
                    Image(
                        painter = painterResource(R.drawable.applogo),
                        contentDescription = "MinLish",
                        modifier = Modifier.height(28.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate(Screen.CreateDeck.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Deck", tint = PrimaryPurple)
                    }
                    Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(22.dp))
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = PrimaryPurple
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                currentUser?.name?.firstOrNull()?.toString()?.uppercase() ?: "A",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_decks_placeholder), color = Color(0xFFBDBFC9)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFBDBFC9))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PrimaryPurple.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dynamicCategories) { category ->
                    val label = if (category == "All Decks") stringResource(R.string.all_decks) else category
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(label) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFE8EAF6),
                            labelColor = Color(0xFF44464F)
                        ),
                        border = null
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val filteredDecks = deckList.filter {
                (selectedCategory == "All Decks" || it.tags.contains(selectedCategory, ignoreCase = true)) &&
                        (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true))
            }

            if (filteredDecks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (deckList.isEmpty()) stringResource(R.string.no_decks_yet)
                        else stringResource(R.string.no_matching_decks),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF44464F)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredDecks, key = { it.id }) { deck ->
                        val stats = deckStats[deck.id] ?: DeckStats()
                        var showMenu by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Screen.DeckDetail.createRoute(deck.id)) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = PrimaryPurple.copy(alpha = 0.1f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    deck.name.firstOrNull()?.toString()?.uppercase() ?: "D",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryPurple
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                deck.name,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFE8EAF6)
                                            ) {
                                                Text(
                                                    if (deck.tags.isNotBlank())
                                                        deck.tags.split(",").first().trim()
                                                    else "General",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF44464F)
                                                )
                                            }
                                        }
                                    }

                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = "More options",
                                                tint = Color(0xFFBDBFC9)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.review_deck)) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    navController.navigate(Screen.Flashcard.createRoute(deck.id, reviewOnly = true))
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.manage_words)) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    navController.navigate(Screen.DeckDetail.createRoute(deck.id))
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.add_word)) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    navController.navigate(Screen.AddWord.createRoute(deck.id))
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.delete_deck), color = Color.Red) },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp),
                                                        tint = Color.Red
                                                    )
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    deckToDelete = deck.id
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${stats.learnedWords}/${stats.totalWords} " + stringResource(R.string.words).lowercase(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF44464F)
                                    )
                                    Text(
                                        "${(stats.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryPurple
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { stats.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF4CAF50),
                                    trackColor = Color(0xFFE8EAF6)
                                )

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { navController.navigate(Screen.Flashcard.createRoute(deck.id, reviewOnly = false)) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(stringResource(R.string.study))
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { navController.navigate(Screen.DeckDetail.createRoute(deck.id)) },
                                        modifier = Modifier.height(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE8EAF6)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF44464F))
                                    ) {
                                        Icon(
                                            Icons.Default.List,
                                            contentDescription = "Manage Words",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(stringResource(R.string.words), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}