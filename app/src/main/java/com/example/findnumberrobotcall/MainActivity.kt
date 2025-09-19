package com.example.findnumberrobotcall

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(::requestCallScreeningRole)
                }
            }
        }
    }

    /**
     * Richiede il ruolo di Call Screener all'utente, necessario per poter intercettare le chiamate.
     */
    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager

            // Verifica se l'app detiene già il ruolo
            if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                Toast.makeText(this, "Ruolo Call Screener già attivo. ✅", Toast.LENGTH_SHORT).show()
            } else {
                // Crea l'intent per richiedere il ruolo all'utente
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)

                // Avvia l'Activity per la richiesta del ruolo
                startActivityForResult(intent, REQUEST_ID_SET_CALL_SCREENER)
            }
        } else {
            Toast.makeText(this, "Questa funzionalità richiede Android 10 (Q) o superiore.", Toast.LENGTH_LONG).show()
        }
    }

    // Gestisce il risultato della richiesta di permesso
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ID_SET_CALL_SCREENER) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Permesso Call Screener CONCESSO! Ora l'app può intercettare le chiamate. ✅", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permesso Call Screener RIFIUTATO. L'app non funzionerà correttamente. ❌", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun MainScreen(onRequestRole: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Questa app ti avvisa con una notifica quando ricevi una chiamata. Per funzionare correttamente, devi concedere il permesso di 'Filtro Chiamate'.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(onClick = onRequestRole) {
            Text("Richiedi Permesso Call Screener")
        }
    }
}
