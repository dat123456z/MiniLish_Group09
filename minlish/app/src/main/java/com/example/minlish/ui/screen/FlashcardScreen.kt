package com.example.minlish.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minlish.R
import com.example.minlish.model.SrsCard
import com.example.minlish.model.Vocabulary
import com.example.minlish.ui.navigation.Screen
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.FlashcardViewModel
import com.example.minlish.ui.viewmodel.QuizCard
import com.example.minlish.ui.viewmodel.QuizType
import com.example.minlish.util.Rating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    navController: NavController,
    deckId: String,
    userId: String,
    viewModel: FlashcardViewModel,
    reviewOnly: Boolean = false,
    dueOnly: Boolean = false
) {
    val session by viewModel.session.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val limitReached by viewModel.limitReached.observeAsState(false)
    val currentStreak by viewModel.currentStreak.observeAsState(0)

    val deckViewModel: com.example.minlish.ui.viewmodel.DeckViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val deckList by deckViewModel.deckList.observeAsState(emptyList())
    val currentDeck = remember(deckList, deckId) { deckList.find { it.id == deckId } }

    var isFlipped by remember { mutableStateOf(false) }
    var bookmarkedIds by remember { mutableStateOf(setOf<String>()) }

    if (limitReached) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLimit() },
            title = { Text(stringResource(R.string.daily_goal_reached), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.daily_goal_reached_desc)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissLimit() }) {
                    Text(stringResource(R.string.keep_going), color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(stringResource(R.string.stop), color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    LaunchedEffect(deckId, reviewOnly, dueOnly) {
        viewModel.loadSession(deckId, userId, reviewOnly = reviewOnly, dueOnly = dueOnly)
        deckViewModel.loadDecks(userId)
    }

    LaunchedEffect(session?.currentIndex) {
        isFlipped = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryPurple)
        }
        return
    }

    val currentSession = session ?: return

    if (currentSession.cards.isEmpty() && currentSession.quizCards.isEmpty() && !currentSession.isFinished) {
        val emptyTitle = when {
            deckId == "all" -> stringResource(R.string.daily_review_sm2)
            else -> currentDeck?.name ?: stringResource(R.string.study_session)
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(emptyTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FF))
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                EmptyDeckState(
                    onAddWord = { navController.navigate(Screen.AddWord.createRoute(deckId)) },
                    onStudyAgain = { viewModel.loadSession(deckId, userId, forceAll = true) }
                )
            }
        }
        return
    }

    if (currentSession.isFinished) {
        val totalCount = if (currentSession.quizCards.isNotEmpty()) currentSession.quizCards.size else currentSession.doneCount
        SessionCompleteScreen(
            correctCount = currentSession.correctCount,
            totalCount = totalCount,
            againCount = currentSession.againCards.size,
            streak = currentStreak,
            onBack = { navController.popBackStack() },
            onKeepGoing = { viewModel.loadSession(deckId, userId, forceAll = true) },
            onReviewAgain = { viewModel.loadSession(deckId, userId, retryAgainOnly = true) }
        )
        return
    }

    val isQuiz = reviewOnly && !dueOnly && currentSession.quizCards.isNotEmpty()
    val currentCard = if (isQuiz) currentSession.quizCards[currentSession.currentIndex].vocab else currentSession.cards[currentSession.currentIndex]
    val isBookmarked = currentCard.id in bookmarkedIds

    var selectedOption by remember(currentSession.currentIndex) { mutableStateOf<String?>(null) }
    var isAnswered by remember(currentSession.currentIndex) { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "flip"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val sessionTitle = when {
                            dueOnly -> stringResource(R.string.daily_review_sm2)
                            deckId == "all" -> stringResource(R.string.daily_review_sm2)
                            reviewOnly -> stringResource(R.string.quiz_mode)
                            else -> currentDeck?.name ?: stringResource(R.string.session_progress)
                        }
                        Text(
                            sessionTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44464F)
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val totalItems = if (isQuiz) currentSession.quizCards.size else currentSession.cards.size
                            val progress = if (totalItems > 0)
                                currentSession.currentIndex.toFloat() / totalItems
                            else 0f

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PrimaryPurple,
                                trackColor = Color(0xFFE8EAF6)
                            )
                            Text(
                                "${currentSession.currentIndex + 1}/$totalItems",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryPurple
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1A1C2E))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        bookmarkedIds = if (isBookmarked)
                            bookmarkedIds - currentCard.id
                        else
                            bookmarkedIds + currentCard.id
                    }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) PrimaryPurple else Color(0xFFBDBFC9)
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            if (isQuiz) {
                val quizCard = currentSession.quizCards[currentSession.currentIndex]
                QuizSection(
                    quizCard = quizCard,
                    selectedOption = selectedOption,
                    isAnswered = isAnswered,
                    onOptionSelected = { option ->
                        if (!isAnswered) {
                            selectedOption = option
                            isAnswered = true
                        }
                    },
                    onNext = {
                        val correct = if (quizCard.type == QuizType.WORD_TO_MEANING) quizCard.vocab.meaning else quizCard.vocab.word
                        val isCorrect = selectedOption == correct
                        viewModel.rate(if (isCorrect) Rating.Good else Rating.Again, deckId, userId)
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable { isFlipped = !isFlipped },
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        FlashcardFront(card = currentCard)
                    } else {
                        Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                            FlashcardBack(card = currentCard)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (!isFlipped) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TouchApp,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color(0xFFBDBFC9)
                        )
                        Text(
                            stringResource(R.string.tap_to_reveal),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFBDBFC9)
                        )
                    }
                } else {
                    RatingButtons(
                        onRate = { rating -> viewModel.rate(rating, deckId, userId) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizSection(
    quizCard: QuizCard,
    selectedOption: String?,
    isAnswered: Boolean,
    onOptionSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    val questionText = if (quizCard.type == QuizType.WORD_TO_MEANING) quizCard.vocab.word else quizCard.vocab.meaning
    val correctAnswer = if (quizCard.type == QuizType.WORD_TO_MEANING) quizCard.vocab.meaning else quizCard.vocab.word

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(
                    questionText,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            quizCard.options.forEachIndexed { index, option ->
                val letter = when(index) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    else -> "D"
                }
                
                val isCorrect = option == correctAnswer
                val isSelected = option == selectedOption
                
                val containerColor = when {
                    isAnswered && isCorrect -> Color(0xFFE8F5E9)
                    isAnswered && isSelected && !isCorrect -> Color(0xFFFFEBEE)
                    isSelected -> PrimaryPurple.copy(alpha = 0.1f)
                    else -> Color.White
                }
                
                val borderColor = when {
                    isAnswered && isCorrect -> Color(0xFF2E7D32)
                    isAnswered && isSelected && !isCorrect -> Color(0xFFD32F2F)
                    isSelected -> PrimaryPurple
                    else -> Color(0xFFE8EAF6)
                }

                Surface(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = containerColor,
                    border = BorderStroke(2.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) PrimaryPurple else Color(0xFFF0F2FF),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    letter,
                                    color = if (isSelected) Color.White else PrimaryPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isAnswered && isCorrect) Color(0xFF1B5E20) else Color(0xFF44464F)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (isAnswered) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(stringResource(R.string.next_word), fontWeight = FontWeight.Bold)
            }
        } else {
            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
fun EmptyDeckState(onAddWord: () -> Unit, onStudyAgain: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = PrimaryPurple.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = PrimaryPurple
                    )
                }
            }
            Text(
                stringResource(R.string.no_cards_in_deck),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1C2E)
            )
            Text(
                stringResource(R.string.add_some_words_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF44464F),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAddWord,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(stringResource(R.string.add_words), fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onStudyAgain,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE8EAF6))
            ) {
                Text(stringResource(R.string.study_again), color = Color(0xFF44464F))
            }
        }
    }
}

