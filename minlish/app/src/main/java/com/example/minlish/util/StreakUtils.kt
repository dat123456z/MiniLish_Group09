package com.example.minlish.util

import com.example.minlish.model.StudyLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object StreakUtils {

    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun today(): String = fmt.format(Date())

    fun yesterday(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return fmt.format(cal.time)
    }

    fun computeStreak(logs: List<StudyLog>): Int {
        if (logs.isEmpty()) return 0
        val dates = logs.map { it.date }.toSortedSet().toList().sortedDescending()
        val todayStr = today()
        val yesterdayStr = yesterday()
        if (dates.first() != todayStr && dates.first() != yesterdayStr) return 0

        var streak = 0
        val cal = Calendar.getInstance()
        if (dates.first() == todayStr) {
            streak = 1
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            streak = 1
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        for (i in 1 until dates.size) {
            val expected = fmt.format(cal.time)
            if (dates[i] == expected) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun accuracyPercent(logs: List<StudyLog>): Int {
        val total = logs.sumOf { it.totalCount }
        val correct = logs.sumOf { it.correctCount }
        if (total == 0) return 0
        return ((correct.toFloat() / total) * 100).toInt()
    }

    fun last7Days(): List<String> {
        val cal = Calendar.getInstance()
        return (6 downTo 0).map {
            cal.time.let { fmt.format(it) }.also { cal.add(Calendar.DAY_OF_YEAR, -1) }
        }.reversed()
    }
}