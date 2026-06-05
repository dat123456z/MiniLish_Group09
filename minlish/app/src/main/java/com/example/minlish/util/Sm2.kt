package com.example.minlish.util

import com.example.minlish.model.SrsCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Rating(val quality: Int) {
    Again(0),
    Hard(2),
    Good(4),
    Easy(5)
}

object Sm2 {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun today(): String = dateFormat.format(Date())

//    fun isdue(card: SrsCard): Boolean {
//        if (card.nextReviewDate.isBlank()) return true
//        return try {
//            val review = dateFormat.parse(card.nextReviewDate) ?: return true
//            !Date().before(review)
//        } catch (e: Exception) { true }
//    }

    fun isdue(card: SrsCard): Boolean {
        if (card.nextReviewDate.isBlank()) return true
        return try {
            val review = dateFormat.parse(card.nextReviewDate) ?: return true

            // DÒNG TEST: Cộng thêm 1 ngày vào thời điểm hiện tại để kiểm tra
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val fakeToday = calendar.time

            !fakeToday.before(review)
        } catch (e: Exception) { true }
    }


    fun review(card: SrsCard, rating: Rating): SrsCard {
        val q = rating.quality

        val newEF = (card.easeFactor + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)))
            .coerceAtLeast(1.3f)

        val (newInterval, newReps) = when {
            q < 3 -> Pair(1, 0)
            card.repetitions == 0 -> Pair(1, 1)
            card.repetitions == 1 -> Pair(6, 2)
            else -> Pair((card.interval * newEF).toInt().coerceAtLeast(1), card.repetitions + 1)
        }

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, newInterval)
        val nextDate = dateFormat.format(cal.time)

        return card.copy(
            interval = newInterval,
            easeFactor = newEF,
            repetitions = newReps,
            nextReviewDate = nextDate
        )
    }
}