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


class CallScreeningReceiver : CallScreeningService() {

    private val CHANNEL_ID = "CallScreeningChannel"
    private val NOTIFICATION_ID = 1

    // Questo metodo viene eseguito quando il servizio viene creato.
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

                //searchNumberOnGoogle(this,incomingNumber)
                // Invece di bloccare, invia una notifica!
                sendNumberNotification(incomingNumber)



            } else {
                Log.w("CallScreening", "Numero in arrivo sconosciuto/non disponibile.")
            }

            // IMPORTANTE: Rimuovi la riga 'respondToCall(callDetails, defaultResponse)'.
            // Senza di essa, il sistema gestirà la chiamata normalmente, facendola squillare.
        }
    }

    /**
     * Avvia il browser predefinito del dispositivo Android per eseguire una ricerca su Google
     * con il numero di telefono specificato.
     * * @param context Il contesto dell'applicazione (es. Activity).
     * @param phoneNumber Il numero di telefono da cercare.
     */
    fun searchNumberOnGoogle(context: Context, phoneNumber: String?) {

        // 1. Controllo base del numero
        if (phoneNumber.isNullOrBlank()) {
            Toast.makeText(context, "Il numero di telefono non è valido.", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Prepara la query di ricerca.
        // Si consiglia di codificare la stringa per URL per gestire spazi o caratteri speciali.
        val query = "spam ${phoneNumber}"
        val encodedQuery = Uri.encode(query)

        // 3. Costruisci l'URI di ricerca di Google.
        // Questa è l'URL standard di ricerca.
        val uri = Uri.parse("https://www.google.com/search?q=$encodedQuery")

        // 4. Crea l'Intent per aprire il browser.
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            // 5. Avvia l'Activity (il browser)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Gestione degli errori, nel caso non ci sia un'app per gestire l'Intent (molto raro)
            Toast.makeText(context, "Impossibile aprire il browser per la ricerca.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    /*private fun sendNumberNotification(number: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Sostituisci con un'icona appropriata
            .setContentTitle("Chiamata in Arrivo")
            .setContentText("Numero intercettato: $number")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL) // Categoria per le chiamate

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }*/


    private fun sendNumberNotification(number: String) {

        // 1. Prepara l'Intent per la ricerca
        val query = "spam $number"
        val encodedQuery = Uri.encode(query)
        val searchUri = Uri.parse("https://www.google.com/search?q=$encodedQuery")
        val searchIntent = Intent(Intent.ACTION_VIEW, searchUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Necessario anche qui per coerenza
        }

        // 2. Crea il PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            number.hashCode(), // Usa un ID unico basato sul numero
            searchIntent,
            // FLAG_IMMUTABLE è richiesto da Android S (API 31) in poi
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Costruisci la notifica
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Chiamata da Verificare")
            .setContentText("Chiama il numero: $number")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)

            // 4. Aggiungi il pulsante 'Cerca Spam'
            .addAction(
                R.drawable.ic_launcher_foreground, // Assicurati di avere un'icona chiamata ic_search
                "Cerca Spam",
                pendingIntent
            )

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}