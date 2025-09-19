package com.example.findnumberrobotcall

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.ai.client.generativeai.GenerativeModel // Rimosso l'uso diretto, ma l'import rimane

class CallScreeningReceiver : CallScreeningService() {

    private val CHANNEL_ID = "CallScreeningChannel"
    private val NOTIFICATION_ID = 1

    companion object {
        const val ACTION_CHECK_GEMINI_SPAM = "com.example.findnumberrobotcall.ACTION_CHECK_GEMINI_SPAM"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifiche Intercettazione Chiamate"
            val descriptionText = "Mostra i numeri intercettati da chiamate in arrivo"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val direction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            callDetails.callDirection
        } else {
            TODO("VERSION.SDK_INT < Q")
        }

        if (direction == Call.Details.DIRECTION_INCOMING) {
            val incomingNumberUri = callDetails.handle
            val incomingNumber = incomingNumberUri?.schemeSpecificPart

            if (incomingNumber != null) {
                Log.d("CallScreening", "🎉 Numero Intercettato: $incomingNumber")
                // Invia la notifica che, se cliccata, avvierà il BroadcastReceiver.
                sendNotificationWithGeminiAction(incomingNumber)
            } else {
                Log.w("CallScreening", "Numero in arrivo sconosciuto/non disponibile.")
            }
        }
    }

    private fun sendNotificationWithGeminiAction(number: String) {
        val ACTION_BLOCK_NUMBER = "com.example.findnumberrobotcall.ACTION_BLOCK_NUMBER"
        val EXTRA_NUMBER_TO_BLOCK = "number_to_block"

        val blockIntent = Intent(ACTION_BLOCK_NUMBER).apply {
            putExtra(EXTRA_NUMBER_TO_BLOCK, number)
        }
        val blockPendingIntent = PendingIntent.getBroadcast(
            this,
            number.hashCode() + 1,
            blockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crea un Intent per l'azione "Gemini" che sarà gestito dal BroadcastReceiver
        val geminiIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = ACTION_CHECK_GEMINI_SPAM
            putExtra("NUMBER_TO_CHECK", number)
        }

        val geminiPendingIntent = PendingIntent.getBroadcast(
            this,
            number.hashCode(),
            geminiIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Chiamata in Arrivo")
            .setContentText("Chiama il numero: $number")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Gemini",
                geminiPendingIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Blocca Numero",
                blockPendingIntent
            )

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun sendSpamAlertNotification(number: String, reason: String) {
        // ... (Questa funzione rimane invariata, ma non è più chiamata in questo file)
    }
}