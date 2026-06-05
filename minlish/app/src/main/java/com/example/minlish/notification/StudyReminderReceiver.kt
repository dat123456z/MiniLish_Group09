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
import com.example.minlish.repository.DeckRepository
import com.example.minlish.repository.SrsRepository
import com.example.minlish.repository.UserRepository
import com.example.minlish.util.LocaleHelper
import com.example.minlish.util.SessionManager
import com.example.minlish.util.Sm2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val lang = SessionManager.getLanguage(context)
        val localizedContext = LocaleHelper.applyLocale(context, lang)

        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_DAILY
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (type) {
                    TYPE_DAILY -> {
                        showDailyReminder(localizedContext)
                    }
                    TYPE_EMAIL -> {
                        triggerEmailReminder(localizedContext)
                    }
                    TYPE_DUE -> {
                        showDueReminder(localizedContext)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun showDailyReminder(context: Context) {
        val userId = SessionManager.getUserId(context) ?: return
        val srsRepo = SrsRepository()
        val deckRepo = DeckRepository()
        
        val allDecks = deckRepo.getByOwner(userId)
        val deckIds = allDecks.map { it.id }
        
        val allDueCards = srsRepo.getByDecks(deckIds).filter { Sm2.isdue(it) }
        val dueCount = allDueCards.size

        val title: String
        val body: String
        val targetDeckId: String?

        if (dueCount > 0) {
            title = context.getString(R.string.sm2_due_reminder_title)
            body = context.getString(R.string.sm2_due_reminder_body_multiple, dueCount, allDecks.size)
            targetDeckId = "all"
        } else {
            title = context.getString(R.string.no_due_words_title)
            body = context.getString(R.string.no_due_words_body)
            targetDeckId = allDecks.firstOrNull()?.id
        }

        if (targetDeckId == null) {
            showNotification(
                context = context,
                notifId = NOTIF_ID_DAILY,
                title = context.getString(R.string.start_journey_title),
                body = context.getString(R.string.start_journey_body)
            )
            return
        }

        showNotification(
            context = context,
            notifId = NOTIF_ID_DAILY,
            title = title,
            body = body,
            deckId = targetDeckId,
            dueOnly = dueCount > 0
        )
    }

    private suspend fun triggerEmailReminder(context: Context) {
        val userId = SessionManager.getUserId(context) ?: "Learner"
        val userRepo = UserRepository()
        val user = userRepo.findById(userId)
        val email = user?.email ?: "alex@minlish.app"
        val name = user?.name ?: "Learner"

        InAppNotificationService.sendAutomatedEmail(
            toEmail = email,
            subject = context.getString(R.string.email_reminder_subject),
            body = context.getString(R.string.email_reminder_body, name)
        )
    }

    private suspend fun showDueReminder(context: Context) {
        val userId = SessionManager.getUserId(context) ?: return
        val srsRepo = SrsRepository()
        val deckRepo = DeckRepository()
        val userRepo = UserRepository()
        val user = userRepo.findById(userId) ?: return

        val allDecks = deckRepo.getByOwner(user.id)
        
        data class DeckDueData(val id: String, val name: String, val count: Int, val oldestDueDate: String)

        val deckDueList = allDecks.mapNotNull { deck ->
            val dueInDeck = srsRepo.getByDeck(deck.id).filter { Sm2.isdue(it) }
            if (dueInDeck.isNotEmpty()) {
                val oldestDueDate = dueInDeck.minOf { it.nextReviewDate }
                DeckDueData(deck.id, deck.name, dueInDeck.size, oldestDueDate)
            } else null
        }.sortedBy { it.oldestDueDate }

        if (deckDueList.isEmpty()) return

        val totalDueCount = deckDueList.sumOf { it.count }
        val deckCount = deckDueList.size
        
        // If cards are due across multiple decks, we use "all" to create a virtual deck session
        val targetDeckId = if (deckCount > 1) "all" else deckDueList.first().id

        val title = context.getString(R.string.sm2_due_reminder_title)
        val body = if (deckCount > 1) {
            context.getString(R.string.sm2_due_reminder_body_multiple, totalDueCount, deckCount)
        } else {
            context.getString(R.string.sm2_due_reminder_body_single, deckDueList.first().name, deckDueList.first().count)
        }

        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
        
        deckDueList.take(4).forEach { deckData ->
            style.addLine("• ${deckData.name}: ${deckData.count} " + context.getString(R.string.words).lowercase())
        }
        
        if (deckDueList.size > 4) {
            style.setSummaryText("… và ${deckDueList.size - 4} bộ thẻ khác")
        }

        showNotification(
            context = context,
            notifId = NOTIF_ID_DUE,
            title = title,
            body = body,
            style = style,
            deckId = targetDeckId,
            dueOnly = true
        )
    }

    private suspend fun showNotification(
        context: Context,
        notifId: Int,
        title: String,
        body: String,
        style: NotificationCompat.Style? = null,
        deckId: String? = null,
        dueOnly: Boolean = false
    ) {
        InAppNotificationService.show(title, body, deckId)

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
            if (deckId != null) {
                putExtra("deckId", deckId)
                putExtra("dueOnly", dueOnly)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notifId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        if (style != null) {
            builder.setStyle(style)
        }

        manager.notify(notifId, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "minlish_reminder"
        const val EXTRA_TYPE = "type"
        const val TYPE_DAILY = "daily"
        const val TYPE_DUE = "due"
        const val TYPE_EMAIL = "email"
        const val NOTIF_ID_DAILY = 1001
        const val NOTIF_ID_DUE = 1002
    }
}
