/*
 * ARCHIVO: Validaciones.kt  (capa VIEWMODEL - apoyo)
 * QUÉ HACE: reglas de validación de los formularios, en un solo lugar.
 * CÓMO: object (singleton) con funciones puras: reciben texto y devuelven
 * true/false (o el número válido). No dependen de Android, por eso son fáciles de probar.
 */
package com.example.viewmodels

object Validaciones {
    const val MAX_CANTIDAD = 100_000
    const val MIN_PASSWORD = 6
    const val MAX_NOMBRE = 50

    // Expresiones regulares (Regex):
    //  SOLO_LETRAS: una o más letras (incluye tildes y ñ), y opcionalmente más palabras
    //               separadas por UN espacio. Rechaza números, símbolos y dobles espacios.
    //  EMAIL: texto@dominio.extensión (extensión de al menos 2 letras).
    private val SOLO_LETRAS = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+( [A-Za-zÁÉÍÓÚáéíóúÑñÜü]+)*$")
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /** Devuelve la cantidad si es un entero entre 1 y MAX_CANTIDAD; si no, null. */
    // toIntOrNull devuelve null si el texto no es número; takeIf devuelve null si
    // está fuera del rango 1..MAX_CANTIDAD.
    fun cantidadValida(texto: String): Int? =
        texto.trim().toIntOrNull()?.takeIf { it in 1..MAX_CANTIDAD }

    /** Solo letras (con tildes) y un espacio entre palabras. */
    // trim() elimina espacios al inicio/fin antes de validar largo y formato.
    fun esSoloLetras(texto: String): Boolean =
        texto.trim().let { it.length <= MAX_NOMBRE && SOLO_LETRAS.matches(it) }

    fun emailValido(email: String): Boolean = EMAIL.matches(email.trim())

    fun passwordValida(password: String): Boolean = password.length >= MIN_PASSWORD
}
