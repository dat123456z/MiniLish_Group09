package com.example.minlish.model

import java.util.UUID

data class SrsCard(
    val id: String = UUID.randomUUID().toString(),
    val vocabId: String,
    val deckId: String,
    val interval: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0,
    val nextReviewDate: String = ""
)