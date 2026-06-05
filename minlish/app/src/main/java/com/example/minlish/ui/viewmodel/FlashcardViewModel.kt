package com.example.minlish.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minlish.model.Deck
import com.example.minlish.model.SrsCard
import com.example.minlish.model.StudyLog
import com.example.minlish.model.Vocabulary
import com.example.minlish.repository.DeckRepository
import com.example.minlish.repository.SrsRepository
import com.example.minlish.repository.StudyLogRepository
import com.example.minlish.repository.VocabularyRepository
import com.example.minlish.util.Rating
import com.example.minlish.util.Sm2
import com.example.minlish.util.StreakUtils
import kotlinx.coroutines.launch
import kotlin.math.min

enum class QuizType { WORD_TO_MEANING, MEANING_TO_WORD }

data class QuizCard(
    val vocab: Vocabulary,
    val type: QuizType,
    val options: List<String>
)

data class FlashcardSession(
    val cards: List<Vocabulary>,
    val quizCards: List<QuizCard> = emptyList(),
    val currentIndex: Int = 0,
    val totalCount: Int = 0,
    val doneCount: Int = 0,
    val correctCount: Int = 0,
    val againCards: List<Vocabulary> = emptyList(),
    val isFinished: Boolean = false
)

class FlashcardViewModel(application: Application) : AndroidViewModel(application) {

    private val deckRepo = DeckRepository()
    private val vocabRepo = VocabularyRepository()
    private val srsRepo = SrsRepository()
    private val logRepo = StudyLogRepository()

    private val sessionState = MutableLiveData<FlashcardSession>()
    val session: LiveData<FlashcardSession> = sessionState

    private val limitReachedState = MutableLiveData(false)
    val limitReached: LiveData<Boolean> = limitReachedState

    private val isLoadingState = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = isLoadingState

    private val srsMapState = MutableLiveData<Map<String, SrsCard>>(emptyMap())
    val srsMap: LiveData<Map<String, SrsCard>> = srsMapState

    private val currentStreakState = MutableLiveData(0)
    val currentStreak: LiveData<Int> = currentStreakState

    private var currentDeckInternal: Deck? = null
    private var hasDismissedLimit = false

    fun loadSession(deckId: String, userId: String, retryAgainOnly: Boolean = false, forceAll: Boolean = false, reviewOnly: Boolean = false, dueOnly: Boolean = false) {
        isLoadingState.value = true
        limitReachedState.value = false
        hasDismissedLimit = false

        viewModelScope.launch {
            val decks = deckRepo.getByOwner(userId)
            val deckIds = if (deckId == "all") decks.map { it.id } else listOf(deckId)
            currentDeckInternal = if (deckId == "all") null else decks.find { it.id == deckId }

            val allSrs = srsRepo.getByDecks(deckIds)
            val srsMap = allSrs.associateBy { it.vocabId } //chuển thành map để giảm độ phức tạp từ O(n) còn O(1)
            srsMapState.postValue(srsMap)

            if (retryAgainOnly) {
                val againList = sessionState.value?.againCards ?: emptyList()
                if (againList.isNotEmpty()) {
                    sessionState.postValue(
                        FlashcardSession(
                            cards = againList,
                            currentIndex = 0,
                            totalCount = againList.size,
                            doneCount = 0,
                            correctCount = 0,
                            againCards = emptyList(),
                            isFinished = false
                        )
                    )
                }
                isLoadingState.postValue(false)
                return@launch
            }

            val allVocabs = vocabRepo.getByDecks(deckIds)
            if (allVocabs.isEmpty()) {
                sessionState.postValue(FlashcardSession(emptyList(), isFinished = false))
                isLoadingState.postValue(false)
                return@launch
            }

            val sessionCards = when {
                forceAll -> allVocabs
                dueOnly -> {
                    // STRICT SM2: Filter only due cards for review session
                    allVocabs.filter { v ->
                        val srs = srsMap[v.id]
                        srs != null && Sm2.isdue(srs)
                    }.sortedBy { srsMap[it.id]?.nextReviewDate }
                }
                reviewOnly -> allVocabs
                else -> {
                    val dueCards = allVocabs.filter { v ->
                        val srs = srsMap[v.id]
                        srs != null && Sm2.isdue(srs) 
                    }.sortedBy { srsMap[it.id]?.nextReviewDate }

                    val newCards = allVocabs.filter { v -> srsMap[v.id] == null }
                    val newLimit = currentDeckInternal?.newWordsPerDay ?: 10
                    val reviewLimit = currentDeckInternal?.reviewsPerDay ?: 40
                    
                    dueCards.take(reviewLimit) + newCards.take(newLimit)
                }
            }

            if (sessionCards.isEmpty()) {
                sessionState.postValue(FlashcardSession(emptyList(), isFinished = false))
            } else {
                if (reviewOnly) {
                    val quizList = mutableListOf<QuizCard>()
                    sessionCards.forEach { vocab ->
                        quizList.add(QuizCard(vocab, QuizType.WORD_TO_MEANING, generateOptions(vocab, allVocabs, QuizType.WORD_TO_MEANING)))
                        quizList.add(QuizCard(vocab, QuizType.MEANING_TO_WORD, generateOptions(vocab, allVocabs, QuizType.MEANING_TO_WORD)))
                    }
                    val shuffledQuiz = quizList.shuffled()
                    sessionState.postValue(
                        FlashcardSession(
                            cards = sessionCards,
                            quizCards = shuffledQuiz,
                            currentIndex = 0,
                            totalCount = shuffledQuiz.size,
                            doneCount = 0,
                            correctCount = 0,
                            againCards = emptyList(),
                            isFinished = false
                        )
                    )
                } else {
                    val initialCards = sessionCards.shuffled()
                    sessionState.postValue(
                        FlashcardSession(
                            cards = initialCards,
                            currentIndex = 0,
                            totalCount = initialCards.size,
                            doneCount = 0,
                            correctCount = 0,
                            againCards = emptyList(),
                            isFinished = false
                        )
                    )
                }
            }
            isLoadingState.postValue(false)
        }
    }

