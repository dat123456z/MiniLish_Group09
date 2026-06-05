package com.example.minlish.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minlish.model.StudyLog
import com.example.minlish.repository.DeckRepository
import com.example.minlish.repository.SrsRepository
import com.example.minlish.repository.StudyLogRepository
import com.example.minlish.repository.VocabularyRepository
import com.example.minlish.util.StreakUtils
import kotlinx.coroutines.launch

data class DailyActivityData(
    val label: String,
    val count: Int,
    val correct: Int,
    val total: Int
)

data class DashboardStats(
    val totalWords: Int = 0,
    val learnedWords: Int = 0,
    val streak: Int = 0,
    val accuracyPercent: Int = 0,
    val totalDecks: Int = 0,
    val todayNewWords: Int = 0,
    val dailyTarget: Int = 0,
    val todayCorrect: Int = 0,
    val todayTotal: Int = 0,
    val weeklyActivity: List<DailyActivityData> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val deckRepo = DeckRepository()
    private val vocabRepo = VocabularyRepository()
    private val srsRepo = SrsRepository()
    private val logRepo = StudyLogRepository()

    private val statsState = MutableLiveData<DashboardStats>()
    val stats: LiveData<DashboardStats> = statsState

    private val isLoadingState = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = isLoadingState

    fun load(userId: String) {
        isLoadingState.value = true
        viewModelScope.launch {
            val decks = deckRepo.getByOwner(userId)
            val deckIds = decks.map { it.id }

            val allVocabs = deckIds.flatMap { vocabRepo.getByDeck(it) }
            val vocabIdsSet = allVocabs.map { it.id }.toSet()

            val allSrs = deckIds.flatMap { srsRepo.getByDeck(it) }
            val learnedIds = allSrs
                .filter { it.repetitions > 0 && it.vocabId in vocabIdsSet }
                .map { it.vocabId }.toSet()

            val logs = logRepo.getByUser(userId)
            val todayLog = logs.find { it.date == StreakUtils.today() }

            val todayNew = todayLog?.wordStudied ?: 0
            val totalDailyTarget = decks.sumOf { it.newWordsPerDay }
            val todayCorrect = todayLog?.correctCount ?: 0
            val todayTotal = todayLog?.totalCount ?: 0

            val streak = StreakUtils.computeStreak(logs)
            val accuracy = StreakUtils.accuracyPercent(logs)

            val last7 = StreakUtils.last7Days()
            val logByDate = logs.associateBy { it.date }
            val weekly = last7.map { date ->
                val log = logByDate[date]
                DailyActivityData(
                    label = date.substring(5),
                    count = log?.totalCount ?: 0,
                    correct = log?.correctCount ?: 0,
                    total = log?.totalCount ?: 0
                )
            }

            statsState.postValue(
                DashboardStats(
                    totalWords = allVocabs.size,
                    learnedWords = learnedIds.size,
                    streak = streak,
                    accuracyPercent = accuracy,
                    totalDecks = decks.size,
                    todayNewWords = todayNew,
                    dailyTarget = totalDailyTarget,
                    todayCorrect = todayCorrect,
                    todayTotal = todayTotal,
                    weeklyActivity = weekly
                )
            )
            isLoadingState.postValue(false)
        }
    }
}