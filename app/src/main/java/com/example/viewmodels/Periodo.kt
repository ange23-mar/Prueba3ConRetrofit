/*
 * ARCHIVO: Periodo.kt  (capa VIEWMODEL - apoyo)
 * QUÉ HACE: define los filtros de tiempo (Hoy, 7 días, 30 días, Todo) y calcula
 * desde qué instante consultar.
 * CÓMO: convierte "inicio del día" de la zona horaria del teléfono a milisegundos,
 * que es como se guarda fechaHora en la base de datos.
 */
package com.example.viewmodels

import java.time.LocalDate
import java.time.ZoneId

/** Periodos de consulta del historial (requisito: "información histórica y por periodo"). */
enum class Periodo(val etiqueta: String) {
    HOY("Hoy"),
    SIETE_DIAS("7 días"),
    TREINTA_DIAS("30 días"),
    TODOS("Todo");

    /** Fecha de inicio del periodo en epoch-millis; las consultas usan "fechaHora >= desde". */
    // desdeMillis: when elige la fecha de inicio según el periodo.
    // SIETE_DIAS usa minusDays(6) porque "hoy" también cuenta (hoy + 6 días previos = 7).
    // TODOS devuelve 0 (1 de enero de 1970) para incluir todo el historial.
    // atStartOfDay -> 00:00 de esa fecha; toEpochMilli -> número Long comparable con fechaHora.
    fun desdeMillis(): Long {
        val zona = ZoneId.systemDefault()
        val hoy = LocalDate.now(zona)
        return when (this) {
            HOY -> hoy
            SIETE_DIAS -> hoy.minusDays(6)
            TREINTA_DIAS -> hoy.minusDays(29)
            TODOS -> return 0L
        }.atStartOfDay(zona).toInstant().toEpochMilli()
    }
}