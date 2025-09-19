package com.example.findnumberrobotcall

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private val REQUEST_ID_SET_CALL_SCREENER = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Assumi che tu abbia un tema di Compose, come MyTheme
            // Sostituisci MyTheme con il nome del tuo tema se è diverso.
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(::requestCallScreeningRole)
                }
            }
        }

        checkCallScreeningRole()
    }

    private fun checkCallScreeningRole() {
        if (isCallScreeningRoleHeld()) {
            Toast.makeText(this, "Ruolo Call Screener già attivo.", Toast.LENGTH_SHORT).show()
        }
    }

    // Verifica se l'app è impostata come Call Screener
    private fun isCallScreeningRoleHeld(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        }
        return false
    }

    // Richiede il ruolo di Call Screener all'utente
    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!isCallScreeningRoleHeld()) {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)

                startActivityForResult(intent, REQUEST_ID_SET_CALL_SCREENER)
            } else {
                Toast.makeText(this, "Ruolo Call Screener già attivo.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Il CallScreeningService richiede Android 10 (Q) o superiore.", Toast.LENGTH_LONG).show()
        }
    }

    // Gestisce il risultato della richiesta di permesso
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ID_SET_CALL_SCREENER) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Permesso Call Screener CONCESSO! ✅", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permesso Call Screener RIFIUTATO. ❌", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// NUOVA FUNZIONE COMPOSABLE AGGIORNATA
@Composable
fun MainScreen(onRequestRole: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Centra verticalmente gli elementi
    ) {
        // Testo di aiuto/descrizione
        Text(
            text = "Questa app ti avvisa con una notifica quando ricevi una chiamata, mostrando immediatamente il numero del chiamante. " +
                    "Per funzionare, devi concedere il permesso di 'Filtro Chiamate'. L'app NON bloccherà o silenzierà le chiamate.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Pulsante per richiedere il permesso
        Button(onClick = onRequestRole) {
            Text("Richiedi Permesso Call Screener")
        }
    }
}