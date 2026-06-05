package com.example.minlish.repository

import com.google.firebase.firestore.FirebaseFirestore

import com.example.minlish.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val col = FirebaseFirestore.getInstance().collection("users")

    suspend fun findById(id: String): User? =
        col.document(id).get().await().toUser()

    suspend fun findByEmail(email: String): User? =
        col.whereEqualTo("email", email).get().await().documents.firstOrNull()?.toUser()

    suspend fun save(user: User) {
        col.document(user.id).set(user.toMap()).await()
    }

    suspend fun update(user: User) {
        col.document(user.id).set(user.toMap()).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        if (!exists()) return null
        return User(
            id = id,
            name = getString("name") ?: return null,
            email = getString("email") ?: return null,
            passwordHash = getString("passwordHash") ?: "",
            level = getString("level") ?: "A1",
            goal = getString("goal") ?: "",
            streak = getLong("streak")?.toInt() ?: 0,
            lastStudyDate = getString("lastStudyDate") ?: ""
        )
    }

    private fun User.toMap() = mapOf(
        "name" to name,
        "email" to email,
        "passwordHash" to passwordHash,
        "level" to level,
        "goal" to goal,
        "streak" to streak,
        "lastStudyDate" to lastStudyDate
    )
}