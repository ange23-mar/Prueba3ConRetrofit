/*
 * ARCHIVO: RegistroFlujo.kt  (capa MODEL)
 * QUÉ HACE: tabla "registro_flujo": CLASIFICA a las personas que entraron
 * (cuántas solo transitan y cuántas compran, y de qué rango etario).
 * CÓMO: el Dashboard compara registro_conteo (total) con registro_flujo
 * (clasificados). La diferencia se muestra como "Sin clasificar".
 */
package com.example.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registro_flujo",
    foreignKeys = [
        // FK a Acceso (CASCADE) y FK a Usuario (SET_NULL), igual que en RegistroConteo.
        ForeignKey(
            entity = Acceso::class,
            parentColumns = ["id"],
            childColumns = ["accesoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("accesoId"), Index("fechaHora"), Index("usuarioId")]
)
data class RegistroFlujo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accesoId: Int,
    val fechaHora: Long,
    // tipoFlujo: TRANSITO o COMPRA. Es la columna que agrupa el Dashboard.
    val tipoFlujo: TipoFlujo,
    val rangoEtario: RangoEtario = RangoEtario.SIN_DATO,
    val cantidad: Int,
    // fotoUri: foto de evidencia opcional que toma el administrador en FlujoFormScreen.
    val fotoUri: String? = null, // foto de evidencia tomada con CameraX (opcional)
    // usuarioId: igual que en RegistroConteo, distingue datos ficticios de datos de clientes.
    val usuarioId: Int? = null   // != null cuando lo generó un cliente desde la app
)
