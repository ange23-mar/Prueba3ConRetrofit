/*
 * ARCHIVO: MovimientoCliente.kt  (capa MODEL)
 * QUÉ HACE: tabla "movimiento_cliente": bitácora de cada Ingreso y Salida que
 * marca un cliente desde la app.
 * CÓMO: cada fila referencia a un usuario y a un acceso mediante llaves foráneas.
 */
package com.example.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Bitácora de ingresos y salidas marcados por un cliente desde la app. */
@Entity(
    tableName = "movimiento_cliente",
    // foreignKeys: SQLite verifica que usuarioId exista en "usuarios" y que accesoId
    // exista en "accesos". onDelete = CASCADE: si se borra el usuario o el acceso,
    // sus movimientos se borran automáticamente.
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Acceso::class,
            parentColumns = ["id"],
            childColumns = ["accesoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // indices: aceleran las búsquedas por usuario, acceso y fecha (consultas del historial
    // y del Dashboard). Room además recomienda indexar toda columna con llave foránea.
    indices = [Index("usuarioId"), Index("accesoId"), Index("fechaHora")]
)
data class MovimientoCliente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Int,
    val accesoId: Int,
    val tipoMovimiento: TipoMovimiento,
    // motivo: "Solo transita" o "Compra" elegido por el cliente al marcar el movimiento.
    val motivo: TipoFlujo,              // "Solo transita" o "Compra"
    // fechaHora: milisegundos desde 1970 (System.currentTimeMillis()). Guardar un Long
    // permite filtrar por periodo con un simple "fechaHora >= desde".
    val fechaHora: Long
)
