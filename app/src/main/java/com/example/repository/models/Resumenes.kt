/*
 * ARCHIVO: Resumenes.kt  (capa MODEL)
 * QUÉ HACE: "moldes" para el resultado de consultas agregadas (SUM, COUNT, GROUP BY).
 * CÓMO: NO son tablas (no tienen @Entity). Room rellena cada campo buscando en el
 * resultado del SELECT una columna con el MISMO NOMBRE. Por eso en los DAO se usa
 * "SUM(cantidad) AS total": el alias "total" calza con la propiedad "total".
 */
package com.example.repository.models

/*
 * Clases que Room usa para devolver el resultado de consultas con SUM / COUNT / GROUP BY.
 * No son tablas: solo "moldes" cuyos nombres de campo coinciden con los alias del SELECT.
 */

// Total de personas por acceso (RegistroConteoDao.totalesPorAcceso).
data class TotalAcceso(
    val accesoId: Int,
    val total: Int
)

// Total por acceso y tipo de flujo (RegistroFlujoDao.totalesPorAccesoYTipo).
data class TotalAccesoTipo(
    val accesoId: Int,
    val tipoFlujo: TipoFlujo,
    val total: Int
)

// Total por rango etario (RegistroFlujoDao.segmentacionTransito).
data class TotalRango(
    val rangoEtario: RangoEtario,
    val total: Int
)

/** Movimientos de clientes agrupados por tipo (ingreso/salida) y motivo. */
// Cantidad de movimientos de clientes por tipo y motivo (MovimientoClienteDao.totalesDesde).
data class TotalMovimiento(
    val tipoMovimiento: TipoMovimiento,
    val motivo: TipoFlujo,
    val total: Int
)
