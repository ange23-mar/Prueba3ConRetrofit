/*
 * ARCHIVO: LoginScreen.kt  (capa VIEW - pantalla)
 * QUÉ HACE: formulario de inicio de sesión ÚNICO para administradores y clientes.
 * CÓMO: la pantalla no decide nada: muestra el estado del LoginViewModel y le avisa
 * cada acción. Cuando hay usuario logueado, entrega ese usuario a AppNavegation
 * (onLoginSuccess), que elige la interfaz según su rol.
 */
package com.example.view.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.repository.models.Usuario
import com.example.view.utils.AnimacionEntradaFormulario
import com.example.viewmodels.LoginViewModel

/** Inicio de sesión único: el rol del usuario decide a qué interfaz se navega. */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (Usuario) -> Unit,
    onRegistrarse: () -> Unit
) {
    // collectAsStateWithLifecycle: convierte el StateFlow del ViewModel en estado de Compose.
    // Cada vez que cambia, la pantalla se "recompone" (se redibuja) con los datos nuevos.
    // "WithLifecycle": deja de observar si la app pasa a segundo plano (ahorra batería).
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // visible: activa la animación de entrada. rememberSaveable conserva el valor incluso
    // si se rota la pantalla (remember solo sobrevive a recomposiciones).
    var visible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    // LaunchedEffect(clave): ejecuta el bloque cuando la clave cambia. Si loggedUser deja
    // de ser null, se llama a onLoginSuccess(usuario) -> AppNavegation navega.
    LaunchedEffect(state.loggedUser) { state.loggedUser?.let(onLoginSuccess) }

    // Todo el formulario aparece con fade + deslizamiento (Animation.kt).
    // verticalScroll permite desplazar si el teclado tapa los campos.
    AnimacionEntradaFormulario(visible = visible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(88.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("Tienda Rincón Sur", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Ingresa como administrador o cliente",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                // Campo controlado: muestra el valor del estado y, al escribir, llama a
                // onEmailChange (referencia a función con "::"). El ViewModel actualiza el estado.
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                // PasswordVisualTransformation oculta los caracteres con puntos.
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ?.let: solo dibuja el error si no es null.
            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                // Botón deshabilitado mientras carga; muestra un spinner en vez del texto.
                onClick = viewModel::login,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Ingresar")
                }
            }
            // Navega al formulario de registro de clientes.
            TextButton(onClick = onRegistrarse) {
                Text("¿Eres cliente y no tienes cuenta? Regístrate")
            }

            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Usuarios de prueba",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Administrador: admin@rinconsur.cl / admin123",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Cliente: cliente@rinconsur.cl / cliente123",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
