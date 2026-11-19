/*
 * ARCHIVO: MainActivity.kt  (punto de entrada de la app)
 * QUÉ HACE: es la única Activity. Crea los ViewModels y muestra la UI de Compose.
 * CÓMO:
 *  - "by viewModels()" crea cada ViewModel la primera vez que se usa y lo conserva
 *    aunque la pantalla rote (la Activity se destruye y recrea, el ViewModel no).
 *  - setContent reemplaza el XML clásico: toda la interfaz se dibuja con Compose.
 */
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.navegations.AppNavigation
import com.example.view.theme.AppTheme
import com.example.viewmodels.DashboardViewModel
import com.example.viewmodels.FlujoViewModel
import com.example.viewmodels.LoginViewModel
import com.example.viewmodels.MovimientoViewModel
import com.example.viewmodels.PerfilViewModel
import com.example.viewmodels.SegmentacionViewModel
import com.example.viewmodels.UsuarioViewModel

class MainActivity : ComponentActivity() {

    // ViewModels (capa VM de MVVM) con alcance de la Activity: sobreviven a rotaciones
    // ViewModels con alcance de Activity: TODAS las pantallas comparten las mismas
    // instancias (por ejemplo, la sesión del LoginViewModel es visible en toda la app).
    private val loginViewModel: LoginViewModel by viewModels()
    private val usuarioViewModel: UsuarioViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val segmentacionViewModel: SegmentacionViewModel by viewModels()
    private val flujoViewModel: FlujoViewModel by viewModels()
    private val perfilViewModel: PerfilViewModel by viewModels()
    private val movimientoViewModel: MovimientoViewModel by viewModels()

    // onCreate: lo llama Android al abrir la app.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge: la app dibuja detrás de las barras del sistema
        // (Scaffold agrega los márgenes necesarios).
        enableEdgeToEdge()
        setContent {
            // AppTheme aplica colores Material 3; AppNavigation decide qué pantalla mostrar.
            AppTheme {
                AppNavigation(
                    loginViewModel = loginViewModel,
                    usuarioViewModel = usuarioViewModel,
                    dashboardViewModel = dashboardViewModel,
                    segmentacionViewModel = segmentacionViewModel,
                    flujoViewModel = flujoViewModel,
                    perfilViewModel = perfilViewModel,
                    movimientoViewModel = movimientoViewModel
                )
            }
        }
    }
}