@Composable
fun FlashcardFront(card: Vocabulary) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    card.word,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        fontSize = 38.sp
                    ),
                    textAlign = TextAlign.Center
                )
                if (card.pronunciation.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            card.pronunciation,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF44464F)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Pronounce",
                            modifier = Modifier.size(22.dp),
                            tint = Color(0xFFBDBFC9)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Color(0xFFBDBFC9)
                )
                Text(
                    stringResource(R.string.tap_to_reveal),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFBDBFC9)
                )
            }
        }
    }
}

@Composable
fun FlashcardBack(card: Vocabulary) {
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = { Text(card.word, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (card.pronunciation.isNotBlank()) {
                        DetailItem(stringResource(R.string.pronunciation_label), card.pronunciation)
                    }
                    DetailItem(stringResource(R.string.meaning_label), card.meaning)
                    if (card.description.isNotBlank()) {
                        DetailItem(stringResource(R.string.definition_label), card.description)
                    }
                    if (card.example.isNotBlank()) {
                        DetailItem(stringResource(R.string.example_label), card.example)
                    }
                    if (card.collocation.isNotBlank()) {
                        DetailItem(stringResource(R.string.collocation_label), card.collocation)
                    }
                    if (card.relatedWords.isNotBlank()) {
                        DetailItem(stringResource(R.string.related_words_label), card.relatedWords)
                    }
                    if (card.note.isNotBlank()) {
                        DetailItem(stringResource(R.string.note_label), card.note)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetails = false }) {
                    Text(stringResource(R.string.cancel), color = PrimaryPurple)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp)
        ) {
            Text(
                card.word,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            )
            if (card.pronunciation.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    card.pronunciation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF44464F)
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFE8EAF6))
            Spacer(Modifier.height(20.dp))

            Text(stringResource(R.string.meaning_label), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C2E))
            Spacer(Modifier.height(8.dp))
            Text(
                card.meaning,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF44464F)
            )

            if (card.example.isNotBlank()) {
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.example_label), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C2E))
                Spacer(Modifier.height(8.dp))
                Text(
                    card.example,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = Color(0xFF44464F)
                )
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDetails = true }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📖", fontSize = 16.sp)
                    Text(
                        stringResource(R.string.view_full_details),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun RatingButtons(onRate: (Rating) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.how_well_remembered),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF44464F)
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RatingButton(
                label = stringResource(R.string.rating_again),
                bg = Color(0xFFFFEBEE),
                fg = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            ) { onRate(Rating.Again) }
            RatingButton(
                label = stringResource(R.string.rating_hard),
                bg = Color(0xFFFFF3E0),
                fg = Color(0xFFF57C00),
                modifier = Modifier.weight(1f)
            ) { onRate(Rating.Hard) }
            RatingButton(
                label = stringResource(R.string.rating_good),
                bg = Color(0xFFE8F5E9),
                fg = Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            ) { onRate(Rating.Good) }
            RatingButton(
                label = stringResource(R.string.rating_easy),
                bg = Color(0xFFE3F2FD),
                fg = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            ) { onRate(Rating.Easy) }
        }
    }
}