    private fun generateOptions(current: Vocabulary, all: List<Vocabulary>, type: QuizType): List<String> {
        val correct = if (type == QuizType.WORD_TO_MEANING) current.meaning else current.word
        val distractors = all.filter { it.id != current.id }
            .map { if (type == QuizType.WORD_TO_MEANING) it.meaning else it.word }
            .filter { it != correct }
            .distinct()
            .shuffled()
            .take(3)
        return (distractors + correct).shuffled()
    }

    fun rate(rating: Rating, deckId: String, userId: String) {
        val current = sessionState.value ?: return //elvis operator
        if (current.isFinished) return

        val vocab = current.cards[current.currentIndex]
        val isCorrect = rating == Rating.Good || rating == Rating.Easy

        val nextIndex = current.currentIndex + 1
        val newDone = current.doneCount + 1

        val limit = currentDeckInternal?.newWordsPerDay ?: 10
        if (newDone == limit && nextIndex < current.cards.size && !hasDismissedLimit) {
            limitReachedState.postValue(true)
        }

        val updatedCards = current.cards.toMutableList()
        val totalCount = updatedCards.size
        val finished = nextIndex >= totalCount
        val newCorrectCount = current.correctCount + if (isCorrect) 1 else 0
        val newAgainCards = if (rating == Rating.Again) current.againCards + vocab else current.againCards

        sessionState.postValue(
            current.copy(
                cards = updatedCards,
                currentIndex = if (finished) current.currentIndex else nextIndex,
                doneCount = newDone,
                correctCount = newCorrectCount,
                againCards = newAgainCards,
                isFinished = finished
            )
        )

        viewModelScope.launch {
            val existingSrs = srsRepo.findByVocab(vocab.id)
            val isFirstTimeLearned = (existingSrs == null || existingSrs.repetitions == 0) && isCorrect
            val srsToUpdate = existingSrs ?: SrsCard(vocabId = vocab.id, deckId = deckId)
            val updatedSrs = Sm2.review(srsToUpdate, rating)
            srsRepo.saveOrUpdate(updatedSrs)

            val currentMap = srsMapState.value?.toMutableMap() ?: mutableMapOf()
            currentMap[vocab.id] = updatedSrs
            srsMapState.postValue(currentMap)

            updateStudyLog(userId, isCorrect, isFirstTimeLearned)
        }
    }

    private suspend fun updateStudyLog(userId: String, isCorrect: Boolean, isFirstTimeLearned: Boolean) {
        val today = StreakUtils.today()
        val existing = logRepo.findByUserAndDate(userId, today)
        val updated = (existing ?: StudyLog(userId = userId, date = today)).copy(
            wordStudied = (existing?.wordStudied ?: 0) + if (isFirstTimeLearned) 1 else 0,
            wordReviewed = (existing?.wordReviewed ?: 0) + 1,
            correctCount = (existing?.correctCount ?: 0) + if (isCorrect) 1 else 0,
            totalCount = (existing?.totalCount ?: 0) + 1
        )
        logRepo.saveOrUpdate(updated)

        val allLogs = logRepo.getByUser(userId)
        currentStreakState.postValue(StreakUtils.computeStreak(allLogs))
    }

    fun dismissLimit() {
        hasDismissedLimit = true
        limitReachedState.postValue(false)
    }
}
