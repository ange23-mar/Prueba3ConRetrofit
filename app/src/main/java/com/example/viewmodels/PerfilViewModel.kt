/*
 * ARCHIVO: PerfilViewModel.kt  (capa VIEWMODEL)
 * QUÉ HACE: entrega los datos del usuario con sesión activa y permite actualizar su
 * foto de perfil y su tipo de cliente / rango etario.
 * CÓMO: guarda el id de la sesión en un MutableStateFlow; flatMapLatest lo transforma
 * en una consulta reactiva a Room para ESE usuario.
 */
package com.example.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.data.AppDatabase
import com.example.repository.data.UsuarioRepository
import com.example.repository.models.RangoEtario
import com.example.repository.models.TipoFlujo
import com.example.repository.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Perfil del usuario con sesión iniciada: datos básicos, foto (CameraX) y tipo de cliente. */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsuarioRepository(AppDatabase.getDatabase(application))

    // usuarioId: lo fija AppNavigation al iniciar sesión (y lo vuelve null al cerrarla).
    private val usuarioId = MutableStateFlow<Int?>(null)

    // Se vuelve a consultar Room cada vez que cambia la sesión; Room avisa cuando cambia la fila
    // flatMapLatest: cada vez que cambia el id, cancela la consulta anterior y abre una nueva.
    // Si el id es null emite null (flowOf(null)), si no, observa la fila del usuario.
    // stateIn convierte el Flow de Room en un StateFlow (siempre tiene un valor actual):
    //  - viewModelScope: la recolección vive mientras viva el ViewModel.
    //  - WhileSubscribed(5000): deja de consultar Room 5 s después de que la pantalla
    //    deja de observar (ahorra recursos, pero sobrevive a una rotación rápida).
    //  - El último parámetro es el valor inicial mientras llega el primer dato.
    val usuario: StateFlow<Usuario?> = usuarioId
        .flatMapLatest { id -> if (id == null) flowOf(null) else repository.observar(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun cerrarMensaje() { _mensaje.value = null }

    /** Lo llama AppNavigation al iniciar/cerrar sesión. */
    // setUsuario: cambia el usuario observado (llamado desde un LaunchedEffect).
    fun setUsuario(id: Int?) {
        usuarioId.value = id
    }

    /** Guarda la nueva foto y borra el archivo anterior para no acumular imágenes. */
    // actualizarFoto: lee la ruta anterior, guarda la nueva y borra el archivo viejo,
    // todo en hilo IO. Room avisa del cambio y el avatar se redibuja (con su animación).
    fun actualizarFoto(nuevaUri: String) {
        val id = usuarioId.value ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val anterior = repository.obtener(id)?.fotoPerfilUri
                repository.actualizarFoto(id, nuevaUri)
                borrarArchivoLocal(anterior)
            }
            _mensaje.value = "Foto de perfil actualizada"
        }
    }

    // actualizarPerfilCliente: lee el usuario actual y guarda una COPIA con los nuevos
    // valores (copy mantiene el resto de los campos sin cambios).
    fun actualizarPerfilCliente(tipoCliente: TipoFlujo, rangoEtario: RangoEtario) {
        val id = usuarioId.value ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.obtener(id)?.let { actual ->
                    repository.actualizar(actual.copy(tipoCliente = tipoCliente, rangoEtario = rangoEtario))
                }
            }
            _mensaje.value = "Perfil actualizado"
        }
    }

    // Solo borra archivos dentro del almacenamiento interno privado de la app
    // borrarArchivoLocal: convierte la URI "file://..." en ruta, y por seguridad solo borra
    // si el archivo está dentro de filesDir (almacenamiento privado de la app).
    private fun borrarArchivoLocal(uri: String?) {
        val path = uri?.let { Uri.parse(it).path } ?: return
        val archivo = File(path)
        if (archivo.absolutePath.startsWith(getApplication<Application>().filesDir.absolutePath)) {
            archivo.delete()
        }
    }
}
