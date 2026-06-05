package com.example.minlish.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.SaveAlt
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
import androidx.navigation.NavController
import com.example.minlish.R
import com.example.minlish.model.Deck
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.DeckViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    navController: NavController,
    deckId: String,
    ownerId: String,
    deckViewModel: DeckViewModel
) {
    val importResult by deckViewModel.importResult.observeAsState()
    val isLoading by deckViewModel.isLoading.observeAsState(false)
    val deckList by deckViewModel.deckList.observeAsState(emptyList())

    var createNewDeck by remember { mutableStateOf(false) }
    var newDeckName by remember { mutableStateOf("") }
    var newDeckDescription by remember { mutableStateOf("") }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    var selectedExportDeck by remember { mutableStateOf<Deck?>(null) }
    var exportDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(ownerId) {
        deckViewModel.loadDecks(ownerId)
    }

    LaunchedEffect(deckList) {
        if (selectedExportDeck == null) {
            selectedExportDeck = deckList.firstOrNull { it.id == deckId } ?: deckList.firstOrNull()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (createNewDeck) {
                pendingUri = uri
                showConfirmDialog = true
            } else {
                deckViewModel.importCsv(uri, deckId)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            val targetDeckId = selectedExportDeck?.id ?: deckId
            deckViewModel.exportCsv(uri, targetDeckId)
        }
    }

    LaunchedEffect(importResult) {
        if (importResult != null) {
            delay(3000L)
            deckViewModel.clearImportResult()
        }
    }

    if (showConfirmDialog && pendingUri != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false; pendingUri = null },
            title = { Text(stringResource(R.string.confirm_import_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.confirm_import_text))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF3F0FF)) {
                        Text(
                            newDeckName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryPurple
                        )
                    }
                    if (newDeckDescription.isNotBlank()) {
                        Text(newDeckDescription, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        deckViewModel.importCsvWithNewDeck(pendingUri!!, newDeckName, newDeckDescription, ownerId)
                        showConfirmDialog = false
                        pendingUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) { Text(stringResource(R.string.create_import_btn)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false; pendingUri = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_export), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = null)
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
                .padding(24.dp)
        ) {
            Text(stringResource(R.string.manage_your_words), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.import_export_desc),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (importResult != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (importResult!!.startsWith("Error") || importResult!!.startsWith("Lỗi")) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ) {
                    Text(
                        importResult!!,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = if (importResult!!.startsWith("Error") || importResult!!.startsWith("Lỗi")) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F9FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = PrimaryPurple)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.import_words), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        stringResource(R.string.import_words_desc),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F5))
                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.create_new_deck), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                if (createNewDeck) stringResource(R.string.import_new_deck_desc)
                                else stringResource(R.string.import_current_deck_desc),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = createNewDeck,
                            onCheckedChange = { createNewDeck = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryPurple)
                        )
                    }

                    AnimatedVisibility(
                        visible = createNewDeck,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = newDeckName,
                                onValueChange = { newDeckName = it },
                                label = { Text(stringResource(R.string.deck_name_label)) },
                                placeholder = { Text("e.g. Daily Life", color = Color(0xFFBDBFC9)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF8F9FF),
                                    focusedContainerColor = Color(0xFFF8F9FF),
                                    focusedBorderColor = PrimaryPurple.copy(alpha = 0.6f),
                                    unfocusedBorderColor = Color(0xFFDDE0F0)
                                )
                            )
                            OutlinedTextField(
                                value = newDeckDescription,
                                onValueChange = { newDeckDescription = it },
                                label = { Text(stringResource(R.string.description_optional)) },
                                placeholder = { Text("e.g. Everyday vocabulary", color = Color(0xFFBDBFC9)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF8F9FF),
                                    focusedContainerColor = Color(0xFFF8F9FF),
                                    focusedBorderColor = PrimaryPurple.copy(alpha = 0.6f),
                                    unfocusedBorderColor = Color(0xFFDDE0F0)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { importLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        enabled = !isLoading && (!createNewDeck || newDeckName.isNotBlank())
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (isLoading) stringResource(R.string.importing) else stringResource(R.string.choosing_file))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F9FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.SaveAlt, contentDescription = null, tint = PrimaryPurple)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.export_deck), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        stringResource(R.string.export_deck_desc),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(20.dp))

                    ExposedDropdownMenuBox(
                        expanded = exportDropdownExpanded,
                        onExpandedChange = { exportDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedExportDeck?.name ?: stringResource(R.string.select_a_deck),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.deck_to_export)) },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF8F9FF),
                                focusedContainerColor = Color(0xFFF8F9FF),
                                focusedBorderColor = PrimaryPurple.copy(alpha = 0.6f),
                                unfocusedBorderColor = Color(0xFFDDE0F0)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = exportDropdownExpanded,
                            onDismissRequest = { exportDropdownExpanded = false }
                        ) {
                            deckList.forEach { deck ->
                                DropdownMenuItem(
                                    text = { Text(deck.name) },
                                    onClick = {
                                        selectedExportDeck = deck
                                        exportDropdownExpanded = false
                                    }
                                )
                            }
                            if (deckList.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.no_decks_available), color = Color.Gray) },
                                    onClick = { exportDropdownExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            val exportName = "${selectedExportDeck?.name?.replace(" ", "_") ?: "minlish"}_export.csv"
                            exportLauncher.launch(exportName)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE8EAF6)),
                        enabled = selectedExportDeck != null
                    ) {
                        Text(stringResource(R.string.export_csv_btn), color = if (selectedExportDeck != null) Color(0xFF1A1C2E) else Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF8E1).copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("💡", fontSize = 16.sp)
                    Text(
                        stringResource(R.string.import_tip),
                        fontSize = 13.sp,
                        color = Color(0xFF795548),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}