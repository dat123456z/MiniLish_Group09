package com.example.minlish.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.minlish.util.SessionManager
import java.util.Calendar

object ReminderScheduler {

    private const val RC_DAILY = 2001
    private const val RC_DUE = 2002
    private const val RC_EMAIL = 2003

    fun scheduleDailyReminder(context: Context, hourOfDay: Int = 20, minute: Int = 0) {
        scheduleAlarm(context, RC_DAILY, StudyReminderReceiver.TYPE_DAILY, hourOfDay, minute)
    }

    fun scheduleDueReminder(context: Context, hourOfDay: Int = 9, minute: Int = 0) {
        scheduleAlarm(context, RC_DUE, StudyReminderReceiver.TYPE_DUE, hourOfDay, minute)
    }

    fun scheduleEmailReminder(context: Context, hourOfDay: Int = 9, minute: Int = 0) {
        scheduleAlarm(context, RC_EMAIL, StudyReminderReceiver.TYPE_EMAIL, hourOfDay, minute)
    }

    private fun scheduleAlarm(context: Context, requestCode: Int, type: String, hour: Int, minute: Int) {
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            putExtra(StudyReminderReceiver.EXTRA_TYPE, type)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val am = alarmManager(context)
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        } else {
            true
        }

        if (canScheduleExact) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } else {
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, 600_000, pendingIntent)
        }
    }

    fun cancelAll(context: Context) {
        val am = alarmManager(context)
        listOf(RC_DAILY, RC_DUE, RC_EMAIL).forEach { rc ->
            val pi = PendingIntent.getBroadcast(
                context, rc,
                Intent(context, StudyReminderReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { am.cancel(it) }
        }
    }

    fun scheduleFromSavedSettings(context: Context) {
        if (SessionManager.isReminderEnabled(context)) {
            scheduleDailyReminder(context, SessionManager.getReminderHour(context), SessionManager.getReminderMinute(context))
        }

        if (SessionManager.isEmailReminderEnabled(context)) {
            scheduleEmailReminder(context, SessionManager.getEmailReminderHour(context), SessionManager.getEmailReminderMinute(context))
        }

        scheduleDueReminder(context, 9, 0)
    }

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
