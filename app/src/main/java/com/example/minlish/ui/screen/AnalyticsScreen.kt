package com.example.minlish.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minlish.ui.theme.PrimaryPurple
import com.example.minlish.ui.viewmodel.AuthViewModel
import com.example.minlish.ui.viewmodel.DashboardViewModel

@Composable
fun AnalyticsScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.observeAsState()
    val stats by dashboardViewModel.stats.observeAsState()

    LaunchedEffect(currentUser?.id) {
        currentUser?.let { dashboardViewModel.load(it.id) }
    }

    val learnedWords = stats?.learnedWords ?: 0
    val accuracyPercent = stats?.accuracyPercent ?: 0
    val weeklyActivity = stats?.weeklyActivity ?: emptyList()

    val todayCorrect = stats?.todayCorrect ?: 0
    val todayTotal = stats?.todayTotal ?: 0
    val accuracyProgress = if (todayTotal > 0) todayCorrect.toFloat() / todayTotal else 0f

    Scaffold(
        containerColor = Color(0xFFF8F9FF)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = PrimaryPurple)
                        Spacer(Modifier.width(12.dp))
                        Text("MinLish", fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 20.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = PrimaryPurple) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(currentUser?.name?.firstOrNull()?.toString()?.uppercase() ?: "A", color = Color.White)
                            }
                        }
                    }
                }
            }

            // 1. Trình độ hiện tại (Beginner/Intermediate/Advanced)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurple)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Current Proficiency", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        val estimatedLevel = when {
                            learnedWords < 50 -> "Beginner"
                            learnedWords < 200 -> "Intermediate"
                            else -> "Advanced"
                        }
                        Text(
                            estimatedLevel,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Master more words to reach the next level!", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }

            // 2. Biểu đồ hình tròn: Số từ Đúng / Tổng số từ đã học qua
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFFF0F2FF),
                                strokeWidth = 10.dp,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            CircularProgressIndicator(
                                progress = { accuracyProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = PrimaryPurple,
                                strokeWidth = 10.dp,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            Text(
                                "${(accuracyProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryPurple
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Today's Accuracy", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(
                                "$todayCorrect / $todayTotal",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1A1C2E)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Correct answers out of total cards studied today.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // 3. Thẻ thống kê phụ
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatBox(
                        modifier = Modifier.weight(1f),
                        label = "Total Learned",
                        value = if (stats != null) learnedWords.toString() else "--",
                        subValue = "Mastered words",
                        iconText = "📚",
                        iconBg = Color(0xFFE8EAF6)
                    )
                    StatBox(
                        modifier = Modifier.weight(1f),
                        label = "Retention Rate",
                        value = if (stats != null) "$accuracyPercent%" else "--",
                        subValue = "Lifetime accuracy",
                        iconText = "📈",
                        iconBg = Color(0xFFE8F5E9)
                    )
                }
            }

            // 4. Biểu đồ cột hoạt động hàng tuần
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Daily Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Flashcard reviews per day", color = Color.Gray, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryPurple))
                                Spacer(Modifier.width(4.dp))
                                Text("Reviews", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        val maxCount = weeklyActivity.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1
                        val todayIndex = weeklyActivity.lastIndex

                        Row(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            if (weeklyActivity.isEmpty()) {
                                repeat(7) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .fillMaxHeight(0.05f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFE8EAF6))
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text("--", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            } else {
                                weeklyActivity.forEachIndexed { index, (label, count) ->
                                    val heightFraction = (count / maxCount.toFloat()).coerceAtLeast(0.05f)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .fillMaxHeight(heightFraction)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (index == todayIndex) PrimaryPurple else Color(0xFFE8EAF6))
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(label, fontSize = 10.sp, color = Color.Gray)
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

@Composable
fun StatBox(modifier: Modifier, label: String, value: String, subValue: String, iconText: String, iconBg: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(8.dp), color = iconBg) {
                Box(contentAlignment = Alignment.Center) { Text(iconText, fontSize = 14.sp) }
            }
            Spacer(Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                subValue,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
