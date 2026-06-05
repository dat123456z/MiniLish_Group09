package com.example.minlish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minlish.R
import com.example.minlish.model.Vocabulary
import com.example.minlish.ui.navigation.Screen
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.DeckViewModel

import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    navController: NavController,
    deckId: String,
    deckViewModel: DeckViewModel
) {
    val vocabList by deckViewModel.vocabList.observeAsState(emptyList())
    val deckList by deckViewModel.deckList.observeAsState(emptyList())
    val currentDeck = remember(deckList) { deckList.find { it.id == deckId } }

    var wordToDelete by remember { mutableStateOf<Vocabulary?>(null) }

    LaunchedEffect(deckId) {
        deckViewModel.loadVocabs(deckId)
    }

    if (wordToDelete != null) {
        AlertDialog(
            onDismissRequest = { wordToDelete = null },
            title = { Text(stringResource(R.string.delete_word), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.confirm_delete_word, wordToDelete?.word ?: "")) },
            confirmButton = {
                TextButton(onClick = {
                    deckViewModel.deleteVocab(wordToDelete!!.id, deckId)
                    wordToDelete = null
                }) {
                    Text(stringResource(R.string.delete_deck).split(" ").first(), color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { wordToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentDeck?.name ?: stringResource(R.string.deck_details), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddWord.createRoute(deckId)) },
                containerColor = PrimaryPurple
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add word", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF8F9FF)
    ) { padding ->
        if (vocabList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_words_yet), color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vocabList) { vocab ->
                    WordItem(
                        vocab = vocab,
                        onEdit = { navController.navigate(Screen.EditWord.createRoute(deckId, vocab.id)) },
                        onDelete = { wordToDelete = vocab }
                    )
                }
            }
        }
    }
}

@Composable
fun WordItem(vocab: Vocabulary, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(vocab.word, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A1C2E))
                Text(vocab.meaning, fontSize = 14.sp, color = Color(0xFF44464F))
                if (vocab.pronunciation.isNotBlank()) {
                    Text(vocab.pronunciation, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF44464F), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
