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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minlish.model.SrsCard
import com.example.minlish.model.Vocabulary
import com.example.minlish.ui.navigation.Screen
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.FlashcardViewModel
import com.example.minlish.util.Rating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    navController: NavController,
    deckId: String,
    userId: String,
    viewModel: FlashcardViewModel
) {
    val session by viewModel.session.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val limitReached by viewModel.limitReached.observeAsState(false)
    val srsMap by viewModel.srsMap.observeAsState(emptyMap())
    val currentStreak by viewModel.currentStreak.observeAsState(0)
    
    // Fetch deck info to show name
    val deckViewModel: com.example.minlish.ui.viewmodel.DeckViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val deckList by deckViewModel.deckList.observeAsState(emptyList())
    val currentDeck = remember(deckList, deckId) { deckList.find { it.id == deckId } }

    var isFlipped by remember { mutableStateOf(false) }
    var bookmarkedIds by remember { mutableStateOf(setOf<String>()) }

    if (limitReached) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLimit() },
            title = { Text("Hôm nay học đủ rồi! 🎯", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn đã hoàn thành số lượng từ mục tiêu cho hôm nay. Bạn có muốn tiếp tục học thêm không?") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissLimit() }) {
                    Text("Tiếp tục", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Dừng lại", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    LaunchedEffect(deckId) {
        viewModel.loadSession(deckId, userId)
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

    val currentSession = session

    if (currentSession == null || (currentSession.cards.isEmpty() && !currentSession.isFinished)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentDeck?.name ?: "Study Session", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
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
        SessionCompleteScreen(
            correctCount = currentSession.correctCount,
            totalCount = currentSession.doneCount,
            againCount = currentSession.againCards.size,
            streak = currentStreak,
            onBack = { navController.popBackStack() },
            onKeepGoing = { viewModel.loadSession(deckId, userId, forceAll = true) },
            onReviewAgain = { viewModel.loadSession(deckId, userId, retryAgainOnly = true) }
        )
        return
    }

    val currentCard = currentSession.cards[currentSession.currentIndex]
    val currentSrs = srsMap[currentCard.id]
    val isBookmarked = currentCard.id in bookmarkedIds

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
                        Text(
                            currentDeck?.name ?: "Session Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF44464F)
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val progress = if (currentSession.cards.isNotEmpty())
                                currentSession.currentIndex.toFloat() / currentSession.cards.size
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
                                "${currentSession.currentIndex + 1}/${currentSession.cards.size}",
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
                        "Tap the card to reveal the meaning",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFBDBFC9)
                    )
                }
            } else {
                RatingButtons(
                    currentSrs = currentSrs,
                    onRate = { rating -> viewModel.rate(rating, deckId, userId) }
                )
            }
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
                "No cards in this deck",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1C2E)
            )
            Text(
                "Add some words first or restart your session.",
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
                Text("Add Words", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onStudyAgain,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE8EAF6))
            ) {
                Text("Study again", color = Color(0xFF44464F))
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
                    "Tap to reveal",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFBDBFC9)
                )
            }
        }
    }
}

@Composable
fun FlashcardBack(card: Vocabulary) {
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

            Text("Meaning", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBDBFC9))
            Spacer(Modifier.height(6.dp))
            Text(
                card.meaning,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1C2E)
            )

            if (card.description.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Text("Definition", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBDBFC9))
                Spacer(Modifier.height(6.dp))
                Text(
                    card.description,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    color = Color(0xFF44464F)
                )
            }

            if (card.example.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF8F9FF)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Example", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBDBFC9))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            card.example,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = Color(0xFF44464F)
                        )
                    }
                }
            }

            if (card.collocation.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Text("Collocations", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBDBFC9))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    card.collocation.split(",").forEach { col ->
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8F9FF)) {
                            Text(
                                col.trim(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF44464F)
                            )
                        }
                    }
                }
            }

            if (card.relatedWords.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Text("Related Words", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBDBFC9))
                Spacer(Modifier.height(6.dp))
                Text(
                    card.relatedWords,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF44464F)
                )
            }

            if (card.note.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Text("Note", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBDBFC9))
                Spacer(Modifier.height(6.dp))
                Text(
                    card.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF44464F)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun intervalLabel(rating: Rating, srs: SrsCard?): String {
    if (srs == null || srs.repetitions == 0) {
        return when (rating) {
            Rating.Again -> "<1m"
            Rating.Hard  -> "6m"
            Rating.Good  -> "10m"
            Rating.Easy  -> "4d"
        }
    }
    val interval = srs.interval
    val ease = srs.easeFactor
    return when (rating) {
        Rating.Again -> "<1m"
        Rating.Hard  -> {
            val next = (interval * 1.2).toInt().coerceAtLeast(1)
            if (next < 7) "${next}d" else "${next / 7}w"
        }
        Rating.Good  -> {
            val next = (interval * ease).toInt().coerceAtLeast(1)
            if (next < 7) "${next}d" else "${next / 7}w"
        }
        Rating.Easy  -> {
            val next = (interval * ease * 1.3).toInt().coerceAtLeast(1)
            if (next < 7) "${next}d" else "${next / 7}w"
        }
    }
}

@Composable
fun RatingButtons(currentSrs: SrsCard?, onRate: (Rating) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "How well did you remember it?",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF44464F)
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RatingButton(
                label = "Again",
                sublabel = intervalLabel(Rating.Again, currentSrs),
                bg = Color(0xFFFFEBEE),
                fg = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            ) { onRate(Rating.Again) }
            RatingButton(
                label = "Hard",
                sublabel = intervalLabel(Rating.Hard, currentSrs),
                bg = Color(0xFFFFF3E0),
                fg = Color(0xFFF57C00),
                modifier = Modifier.weight(1f)
            ) { onRate(Rating.Hard) }
            RatingButton(
                label = "Good",
                sublabel = intervalLabel(Rating.Good, currentSrs),
                bg = Color(0xFFE8F5E9),
                fg = Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            ) { onRate(Rating.Good) }
            RatingButton(
                label = "Easy",
                sublabel = intervalLabel(Rating.Easy, currentSrs),
                bg = Color(0xFFE3F2FD),
                fg = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            ) { onRate(Rating.Easy) }
        }
    }
}

@Composable
fun RatingButton(label: String, sublabel: String, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
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
            Text(
                sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = fg.copy(alpha = 0.7f)
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
                "Session complete!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1A1C2E)
            )
            Text(
                "You reviewed $totalCount words in this session.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF44464F)
            )

            Spacer(Modifier.height(40.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "CORRECT",
                    value = "$correctCount",
                    subValue = "${if (totalCount > 0) correctCount * 100 / totalCount else 0}%",
                    color = Color(0xFF2E7D32)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "NEEDS AGAIN",
                    value = "$againCount",
                    subValue = "words",
                    color = Color(0xFFD32F2F)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "REVIEWED",
                    value = "$totalCount",
                    subValue = "words",
                    color = Color(0xFF44464F)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "STREAK",
                    value = "$streak",
                    subValue = "days 🔥",
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
                        "Review $againCount 'Again' words",
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
                    Text("Back to deck", color = Color(0xFF44464F))
                }
                Button(
                    onClick = onKeepGoing,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Keep going")
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
