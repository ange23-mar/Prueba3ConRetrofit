/*
 * ARCHIVO: RegistroFlujoDao.kt  (capa DATA - DAO)
 * QUÉ HACE: lee y escribe la tabla "registro_flujo" (clasificación tránsito/compra).
 */
package com.example.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.repository.models.RegistroFlujo
import com.example.repository.models.TotalAccesoTipo
import com.example.repository.models.TotalRango
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroFlujoDao {

    // READ: últimos registros (para la lista)
    @Query("SELECT * FROM registro_flujo WHERE fechaHora >= :desde ORDER BY fechaHora DESC LIMIT 200")
    // getDesde: últimos 200 registros desde una fecha (pantalla "Registros" del admin).
    fun getDesde(desde: Long): Flow<List<RegistroFlujo>>

    // Tránsito vs. compra por acceso en un periodo (pantalla Resumen).
    // Incluye automáticamente los registros generados por clientes (usuarioId != null).
    @Query(
        """
        SELECT accesoId, tipoFlujo, SUM(cantidad) AS total
        FROM registro_flujo
        WHERE fechaHora >= :desde
        GROUP BY accesoId, tipoFlujo
        """
    )
    // totalesPorAccesoYTipo: suma por acceso y por tipo (TRANSITO/COMPRA).
    // Es la consulta principal del Dashboard. Incluye automáticamente los registros
    // creados por los clientes, porque están en la misma tabla.
    fun totalesPorAccesoYTipo(desde: Long): Flow<List<TotalAccesoTipo>>

    // Perfil etario de quienes SOLO transitan. accesoId = 0 significa "todos los accesos".
    @Query(
        """
        SELECT rangoEtario, SUM(cantidad) AS total
        FROM registro_flujo
        WHERE tipoFlujo = 'TRANSITO'
          AND (:accesoId = 0 OR accesoId = :accesoId)
          AND fechaHora >= :desde
        GROUP BY rangoEtario
        """
    )
    // segmentacionTransito: personas que SOLO transitan, agrupadas por rango etario.
    // Truco: "(:accesoId = 0 OR accesoId = :accesoId)" -> si se pasa 0 la condición
    // es siempre verdadera (todos los accesos); si no, filtra por ese acceso.
    fun segmentacionTransito(accesoId: Int, desde: Long): Flow<List<TotalRango>>

    // CREATE
    @Insert
    // insert / insertAll: registro individual o carga masiva.
    suspend fun insert(registro: RegistroFlujo): Long

    @Insert
    suspend fun insertAll(registros: List<RegistroFlujo>)

    // DELETE
    // delete: Room busca la fila por su clave primaria (id) y la elimina.
    @Delete
    suspend fun delete(registro: RegistroFlujo)

    // Al regenerar datos de ejemplo se conservan los registros de clientes
    @Query("DELETE FROM registro_flujo WHERE usuarioId IS NULL")
    // deleteFicticios: borra solo los datos de ejemplo (usuarioId IS NULL).
    suspend fun deleteFicticios()

    @Query("DELETE FROM registro_flujo")
    suspend fun deleteAll()
}
