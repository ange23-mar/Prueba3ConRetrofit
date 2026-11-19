/*
 * ARCHIVO: UsuarioRepository.kt  (capa DATA - Repository)
 * QUÉ HACE: punto único de acceso a los datos de usuarios para los ViewModels
 * (LoginViewModel, UsuarioViewModel, PerfilViewModel).
 * CÓMO: patrón Repository. Envuelve al UsuarioDao; el ViewModel no sabe si los datos
 * vienen de Room, de una API o de otro lado. Hoy solo delega en el DAO, pero si
 * mañana se agrega un servidor, solo se cambia esta clase.
 */
package com.example.repository.data

import com.example.repository.models.Usuario
import kotlinx.coroutines.flow.Flow

/** Fuente de datos de usuarios (login, registro de clientes y perfil). */
class UsuarioRepository(db: AppDatabase) {

    // Obtiene el DAO desde la instancia única de la base de datos.
    private val usuarioDao = db.usuarioDao()

    suspend fun login(email: String, password: String): Usuario? = usuarioDao.login(email, password)

    // Flow reactivo del usuario (perfil y menú lateral).
    fun observar(id: Int): Flow<Usuario?> = usuarioDao.observarPorId(id)

    suspend fun obtener(id: Int): Usuario? = usuarioDao.getById(id)

    /** Lanza SQLiteConstraintException si el email ya existe. */
    // Inserta un usuario nuevo; si el email ya existe, la excepción sube al ViewModel.
    suspend fun crear(usuario: Usuario): Long = usuarioDao.insert(usuario)

    suspend fun actualizar(usuario: Usuario) = usuarioDao.update(usuario)

    suspend fun actualizarFoto(id: Int, uri: String?) = usuarioDao.actualizarFoto(id, uri)
}
