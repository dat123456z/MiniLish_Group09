package com.example.minlish.model

import java.util.UUID

data class Vocabulary(
    val id: String = UUID.randomUUID().toString(),
    val word: String,
    val pronunciation: String = "",
    val meaning: String,
    val description: String = "",
    val example: String = "",
    val collocation: String = "",
    val relatedWords: String = "",
    val note: String = "",
    val deckId: String
)