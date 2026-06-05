package com.example.minlish.model

import java.util.UUID

data class StudyLog(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val date: String,
    val wordStudied: Int = 0,
    val wordReviewed: Int = 0,
    val correctCount: Int = 0,
    val totalCount: Int = 0
)