@Composable
fun RatingButton(label: String, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = bg
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = fg
            )
        }
    }
}

@Composable
fun SessionCompleteScreen(
    correctCount: Int,
    totalCount: Int,
    againCount: Int,
    streak: Int,
    onBack: () -> Unit,
    onKeepGoing: () -> Unit,
    onReviewAgain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = Color(0xFFFFE082)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🎉", fontSize = 60.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                stringResource(R.string.session_complete),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1C2E)
            )
            Text(
                stringResource(R.string.reviewed_words_count, totalCount),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF44464F)
            )

            Spacer(Modifier.height(40.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.stat_correct),
                    value = "$correctCount",
                    subValue = "${if (totalCount > 0) correctCount * 100 / totalCount else 0}%",
                    color = Color(0xFF2E7D32)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.stat_needs_again),
                    value = "$againCount",
                    subValue = stringResource(R.string.words).lowercase(),
                    color = Color(0xFFD32F2F)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.stat_reviewed),
                    value = "$totalCount",
                    subValue = stringResource(R.string.words).lowercase(),
                    color = Color(0xFF44464F)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.stat_streak),
                    value = "$streak",
                    subValue = stringResource(R.string.stat_days_fire),
                    color = Color(0xFFFF6D00)
                )
            }

            Spacer(Modifier.weight(1f))

            if (againCount > 0) {
                Button(
                    onClick = onReviewAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        stringResource(R.string.review_again_words, againCount),
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE8EAF6))
                ) {
                    Text(stringResource(R.string.back_to_deck), color = Color(0xFF44464F))
                }
                Button(
                    onClick = onKeepGoing,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text(stringResource(R.string.keep_going))
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, subValue: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFBDBFC9))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(subValue, style = MaterialTheme.typography.bodySmall, color = Color(0xFF44464F))
        }
    }
}
