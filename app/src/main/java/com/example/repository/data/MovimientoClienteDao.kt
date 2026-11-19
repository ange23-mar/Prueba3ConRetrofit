/*
 * ARCHIVO: MovimientoClienteDao.kt  (capa DATA - DAO)
 * QUÉ HACE: inserta y consulta los Ingresos/Salidas de los clientes.
 * CÓMO: SQL directo con @Query. Las consultas con Flow mantienen la UI al día.
 */
package com.example.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.repository.models.MovimientoCliente
import com.example.repository.models.TotalMovimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoClienteDao {

    // CREATE
    // Guarda un movimiento nuevo y devuelve el id generado.
    @Insert
    suspend fun insert(movimiento: MovimientoCliente): Long

    // Historial del cliente (el primero es el último movimiento -> estado dentro/fuera)
    @Query(
        """
        SELECT * FROM movimiento_cliente
        WHERE usuarioId = :usuarioId
        ORDER BY fechaHora DESC, id DESC
        LIMIT 100
        """
    )
    // historial: últimos 100 movimientos del cliente, del más nuevo al más antiguo.
    // El PRIMERO de la lista es su último movimiento: si es INGRESO, el cliente está "dentro".
    // Se ordena también por id para desempatar dos registros con la misma fechaHora.
    fun historial(usuarioId: Int): Flow<List<MovimientoCliente>>

    // Último movimiento (lectura puntual, se usa dentro de la transacción de registro)
    @Query("SELECT * FROM movimiento_cliente WHERE usuarioId = :usuarioId ORDER BY fechaHora DESC, id DESC LIMIT 1")
    // ultimo: lectura puntual del último movimiento. ConteoRepository la usa para
    // validar que no se marque un Ingreso estando dentro (o una Salida estando fuera).
    suspend fun ultimo(usuarioId: Int): MovimientoCliente?

    // Dashboard: ingresos/salidas por motivo en un periodo
    @Query(
        """
        SELECT tipoMovimiento, motivo, COUNT(*) AS total
        FROM movimiento_cliente
        WHERE fechaHora >= :desde
        GROUP BY tipoMovimiento, motivo
        """
    )
    // totalesDesde: cuenta movimientos agrupados por tipo y motivo desde una fecha.
    // COUNT(*) AS total -> se mapea a la clase TotalMovimiento (Resumenes.kt).
    fun totalesDesde(desde: Long): Flow<List<TotalMovimiento>>

    // Dashboard: clientes cuyo último movimiento es un INGRESO (están dentro ahora)
    @Query(
        """
        SELECT COUNT(*) FROM movimiento_cliente m
        WHERE m.tipoMovimiento = 'INGRESO'
          AND m.id = (
              SELECT m2.id FROM movimiento_cliente m2
              WHERE m2.usuarioId = m.usuarioId
              ORDER BY m2.fechaHora DESC, m2.id DESC
              LIMIT 1
          )
        """
    )
    // clientesDentro: cuántos clientes están en la tienda AHORA.
    // Cómo: para cada movimiento "m" de tipo INGRESO, la subconsulta busca el id del
    // último movimiento de ese mismo usuario; si coincide con m.id, ese INGRESO es su
    // movimiento más reciente (aún no marca salida) y se cuenta.
    fun clientesDentro(): Flow<Int>
}
