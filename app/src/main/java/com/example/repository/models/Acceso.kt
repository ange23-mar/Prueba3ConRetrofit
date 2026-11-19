/*
 * ARCHIVO: Acceso.kt  (capa MODEL de MVVM)
 * QUÉ HACE: define la tabla "accesos": las 4 entradas físicas de la tienda.
 * CÓMO: @Entity convierte esta data class en una tabla SQLite. Room genera
 * el SQL de creación a partir de las propiedades (cada propiedad = una columna).
 */
package com.example.repository.models
import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity(tableName = "accesos"): nombre de la tabla en SQLite.
@Entity(tableName = "accesos")
data class Acceso (
    // id: clave primaria. autoGenerate = true hace que SQLite asigne 1, 2, 3...
    // si se inserta con id = 0. Los 4 accesos se crean en AppDatabase (seedCallback).
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val nombreAcceso: String,
    val ubicacionDescripcion: String
)