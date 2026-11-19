/*
 * ARCHIVO: AccesoDao.kt  (capa DATA - DAO)
 * QUÉ HACE: operaciones CRUD sobre la tabla "accesos".
 * CÓMO: @Dao marca una interfaz; Room genera en compilación (KSP) la clase que
 * implementa cada método con el SQL indicado en @Query/@Insert/@Update/@Delete.
 * - Métodos que devuelven Flow: Room re-emite el resultado cada vez que la tabla cambia.
 * - Métodos "suspend": se ejecutan dentro de una corrutina, fuera del hilo principal.
 */
package com.example.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.repository.models.Acceso
import kotlinx.coroutines.flow.Flow


@Dao
interface AccesoDao {
    // READ reactivo: la UI se actualiza sola si la tabla cambia
    @Query("SELECT * FROM accesos ORDER BY nombreAcceso ASC")
    // Lista reactiva de accesos ordenados por nombre (la usan selectores y el Dashboard).
    fun getAll(): Flow<List<Acceso>>

    //Buscar por Id
    @Query("SELECT * FROM accesos WHERE id = :id")
    // Búsqueda puntual por id (devuelve null si no existe).
    suspend fun getProductById(id: Int): Acceso?

    //Buscar por nombre
    @Query("SELECT * FROM accesos WHERE nombreAcceso = :nombre_acceso")
    // Búsqueda por nombre (":nombre_acceso" se reemplaza por el parámetro).
    suspend fun getProductByName(nombre_acceso: String): Acceso?


    // CREATE
    // REPLACE: si ya existe un acceso con ese id, se sobrescribe.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcceso(acceso: Acceso): Long

    // UPDATE
    @Update
    suspend fun updateAcceso(acceso: Acceso)

    // DELETE
    @Delete
    suspend fun deleteAcceso(acceso: Acceso)

}
