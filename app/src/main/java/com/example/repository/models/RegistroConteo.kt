/*
 * ARCHIVO: RegistroConteo.kt  (capa MODEL)
 * QUÉ HACE: tabla "registro_conteo": cuántas personas ENTRARON por cada acceso
 * (simula lo que mide el sistema de conteo/sensor de la tienda).
 * CÓMO: el Dashboard suma "cantidad" por acceso para obtener el total de ingresos.
 */
package com.example.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registro_conteo",
    foreignKeys = [
        // La relación de llave foránea es con la clase/entidad Acceso como tal,
        // no con una instancia particular de un acceso
        // FK a Acceso (CASCADE): el conteo pertenece a un acceso existente.
        ForeignKey(
            entity = Acceso::class,
            parentColumns = ["id"],
            childColumns = ["accesoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            // FK a Usuario con SET_NULL: si se borra el usuario, el conteo se conserva
            // pero queda como "anónimo" (usuarioId = null).
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("accesoId"), Index("usuarioId")]
)
data class RegistroConteo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accesoId: Int,
    val fechaHora: Long,
    // cantidad: personas contadas en esa lectura (datos ficticios = decenas; cliente = 1).
    val cantidad: Int,
    // usuarioId: null = dato ficticio generado por DatosIniciales.
    //              distinto de null = ingreso marcado por un cliente desde la app.
    // Esta marca permite que "Regenerar datos de ejemplo" borre solo los ficticios.
    val usuarioId: Int? = null // != null cuando el ingreso lo marcó un cliente desde la app
)
