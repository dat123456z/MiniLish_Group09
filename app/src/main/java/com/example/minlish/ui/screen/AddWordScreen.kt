package com.example.minlish.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.DeckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(navController: NavController, deckId: String, deckViewModel: DeckViewModel) {
    var word by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var collocation by remember { mutableStateOf("") }
    var relatedWords by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var showAdvanced by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val canSave = word.isNotBlank() && meaning.isNotBlank()

    fun resetForm() {
        word = ""
        pronunciation = ""
        meaning = ""
        definition = ""
        example = ""
        collocation = ""
        relatedWords = ""
        note = ""
        showAdvanced = false
    }

    fun save(andAddAnother: Boolean) {
        if (!canSave) return
        deckViewModel.addVocab(
            word = word,
            pronunciation = pronunciation,
            meaning = meaning,
            description = definition,
            example = example,
            collocation = collocation,
            relatedWords = relatedWords,
            note = note,
            deckId = deckId
        )
        if (andAddAnother) {
            resetForm()
            showSnackbar = true
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar("Word saved! Add another.")
            showSnackbar = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add New Word",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1A1C2E))
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            WordField(
                label = "Word *",
                value = word,
                onValueChange = { word = it },
                placeholder = "e.g. Persistent"
            )

            WordField(
                label = "Pronunciation",
                value = pronunciation,
                onValueChange = { pronunciation = it },
                placeholder = "e.g. /pərˈsɪstənt/"
            )

            WordField(
                label = "Meaning *",
                value = meaning,
                onValueChange = { meaning = it },
                placeholder = "Vietnamese meaning"
            )

            WordField(
                label = "Definition",
                value = definition,
                onValueChange = { definition = it },
                placeholder = "English definition",
                minLines = 2,
                maxLines = 4
            )

            WordField(
                label = "Example Sentence",
                value = example,
                onValueChange = { example = it },
                placeholder = "e.g. She was persistent in her efforts to learn.",
                minLines = 2,
                maxLines = 4
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdvanced = !showAdvanced },
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Advanced fields",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF44464F)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Collocation · Related · Note",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBDBFC9)
                        )
                        Icon(
                            if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFFBDBFC9),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showAdvanced,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    WordField(
                        label = "Collocation",
                        value = collocation,
                        onValueChange = { collocation = it },
                        placeholder = "e.g. persistent effort, persistent pain"
                    )
                    WordField(
                        label = "Related Words",
                        value = relatedWords,
                        onValueChange = { relatedWords = it },
                        placeholder = "e.g. persist, persistence, persistently"
                    )
                    WordField(
                        label = "Note",
                        value = note,
                        onValueChange = { note = it },
                        placeholder = "Personal notes or memory tips",
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = { save(andAddAnother = false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                enabled = canSave
            ) {
                Text(
                    "Save Word",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            OutlinedButton(
                onClick = { save(andAddAnother = true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = canSave,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple)
            ) {
                Text(
                    "Save & Add Another",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF44464F)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFBDBFC9)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = minLines,
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = PrimaryPurple.copy(alpha = 0.5f)
            )
        )
    }
}