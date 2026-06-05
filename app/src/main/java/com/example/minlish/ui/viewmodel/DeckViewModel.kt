package com.example.minlish.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minlish.model.Deck
import com.example.minlish.model.Vocabulary
import com.example.minlish.repository.DeckRepository
import com.example.minlish.repository.ImportExportRepository
import com.example.minlish.repository.SrsRepository
import com.example.minlish.repository.VocabularyRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeckStats(
    val totalWords: Int = 0,
    val learnedWords: Int = 0
) {
    val progress: Float get() = if (totalWords == 0) 0f else learnedWords.toFloat() / totalWords
}

class DeckViewModel(application: Application) : AndroidViewModel(application) {

    private val deckRepo = DeckRepository()
    private val vocabRepo = VocabularyRepository()
    private val srsRepo = SrsRepository()
    private val importExportRepo = ImportExportRepository(application)

    private val deckListState = MutableLiveData<List<Deck>>(emptyList())
    val deckList: LiveData<List<Deck>> = deckListState

    private val vocabListState = MutableLiveData<List<Vocabulary>>(emptyList())
    val vocabList: LiveData<List<Vocabulary>> = vocabListState

    private val deckStatsState = MutableLiveData<Map<String, DeckStats>>(emptyMap())
    val deckStats: LiveData<Map<String, DeckStats>> = deckStatsState

    private val isLoadingState = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = isLoadingState

    private val errorState = MutableLiveData<String?>(null)
    val error: LiveData<String?> = errorState

    private val importResultState = MutableLiveData<String?>(null)
    val importResult: LiveData<String?> = importResultState

    fun loadDecks(ownerId: String) {
        viewModelScope.launch {
            isLoadingState.postValue(true)
            val decks = deckRepo.getByOwner(ownerId)
            deckListState.postValue(decks)

            val statsMap = mutableMapOf<String, DeckStats>()
            for (deck in decks) {
                val vocabs = vocabRepo.getByDeck(deck.id)
                val srsCards = srsRepo.getByDeck(deck.id)
                val learnedIds = srsCards.filter { it.repetitions > 0 }.map { it.vocabId }.toSet()
                statsMap[deck.id] = DeckStats(
                    totalWords = vocabs.size,
                    learnedWords = learnedIds.size
                )
            }
            deckStatsState.postValue(statsMap)
            isLoadingState.postValue(false)
        }
    }

    fun loadVocabs(deckId: String) {
        viewModelScope.launch {
            isLoadingState.postValue(true)
            vocabListState.postValue(vocabRepo.getByDeck(deckId))
            isLoadingState.postValue(false)
        }
    }

    fun addDeck(name: String, description: String, tags: String, ownerId: String, newWords: Int = 10, reviews: Int = 40) {
        if (name.isBlank()) { errorState.value = "Tên bộ từ không được để trống"; return }
        viewModelScope.launch {
            val deck = Deck(
                name = name,
                description = description,
                tags = tags,
                createdAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                ownerId = ownerId,
                newWordsPerDay = newWords,
                reviewsPerDay = reviews
            )
            deckRepo.save(deck)
            loadDecks(ownerId)
        }
    }

    fun deleteDeck(deckId: String, ownerId: String) {
        viewModelScope.launch {
            deckRepo.delete(deckId)
            vocabRepo.deleteByDeck(deckId)
            srsRepo.deleteByDeck(deckId)
            loadDecks(ownerId)
        }
    }

    fun addVocab(
        word: String, pronunciation: String, meaning: String,
        description: String, example: String, collocation: String,
        relatedWords: String, note: String, deckId: String
    ) {
        viewModelScope.launch {
            val vocab = Vocabulary(
                word = word, pronunciation = pronunciation, meaning = meaning,
                description = description, example = example, collocation = collocation,
                relatedWords = relatedWords, note = note, deckId = deckId
            )
            vocabRepo.save(vocab)

            val updatedVocabs = vocabRepo.getByDeck(deckId)
            vocabListState.postValue(updatedVocabs)

            val srsCards = srsRepo.getByDeck(deckId)
            val learnedIds = srsCards.filter { it.repetitions > 0 }.map { it.vocabId }.toSet()
            val currentMap = deckStatsState.value?.toMutableMap() ?: mutableMapOf()
            currentMap[deckId] = DeckStats(
                totalWords = updatedVocabs.size,
                learnedWords = learnedIds.size
            )
            deckStatsState.postValue(currentMap)
        }
    }

    fun deleteVocab(vocabId: String, deckId: String) {
        viewModelScope.launch {
            vocabRepo.delete(vocabId)

            val updatedVocabs = vocabRepo.getByDeck(deckId)
            vocabListState.postValue(updatedVocabs)

            val srsCards = srsRepo.getByDeck(deckId)
            val learnedIds = srsCards.filter { it.repetitions > 0 }.map { it.vocabId }.toSet()
            val currentMap = deckStatsState.value?.toMutableMap() ?: mutableMapOf()
            currentMap[deckId] = DeckStats(
                totalWords = updatedVocabs.size,
                learnedWords = learnedIds.size
            )
            deckStatsState.postValue(currentMap)
        }
    }

    fun importCsv(uri: Uri, deckId: String) {
        viewModelScope.launch {
            isLoadingState.postValue(true)
            val result = importExportRepo.importFromUri(uri, deckId)
            if (result.error != null) {
                importResultState.postValue("Lỗi: ${result.error}")
            } else {
                importResultState.postValue("Đã nhập ${result.imported} từ, bỏ qua ${result.skipped}")
                vocabListState.postValue(vocabRepo.getByDeck(deckId))
            }
            isLoadingState.postValue(false)
        }
    }

    fun importCsvWithNewDeck(uri: Uri, deckName: String, deckDescription: String, ownerId: String) {
        if (deckName.isBlank()) { errorState.value = "Tên bộ từ không được để trống"; return }
        viewModelScope.launch {
            isLoadingState.postValue(true)
            val deck = Deck(
                name = deckName,
                description = deckDescription,
                createdAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                ownerId = ownerId
            )
            deckRepo.save(deck)
            val result = importExportRepo.importFromUri(uri, deck.id)
            if (result.error != null) {
                importResultState.postValue("Lỗi: ${result.error}")
            } else {
                importResultState.postValue("Đã tạo deck \"${deckName}\" và nhập ${result.imported} từ, bỏ qua ${result.skipped}")
                loadDecks(ownerId)
            }
            isLoadingState.postValue(false)
        }
    }

    fun exportCsv(uri: Uri, deckId: String) {
        viewModelScope.launch {
            val success = importExportRepo.exportToUri(uri, deckId)
            importResultState.postValue(if (success) "Xuất file thành công" else "Xuất file thất bại")
        }
    }

    fun clearError() { errorState.value = null }
    fun clearImportResult() { importResultState.value = null }
}