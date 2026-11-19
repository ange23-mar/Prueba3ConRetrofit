/*
 * ARCHIVO: RegistroConteoDao.kt  (capa DATA - DAO)
 * QUÉ HACE: lee y escribe la tabla "registro_conteo" (total de personas que entran).
 */
package com.example.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repository.models.RegistroConteo
import com.example.repository.models.TotalAcceso
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroConteoDao {
    // Total de ingresos por acceso desde una fecha (el "conteo agregado" del sistema actual)
    @Query(
        """
        SELECT accesoId, SUM(cantidad) AS total
        FROM registro_conteo
        WHERE fechaHora >= :desde
        GROUP BY accesoId
        """
    )
    // totalesPorAcceso: suma "cantidad" por acceso desde una fecha (GROUP BY accesoId).
    // Devuelve Flow: el Dashboard se actualiza solo cuando un cliente marca su ingreso.
    fun totalesPorAcceso(desde: Long): Flow<List<TotalAcceso>>

    @Insert
    // insert: un registro (lo usa el ingreso de un cliente: cantidad = 1).
    suspend fun insert(registro: RegistroConteo): Long

    @Insert
    // insertAll: inserción masiva de los datos ficticios (más rápido que uno por uno).
    suspend fun insertAll(registros: List<RegistroConteo>)

    @Query("SELECT COUNT(*) FROM registro_conteo")
    suspend fun contar(): Int

    // Datos ficticios = los que no vienen de un cliente
    @Query("SELECT COUNT(*) FROM registro_conteo WHERE usuarioId IS NULL")
    // contarFicticios: cuenta solo los datos de ejemplo (usuarioId IS NULL). Si es 0,
    // la app sabe que debe generar los datos iniciales.
    suspend fun contarFicticios(): Int

    @Query("DELETE FROM registro_conteo WHERE usuarioId IS NULL")
    // deleteFicticios: borra SOLO los datos de ejemplo y conserva los de clientes.
    suspend fun deleteFicticios()

    @Query("DELETE FROM registro_conteo")
    suspend fun deleteAll()
}
