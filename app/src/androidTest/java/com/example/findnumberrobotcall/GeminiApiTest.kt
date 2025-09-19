package com.example.findnumberrobotcall


import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GeminiApiTest {

    @Test
    fun testGeminiApiConnection() = runTest {
        // 1. Leggi la chiave API dal BuildConfig.
        // Assicurati di aver configurato build.gradle.kts e local.properties.
        val geminiApiKey = BuildConfig.GEMINI_API_KEY

        // 2. Verifica che la chiave non sia vuota
        assertFalse("La chiave API non deve essere vuota o non configurata.", geminiApiKey.isBlank())

        // 3. Inizializza il modello Gemini
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-latest",
            apiKey = geminiApiKey
        )

        // 4. Esegui una semplice richiesta API
        val prompt = "Rispondi a questa domanda: qual è la capitale dell'Italia?"
        val response = generativeModel.generateContent(prompt)

        // 5. Verifica che la risposta non sia nulla e che non sia vuota
        assertNotNull("La risposta dell'API non deve essere nulla.", response.text)
        assertFalse("La risposta dell'API non deve essere vuota.", response.text?.isBlank() ?: true)

        // Opzionale: stampa la risposta per vederla nel Logcat
        println("Risposta di Gemini: ${response.text}")
    }
}