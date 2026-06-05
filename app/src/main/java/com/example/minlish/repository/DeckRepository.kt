package com.example.minlish.repository

import com.google.firebase.firestore.FirebaseFirestore

import com.example.minlish.model.Deck
import kotlinx.coroutines.tasks.await

class DeckRepository {

    private val col = FirebaseFirestore.getInstance().collection("decks")

    suspend fun getByOwner(ownerId: String): List<Deck> =
        col.whereEqualTo("ownerId", ownerId).get().await().documents.mapNotNull { it.toDeck() }

    suspend fun save(deck: Deck) {
        col.document(deck.id).set(deck.toMap()).await()
    }

    suspend fun update(deck: Deck) {
        col.document(deck.id).set(deck.toMap()).await()
    }

    suspend fun delete(deckId: String) {
        col.document(deckId).delete().await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toDeck(): Deck? {
        if (!exists()) return null
        return Deck(
            id = id,
            name = getString("name") ?: return null,
            description = getString("description") ?: "",
            tags = getString("tags") ?: "",
            createdAt = getString("createdAt") ?: "",
            ownerId = getString("ownerId") ?: return null,
            newWordsPerDay = getLong("newWordsPerDay")?.toInt() ?: 10,
            reviewsPerDay = getLong("reviewsPerDay")?.toInt() ?: 40
        )
    }

    private fun Deck.toMap() = mapOf(
        "name" to name,
        "description" to description,
        "tags" to tags,
        "createdAt" to createdAt,
        "ownerId" to ownerId,
        "newWordsPerDay" to newWordsPerDay,
        "reviewsPerDay" to reviewsPerDay
    )
}