package com.example.minlish.repository

import com.google.firebase.firestore.FirebaseFirestore

import com.example.minlish.model.SrsCard
import kotlinx.coroutines.tasks.await

class SrsRepository {

    private val col = FirebaseFirestore.getInstance().collection("srs_cards")

    suspend fun getByDeck(deckId: String): List<SrsCard> =
        col.whereEqualTo("deckId", deckId).get().await().documents.mapNotNull { it.toCard() }

    suspend fun getByDecks(deckIds: List<String>): List<SrsCard> {
        if (deckIds.isEmpty()) return emptyList()
        // Firestore 'in' query limited to 10 items. For more, we might need multiple queries.
        return deckIds.chunked(10).flatMap { batch ->
            col.whereIn("deckId", batch).get().await().documents.mapNotNull { it.toCard() }
        }
    }

    suspend fun findByVocab(vocabId: String): SrsCard? =
        col.whereEqualTo("vocabId", vocabId).get().await().documents.firstOrNull()?.toCard()

    suspend fun saveOrUpdate(card: SrsCard) {
        val existing = findByVocab(card.vocabId)
        val docId = existing?.id ?: card.id
        col.document(docId).set(card.toMap()).await()
    }

    suspend fun deleteByDeck(deckId: String) {
        val batch = FirebaseFirestore.getInstance().batch()
        col.whereEqualTo("deckId", deckId).get().await().documents.forEach {
            batch.delete(it.reference)
        }
        batch.commit().await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toCard(): SrsCard? {
        if (!exists()) return null
        return SrsCard(
            id = id,
            vocabId = getString("vocabId") ?: return null,
            deckId = getString("deckId") ?: return null,
            interval = getLong("interval")?.toInt() ?: 1,
            easeFactor = getDouble("easeFactor")?.toFloat() ?: 2.5f,
            repetitions = getLong("repetitions")?.toInt() ?: 0,
            nextReviewDate = getString("nextReviewDate") ?: ""
        )
    }

    private fun SrsCard.toMap() = mapOf(
        "vocabId" to vocabId,
        "deckId" to deckId,
        "interval" to interval,
        "easeFactor" to easeFactor,
        "repetitions" to repetitions,
        "nextReviewDate" to nextReviewDate
    )
}