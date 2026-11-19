/*
 * ARCHIVO: FlujoViewModel.kt  (capa VIEWMODEL)
 * QUÉ HACE: lista, crea y elimina registros de flujo (pantallas "Registros" y
 * "Nuevo registro" del administrador).
 */
package com.example.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.data.AppDatabase
import com.example.repository.data.ConteoRepository
import com.example.repository.models.Acceso
import com.example.repository.models.RangoEtario
import com.example.repository.models.RegistroFlujo
import com.example.repository.models.TipoFlujo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lista y registro (crear / eliminar) de clasificaciones de flujo hechas por el personal. */
class FlujoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ConteoRepository(AppDatabase.getDatabase(application))

    // stateIn convierte el Flow de Room en un StateFlow (siempre tiene un valor actual):
    //  - viewModelScope: la recolección vive mientras viva el ViewModel.
    //  - WhileSubscribed(5000): deja de consultar Room 5 s después de que la pantalla
    //    deja de observar (ahorra recursos, pero sobrevive a una rotación rápida).
    //  - El último parámetro es el valor inicial mientras llega el primer dato.
    val accesos: StateFlow<List<Acceso>> = repository.accesos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // READ: últimos 30 días, reactivo
    val flujos: StateFlow<List<RegistroFlujo>> =
        repository.flujos(Periodo.TREINTA_DIAS.desdeMillis())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * CREATE. Valida y devuelve un mensaje de error, o null si todo está bien
     * (en ese caso el guardado ocurre en segundo plano).
     */
    // guardar: valida en el hilo principal y devuelve el error de inmediato (String) para
    // mostrarlo en el formulario. Si todo está bien devuelve null y guarda en segundo
    // plano (Dispatchers.IO). "?: return" sale de la función si la cantidad no es válida.
    fun guardar(accesoId: Int?, tipo: TipoFlujo, rango: RangoEtario, cantidadTexto: String, fotoUri: String?): String? {
        if (accesoId == null) return "Seleccione un acceso"
        val cantidad = Validaciones.cantidadValida(cantidadTexto)
            ?: return "Ingrese una cantidad entera entre 1 y ${Validaciones.MAX_CANTIDAD}"

        viewModelScope.launch(Dispatchers.IO) {
            repository.guardarFlujo(accesoId, tipo, rango, cantidad, fotoUri)
        }
        return null
    }

    // DELETE
    // eliminar: borra en hilo IO; la lista se actualiza sola porque es un Flow de Room.
    fun eliminar(registro: RegistroFlujo) {
        viewModelScope.launch(Dispatchers.IO) { repository.eliminarFlujo(registro) }
    }
}
