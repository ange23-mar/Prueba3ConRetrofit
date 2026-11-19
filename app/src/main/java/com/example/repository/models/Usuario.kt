/*
 * ARCHIVO: Usuario.kt  (capa MODEL)
 * QUÉ HACE: define la tabla "usuarios" (administradores y clientes).
 * CÓMO: una sola tabla para ambos roles; la columna "rol" los distingue.
 * Los campos propios del cliente (tipoCliente) quedan en null para el administrador.
 */
package com.example.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Usuario de la app. El rol (ADMINISTRADOR / CLIENTE) define la interfaz tras el login.
 * - tipoCliente: solo para clientes (Cliente en tránsito / Cliente comprador). Null para administradores.
 * - rangoEtario: se copia a cada RegistroFlujo que genera el cliente (alimenta la segmentación).
 */
// indices = Index(email, unique = true): SQLite no permite dos usuarios con el
// mismo email. Si se intenta, Room lanza SQLiteConstraintException (se captura en
// UsuarioViewModel para mostrar "Ya existe una cuenta...").
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    // Contraseña en texto plano: SOLO para fines académicos. En producción se guardaría un hash.
    val password: String,               // Solo para fines académicos (sin hash)
    // rol: se guarda como 'ADMINISTRADOR' o 'CLIENTE'. AppNavigation lo lee para elegir el menú.
    val rol: RolUsuario,
    // tipoCliente: nullable (TipoFlujo?) porque el administrador no tiene tipo de cliente.
    val tipoCliente: TipoFlujo? = null,
    // rangoEtario: valor por defecto SIN_DATO si el cliente no lo indica.
    val rangoEtario: RangoEtario = RangoEtario.SIN_DATO,
    // fotoPerfilUri: ruta del archivo .jpg tomado con CameraX (ej. file:///data/.../perfil/img_123.jpg).
    // Se guarda la RUTA, no la imagen, para no inflar la base de datos.
    val fotoPerfilUri: String? = null   // Foto tomada con CameraX (almacenamiento interno)
)
