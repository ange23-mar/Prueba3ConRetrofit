/*
 * ARCHIVO: SegmentacionScreen.kt  (capa VIEW - pantalla del ADMINISTRADOR)
 * QUÉ HACE: muestra el perfil etario de las personas que SOLO transitan, por periodo
 * y por acceso, con barras de porcentaje animadas.
 */
package com.example.view.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.view.utils.SelectorPeriodo
import com.example.viewmodels.FilaSegmento
import com.example.viewmodels.SegmentacionViewModel
import kotlin.math.roundToInt

/** Perfil etario de quienes SOLO transitan (objetivo principal del caso). */
@Composable
fun SegmentacionScreen(viewModel: SegmentacionViewModel) {
    // collectAsStateWithLifecycle: convierte el StateFlow del ViewModel en estado de Compose.
    // Cada vez que cambia, la pantalla se "recompone" (se redibuja) con los datos nuevos.
    // "WithLifecycle": deja de observar si la app pasa a segundo plano (ahorra batería).
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Perfil de quienes solo transitan", style = MaterialTheme.typography.titleLarge)

        SelectorPeriodo(state.periodo, viewModel::cambiarPeriodo)

        // Chips de acceso: "Todos" (id 0) o uno de los 4 accesos.
        // Filtro por acceso: "Todos" (0) o uno de los 4 accesos
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.accesoId == 0,
                onClick = { viewModel.seleccionarAcceso(0) },
                label = { Text("Todos") }
            )
            state.accesos.forEach { acceso ->
                FilterChip(
                    selected = state.accesoId == acceso.id,
                    onClick = { viewModel.seleccionarAcceso(acceso.id) },
                    label = { Text(acceso.nombreAcceso) }
                )
            }
        }

        // Tarjeta con el total en tránsito y el rango predominante.
        // Conclusión rápida para apoyar decisiones comerciales
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${state.totalTransito} personas en tránsito", style = MaterialTheme.typography.titleMedium)
                val top = state.predominante
                if (top != null) {
                    Text(
                        "Perfil predominante: ${top.rango.etiqueta} (${(top.porcentaje * 100).roundToInt()}%)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text("Sin datos en este periodo.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        state.filas.forEach { fila -> com.example.view.ui.FilaRango(fila) }
    }
}

@Composable
// FilaRango: nombre del rango, cantidad, % y barra de progreso.
private fun FilaRango(fila: FilaSegmento) {
    // Animación: la barra "crece" hasta su valor cada vez que cambian los datos
    // animateFloatAsState: cuando cambia el porcentaje, la barra "crece" suavemente
    // hasta el nuevo valor en 600 ms en vez de saltar.
    val progreso by animateFloatAsState(
        targetValue = fila.porcentaje,
        animationSpec = tween(600),
        label = "progresoRango"
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(fila.rango.etiqueta, modifier = Modifier.weight(1f))
            Text("${fila.total} (${(fila.porcentaje * 100).roundToInt()}%)")
        }
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier.fillMaxWidth()
        )
    }
}