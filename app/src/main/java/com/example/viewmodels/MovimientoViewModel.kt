/*
 * ARCHIVO: MovimientoViewModel.kt  (capa VIEWMODEL)
 * QUÉ HACE: lógica de la pantalla del cliente: estado dentro/fuera, historial y
 * registro de Ingreso/Salida con su motivo.
 * CÓMO: combina varios Flows (accesos, historial, procesando, aviso) en un único
 * MovimientoUiState que la pantalla observa.
 */
package com.example.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.data.AppDatabase
import com.example.repository.data.ConteoRepository
import com.example.repository.models.Acceso
import com.example.repository.models.MovimientoCliente
import com.example.repository.models.TipoFlujo
import com.example.repository.models.TipoMovimiento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Aviso: mensaje para el usuario; esError decide si se pinta en rojo o en color primario.
data class Aviso(val texto: String, val esError: Boolean)

// Estado de la pantalla. "ultimo" y "dentro" son propiedades CALCULADAS (get()):
// se derivan del historial, así nunca quedan desincronizadas.
data class MovimientoUiState(
    val accesos: List<Acceso> = emptyList(),
    val historial: List<MovimientoCliente> = emptyList(),
    val procesando: Boolean = false,
    val aviso: Aviso? = null
) {
    /** El historial viene ordenado del más reciente al más antiguo. */
    val ultimo: MovimientoCliente? get() = historial.firstOrNull()

    /** El cliente está dentro si su último movimiento fue un INGRESO. */
    val dentro: Boolean get() = ultimo?.tipoMovimiento == TipoMovimiento.INGRESO
}

/** Registro de Ingreso / Salida del cliente con sesión iniciada. */
@OptIn(ExperimentalCoroutinesApi::class)
class MovimientoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ConteoRepository(AppDatabase.getDatabase(application))

    // Estados internos: usuario con sesión, indicador de proceso y aviso actual.
    private val usuarioId = MutableStateFlow<Int?>(null)
    private val procesando = MutableStateFlow(false)
    private val aviso = MutableStateFlow<Aviso?>(null)

    // historial: se vuelve a consultar Room cuando cambia el usuario de la sesión.
    private val historial = usuarioId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.historialCliente(id)
    }

    // combine: cada vez que CUALQUIERA de los 4 flows emite, se construye un nuevo UiState.
    // stateIn convierte el Flow de Room en un StateFlow (siempre tiene un valor actual):
    //  - viewModelScope: la recolección vive mientras viva el ViewModel.
    //  - WhileSubscribed(5000): deja de consultar Room 5 s después de que la pantalla
    //    deja de observar (ahorra recursos, pero sobrevive a una rotación rápida).
    //  - El último parámetro es el valor inicial mientras llega el primer dato.
    val uiState: StateFlow<MovimientoUiState> =
        combine(repository.accesos(), historial, procesando, aviso) { accesos, hist, p, a ->
            MovimientoUiState(accesos = accesos, historial = hist, procesando = p, aviso = a)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MovimientoUiState())

    /** Lo llama AppNavigation al iniciar/cerrar sesión. */
    fun setUsuario(id: Int?) {
        usuarioId.value = id
        aviso.value = null
    }

    // registrar:
    // 1) Validaciones rápidas en la vista: sesión, acceso y motivo seleccionados.
    //    "?: run { ...; return }" sale de la función si falta algún dato.
    // 2) Corrutina: marca procesando = true (los botones se deshabilitan).
    // 3) El repositorio valida la secuencia y guarda en una transacción (hilo IO).
    // 4) Muestra el resultado: error (texto devuelto) o confirmación (null = éxito).
    fun registrar(tipo: TipoMovimiento, accesoId: Int?, motivo: TipoFlujo?) {
        val id = usuarioId.value ?: run { aviso.value = Aviso("Sesión no válida", true); return }
        if (accesoId == null) { aviso.value = Aviso("Seleccione el acceso", true); return }
        if (motivo == null) { aviso.value = Aviso("Seleccione el motivo: Solo transita o Compra", true); return }

        viewModelScope.launch {
            procesando.value = true
            val error = withContext(Dispatchers.IO) {
                repository.registrarMovimiento(id, accesoId, tipo, motivo)
            }
            aviso.value = if (error != null) Aviso(error, true)
            else Aviso("${tipo.etiqueta} registrado · ${motivo.etiqueta}", false)
            procesando.value = false
        }
    }

    fun cerrarAviso() { aviso.value = null }
}
