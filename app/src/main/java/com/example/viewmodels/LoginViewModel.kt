/*
 * ARCHIVO: LoginViewModel.kt  (capa VIEWMODEL de MVVM)
 * QUÉ HACE: maneja el formulario de inicio de sesión y guarda el usuario con sesión activa.
 * CÓMO: patrón "UI State": toda la pantalla se describe con un único objeto
 * (LoginUiState) expuesto como StateFlow de solo lectura. La vista lo observa y
 * llama a funciones del ViewModel ante cada acción del usuario.
 */
package com.example.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.data.AppDatabase
import com.example.repository.data.UsuarioRepository
import com.example.repository.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Estado completo de la pantalla de login. loggedUser != null significa sesión iniciada.
// Su "rol" lo usa AppNavigation para decidir la interfaz (admin o cliente).
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedUser: Usuario? = null // su rol decide si se muestra Dashboard o interfaz de cliente
)

// AndroidViewModel: igual que ViewModel pero recibe la Application (necesaria para
// abrir la base de datos). Sobrevive a rotaciones de pantalla.
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsuarioRepository(AppDatabase.getDatabase(application))

    // Patrón de encapsulamiento: _uiState (Mutable, privado) solo lo modifica el ViewModel;
    // uiState (StateFlow, público) es de solo lectura para la vista.
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // update { it.copy(...) }: crea una copia del estado cambiando un campo (inmutabilidad).
    // Además borra el error anterior para que desaparezca al volver a escribir.
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    // login:
    // 1) Valida que no haya campos vacíos.
    // 2) viewModelScope.launch inicia una corrutina (se cancela sola si el ViewModel muere).
    // 3) withContext(Dispatchers.IO) consulta la BD en un hilo de entrada/salida, para no
    //    congelar la interfaz.
    // 4) Actualiza el estado: usuario encontrado -> loggedUser; si no -> mensaje de error.
    // El email se normaliza (trim + lowercase) igual que al registrarse.
    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Ingrese email y contraseña") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(600) // pequeña pausa para mostrar el indicador de carga
            val usuario = withContext(Dispatchers.IO) {
                repository.login(state.email.trim().lowercase(), state.password)
            }
            _uiState.update {
                if (usuario != null) it.copy(isLoading = false, loggedUser = usuario, password = "")
                else it.copy(isLoading = false, error = "Credenciales inválidas")
            }
        }
    }

    // logout: vuelve al estado inicial (sin usuario). AppNavigation reacciona y va al Login.
    fun logout() {
        _uiState.value = LoginUiState()
    }
}
