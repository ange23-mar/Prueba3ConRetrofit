/*
 * ARCHIVO: Enums.kt  (capa MODEL)
 * QUÉ HACE: reúne los valores fijos (catálogos) que usa toda la app.
 * CÓMO: cada enum class es una lista cerrada de opciones. Room 2.6 guarda un enum
 * en SQLite como TEXTO con su nombre (ej. 'COMPRA'), sin necesidad de convertidores.
 * Los parámetros (etiqueta, tipoCliente) son solo textos para mostrar en pantalla.
 */
package com.example.repository.models

/**
 * Motivo del paso por la tienda.
 * Se reutiliza también como TIPO DE CLIENTE, así cada registro del cliente
 * cae directamente en las mismas columnas que lee el Dashboard.
 */
// TipoFlujo cumple DOS funciones:
//  1) Motivo de la visita al marcar Ingreso/Salida: "Solo transita" o "Compra".
//  2) Tipo de cliente del perfil: "Cliente en tránsito" o "Cliente comprador".
// Usar el mismo enum hace que lo que marca el cliente quede guardado EXACTAMENTE
// en la misma columna (tipoFlujo) que lee el Dashboard del administrador.
enum class TipoFlujo(val etiqueta: String, val tipoCliente: String) {
    TRANSITO("Solo transita", "Cliente en tránsito"),
    COMPRA("Compra", "Cliente comprador")
}

// Para segmentar por edad
// RangoEtario: tramo de edad. Se usa para la pantalla "Perfil de tránsito"
// (segmentación) y se copia desde el perfil del cliente a cada ingreso que registra.
enum class RangoEtario(val etiqueta: String) {
    MENOR_18("Menor de 18"),
    ADULTO_18_29("18 a 29"),
    ADULTO_30_44("30 a 44"),
    ADULTO_45_59("45 a 59"),
    MAYOR_60("60 o más"),
    SIN_DATO("Sin dato")
}

/** Rol con el que inicia sesión una persona: decide qué interfaz ve. */
// RolUsuario: define qué interfaz ve la persona después del login
// (ADMINISTRADOR -> Dashboard, CLIENTE -> Ingreso/Salida y Perfil).
enum class RolUsuario(val etiqueta: String) {
    ADMINISTRADOR("Administrador"),
    CLIENTE("Cliente")
}

/** Movimiento que marca el cliente en un acceso. */
// TipoMovimiento: lo que marca el cliente en un acceso.
// INGRESO abre una visita; SALIDA la cierra.
enum class TipoMovimiento(val etiqueta: String) {
    INGRESO("Ingreso"),
    SALIDA("Salida")
}
