package com.example.findnumberrobotcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.BlockedNumberContract
import android.util.Log
import android.widget.Toast
import android.net.Uri
import android.content.ContentValues
import android.telephony.PhoneNumberUtils.normalizeNumber


const val ACTION_BLOCK_NUMBER = "com.example.findnumberrobotcall.ACTION_BLOCK_NUMBER"
const val EXTRA_NUMBER_TO_BLOCK = "number_to_block"


class BlockNumberReceiver : BroadcastReceiver() {

    // Definisci queste costanti esattamente come sono nella funzione sendStandardNotification

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Verifica che l'Intent sia quello corretto
        if (intent.action == ACTION_BLOCK_NUMBER) {

            // 2. Estrai il numero dal campo extra
            val numberToBlock = intent.getStringExtra(EXTRA_NUMBER_TO_BLOCK)

            // 3. Esegui la logica di blocco
            if (numberToBlock != null) {
                // Chiamata alla funzione di blocco effettiva (vedi Sezione 3)
                blockNumber(context, numberToBlock)
            } else {
                Log.e("BlockReceiver", "Numero da bloccare non trovato nell'Intent.")
            }
        }
    }

    /**
     * Funzione fittizia per simulare l'azione di blocco.
     * La logica REALE di blocco andrebbe qui.
     */
    fun blockNumber(context: Context, number: String): Boolean {

        // 1. Verifica della Versione API
        // Il Content Provider BlockedNumberContract è disponibile solo da Android 7.0 (API 24)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w("Blocco", "Blocco di sistema non supportato su API < 24.")
            return false
        }

        // 2. Preparazione dei dati (ContentValues)
        val contentValues = ContentValues().apply {
            // La colonna REQUIRED per il numero originale
            put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)

            // Android gestirà la normalizzazione E164, ma è buona pratica fornirlo se possibile
            //put(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER, normalizeNumber(number))
        }

        // 3. Esecuzione dell'Azione di Inserimento
        try {
            val contentResolver = context.contentResolver

            // Chiama il metodo insert()
            val resultUri: Uri? = contentResolver.insert(
                BlockedNumberContract.BlockedNumbers.CONTENT_URI, // L'URI target per la tabella dei numeri bloccati
                contentValues
            )

            // 4. Gestione del Risultato
            if (resultUri != null) {
                // Notifica l'utente del successo
                Toast.makeText(context, "Numero $number bloccato.", Toast.LENGTH_SHORT).show()
                return true
            } else {
                Toast.makeText(context, "Blocco Fallimento nell'inserimento del numero: URI nullo.",Toast.LENGTH_SHORT).show()
                return false
            }

        } catch (e: SecurityException) {
            // Questa è l'eccezione più comune se l'app non è l'app predefinita per l'ID chiamante/Spam
            Toast.makeText(context,"Blocco SecurityException: L'app non ha i permessi per scrivere nella lista bloccata.",Toast.LENGTH_SHORT).show()
            // NOTA: L'app DEVE essere il dialer predefinito o avere un ruolo privilegiato.
            return false
        } catch (e: Exception) {
            Log.e("Blocco", "Errore generico durante il blocco: ${e.message}")
            return false
        }

    }
}