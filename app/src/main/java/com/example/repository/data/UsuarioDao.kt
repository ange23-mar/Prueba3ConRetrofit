/*
 * ARCHIVO: UsuarioDao.kt  (capa DATA - DAO)
 * QUÉ HACE: consultas de la tabla "usuarios": login, lectura, alta y actualización.
 * CÓMO: Room genera la implementación a partir de las anotaciones.
 */
package com.example.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.repository.models.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    // login: busca un usuario con ese email Y esa contraseña. LIMIT 1 asegura
    // un solo resultado. Si no coincide, devuelve null (credenciales inválidas).
    // LOGIN
    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): Usuario?

    // READ reactivo: el perfil y el menú se actualizan solos (ej. al cambiar la foto)
    @Query("SELECT * FROM usuarios WHERE id = :id")
    // observarPorId: Flow que emite el usuario cada vez que su fila cambia.
    // Así, al actualizar la foto, el perfil y el menú lateral se redibujan solos.
    fun observarPorId(id: Int): Flow<Usuario?>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    // getById: lectura única (no reactiva), usada dentro de transacciones.
    suspend fun getById(id: Int): Usuario?

    // CREATE (ABORT: el email es único)
    // ABORT: si el email ya existe (índice único), lanza excepción en vez de sobrescribir.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(usuario: Usuario): Long

    // UPDATE
    @Update
    suspend fun update(usuario: Usuario)

    // Actualiza SOLO la columna de la foto (más eficiente que reescribir todo el usuario).
    @Query("UPDATE usuarios SET fotoPerfilUri = :uri WHERE id = :id")
    suspend fun actualizarFoto(id: Int, uri: String?)
}
