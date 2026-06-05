package com.example.minlish.model

import java.util.UUID

data class Deck(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val tags: String = "",
    val createdAt: String = "",
    val ownerId: String,
    val newWordsPerDay: Int = 10,
    val reviewsPerDay: Int = 40
)
