package com.example.minlish.repository

import com.google.firebase.firestore.FirebaseFirestore

import com.example.minlish.model.Vocabulary
import kotlinx.coroutines.tasks.await

class VocabularyRepository {

    private val col = FirebaseFirestore.getInstance().collection("vocabularies")

    suspend fun getByDeck(deckId: String): List<Vocabulary> =
        col.whereEqualTo("deckId", deckId).get().await().documents.mapNotNull { it.toVocab() }

    suspend fun save(vocab: Vocabulary) {
        col.document(vocab.id).set(vocab.toMap()).await()
    }

    suspend fun update(vocab: Vocabulary) {
        col.document(vocab.id).set(vocab.toMap()).await()
    }

    suspend fun delete(vocabId: String) {
        col.document(vocabId).delete().await()
    }

    suspend fun deleteByDeck(deckId: String) {
        val batch = FirebaseFirestore.getInstance().batch()
        col.whereEqualTo("deckId", deckId).get().await().documents.forEach {
            batch.delete(it.reference)
        }
        batch.commit().await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toVocab(): Vocabulary? {
        if (!exists()) return null
        return Vocabulary(
            id = id,
            word = getString("word") ?: return null,
            pronunciation = getString("pronunciation") ?: "",
            meaning = getString("meaning") ?: return null,
            description = getString("description") ?: "",
            example = getString("example") ?: "",
            collocation = getString("collocation") ?: "",
            relatedWords = getString("relatedWords") ?: "",
            note = getString("note") ?: "",
            deckId = getString("deckId") ?: return null
        )
    }

    private fun Vocabulary.toMap() = mapOf(
        "word" to word,
        "pronunciation" to pronunciation,
        "meaning" to meaning,
        "description" to description,
        "example" to example,
        "collocation" to collocation,
        "relatedWords" to relatedWords,
        "note" to note,
        "deckId" to deckId
    )
}