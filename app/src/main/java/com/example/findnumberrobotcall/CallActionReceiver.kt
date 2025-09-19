package com.example.findnumberrobotcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.ai.client.generativeai.GenerativeModel
import com.example.findnumberrobotcall.BuildConfig // <-- Import corretto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CallActionReceiver : BroadcastReceiver() {

    companion object {
        // Leggi la chiave API in modo sicuro dalla classe BuildConfig.
        // Assicurati che il tuo file build.gradle.kts sia configurato correttamente.
        private val geminiApiKey = BuildConfig.GEMINI_API_KEY // <-- Riferimento corretto e sicuro
        private val generativeModel by lazy {
            GenerativeModel(
                modelName = "gemini-1.5-flash-latest",
                apiKey = geminiApiKey
            )
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()

        if (intent?.action == CallScreeningReceiver.ACTION_CHECK_GEMINI_SPAM) {
            val number = intent.getStringExtra("NUMBER_TO_CHECK")
            if (number != null && context != null) {
                Log.d("CallActionReceiver", "Avvio del controllo Gemini per il numero: $number")
                Toast.makeText(context, "Avvio del controllo Gemini...", Toast.LENGTH_SHORT).show()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val isSpam = askGemini(number)

                        withContext(Dispatchers.Main) {
                            val message = if (isSpam) {
                                "Gemini: Il numero è spam."
                            } else {
                                "Gemini: Il numero non è spam."
                            }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("CallActionReceiver", "Errore nella verifica con Gemini: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Errore: " + e.message, Toast.LENGTH_LONG).show()
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            } else {
                pendingResult.finish()
            }
        } else {
            pendingResult.finish()
        }
    }

    private suspend fun askGemini(phoneNumber: String): Boolean {
        // La chiave API viene già gestita in modo sicuro dal blocco companion object
        val prompt = "Il numero di telefono $phoneNumber è spam? Rispondi solo 'sì' o 'no'."
        val response = generativeModel.generateContent(prompt)
        val textResponse = response.text?.trim()?.lowercase()
        Log.d("GeminiApi", "Risposta Gemini: $textResponse")
        return textResponse?.contains("sì") == true
    }
}