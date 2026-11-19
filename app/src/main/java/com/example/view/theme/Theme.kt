/*
 * ARCHIVO: Theme.kt  (capa VIEW - tema visual)
 * QUÉ HACE: define los colores de la app (Material Design 3) en modo claro y oscuro.
 * CÓMO: lightColorScheme / darkColorScheme crean paletas completas a partir de unos
 * pocos colores. AppTheme elige una u otra con isSystemInDarkTheme().
 * Toda la UI lee estos colores con MaterialTheme.colorScheme.
 * Nota: este tema de Compose reemplaza al de res/values/themes.xml, que solo se usa
 * mientras la Activity arranca.
 */
package com.example.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Material Design 3: esquema de color claro/oscuro según el tema del sistema
private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),   // compra
    secondary = Color(0xFF00897B),
    tertiary = Color(0xFFF9A825)   // solo tránsito
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFFFE082)
)

@Composable
// AppTheme envuelve toda la UI (ver MainActivity) y entrega el esquema de colores.
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
