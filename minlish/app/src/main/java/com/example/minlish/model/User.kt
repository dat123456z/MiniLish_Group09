package com.example.minlish.model

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val passwordHash: String,
    val level: String = "A1",
    val goal: String = "",
    val streak: Int = 0,
    val lastStudyDate: String = ""
)