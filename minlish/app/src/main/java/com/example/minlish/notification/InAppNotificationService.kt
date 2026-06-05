package com.example.minlish.notification

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class InAppNotification(
    val title: String,
    val body: String,
    val deckId: String? = null
)

object InAppNotificationService {

    private val _notificationEvents = MutableSharedFlow<InAppNotification>()
    val notificationEvents = _notificationEvents.asSharedFlow()

    private val SENDGRID_API_KEY = com.example.minlish.BuildConfig.SENDGRID_API_KEY
    private val SENDER_EMAIL = com.example.minlish.BuildConfig.SENDER_EMAIL

    suspend fun sendAutomatedEmail(toEmail: String, subject: String, body: String) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            val cleanBody = body.replace("\"", "'").replace("\n", " ")
            val cleanSubject = subject.replace("\"", "'")

            val htmlContent = """
                <div style='font-family: sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px; border-radius: 10px;'>
                    <h2 style='color: #4F6EF7;'>MinLish Learning</h2>
                    <p style='font-size: 16px; color: #333;'>$cleanBody</p>
                    <br>
                    <hr style='border: none; border-top: 1px solid #eee;'>
                    <p style='font-size: 12px; color: #999;'>
                        You received this email because you subscribed to daily reminders in the MinLish App.<br>
                        © 2024 MinLish Project.
                    </p>
                </div>
            """.trimIndent().replace("\n", "")

            val json = """
                {
                  "personalizations": [{
                      "to": [{ "email": "$toEmail" }],
                      "subject": "$cleanSubject"
                  }],
                  "from": { "email": "$SENDER_EMAIL", "name": "MinLish" },
                  "content": [{ "type": "text/html", "value": "$htmlContent" }]
                }
            """.trimIndent()

            val requestBody = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://api.sendgrid.com/v3/mail/send")
                .header("Authorization", "Bearer $SENDGRID_API_KEY")
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.d("MailService", "SILENT SUCCESS: Email sent to $toEmail")
                } else {
                    Log.e("MailService", "API ERROR: ${response.code} - ${response.body?.string()}")
                }
                response.close()
            } catch (e: Exception) {
                Log.e("MailService", "NETWORK FAILURE: ${e.message}")
            }
        }
    }

    suspend fun show(title: String, body: String, deckId: String? = null) {
        _notificationEvents.emit(InAppNotification(title, body, deckId))
    }
}
