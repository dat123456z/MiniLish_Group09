package com.example.minlish.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.minlish.MainActivity
import com.example.minlish.R
import com.example.minlish.repository.SrsRepository
import com.example.minlish.repository.UserRepository
import com.example.minlish.util.SessionManager
import com.example.minlish.util.Sm2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_DAILY
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (type) {
                    TYPE_DAILY -> {
                        showDailyReminder(context)
                        ReminderScheduler.scheduleFromSavedSettings(context)
                    }
                    TYPE_DUE -> {
                        showDueReminder(context)
                        ReminderScheduler.scheduleDueReminder(context, 9, 0)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showDailyReminder(context: Context) {
        showNotification(
            context = context,
            notifId = NOTIF_ID_DAILY,
            title = "Đến giờ học từ vựng rồi! 📚",
            body = "Duy trì streak của bạn — học vài từ hôm nay nhé."
        )
    }

    private suspend fun showDueReminder(context: Context) {
        val userId = SessionManager.getUserId(context) ?: return
        val srsRepo = SrsRepository()
        val userRepo = UserRepository()
        val user = userRepo.findById(userId) ?: return

        val allDecks = com.example.minlish.repository.DeckRepository().getByOwner(user.id)
        var dueCount = 0
        allDecks.forEach { deck ->
            dueCount += srsRepo.getByDeck(deck.id).count { Sm2.isdue(it) }
        }

        if (dueCount == 0) return

        showNotification(
            context = context,
            notifId = NOTIF_ID_DUE,
            title = "Có $dueCount từ cần ôn tập ⏰",
            body = "Đừng để quên — ôn ngay để không mất streak!"
        )
    }

    private fun showNotification(
        context: Context,
        notifId: Int,
        title: String,
        body: String
    ) {
        InAppNotificationService.show(title, body)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc học từ vựng",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nhắc học hằng ngày và từ đến hạn ôn"
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(notifId, notification)
    }

    companion object {
        const val CHANNEL_ID = "minlish_reminder"
        const val EXTRA_TYPE = "type"
        const val TYPE_DAILY = "daily"
        const val TYPE_DUE = "due"
        const val NOTIF_ID_DAILY = 1001
        const val NOTIF_ID_DUE = 1002
    }
}