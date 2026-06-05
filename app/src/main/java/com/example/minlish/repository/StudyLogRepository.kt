package com.example.minlish.repository

import com.google.firebase.firestore.FirebaseFirestore

import com.example.minlish.model.StudyLog
import kotlinx.coroutines.tasks.await

class StudyLogRepository {

    private val col = FirebaseFirestore.getInstance().collection("study_logs")

    suspend fun getByUser(userId: String): List<StudyLog> =
        col.whereEqualTo("userId", userId).get().await().documents.mapNotNull { it.toLog() }

    suspend fun findByUserAndDate(userId: String, date: String): StudyLog? =
        col.whereEqualTo("userId", userId).whereEqualTo("date", date)
            .get().await().documents.firstOrNull()?.toLog()

    suspend fun saveOrUpdate(log: StudyLog) {
        val existing = findByUserAndDate(log.userId, log.date)
        val docId = existing?.id ?: log.id
        col.document(docId).set(log.toMap()).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toLog(): StudyLog? {
        if (!exists()) return null
        return StudyLog(
            id = id,
            userId = getString("userId") ?: return null,
            date = getString("date") ?: return null,
            wordStudied = getLong("wordStudied")?.toInt() ?: 0,
            wordReviewed = getLong("wordReviewed")?.toInt() ?: 0,
            correctCount = getLong("correctCount")?.toInt() ?: 0,
            totalCount = getLong("totalCount")?.toInt() ?: 0
        )
    }

    private fun StudyLog.toMap() = mapOf(
        "userId" to userId,
        "date" to date,
        "wordStudied" to wordStudied,
        "wordReviewed" to wordReviewed,
        "correctCount" to correctCount,
        "totalCount" to totalCount
    )
}