/*
 * ARCHIVO: AppNavegation.kt  (capa VIEW - navegación)
 * QUÉ HACE: define las rutas, los menús por rol, la protección de rutas, el menú
 * lateral, la barra superior y el NavHost con todas las pantallas.
 * CÓMO: Navigation Compose. Cada pantalla tiene una "ruta" (texto). navController
 * navega entre rutas y guarda la pila de pantallas (back stack). El ROL del usuario
 * logueado decide qué menú y qué pantallas puede ver:
 *    ADMINISTRADOR -> Dashboard, Perfil de tránsito, Registros.
 *    CLIENTE       -> Ingreso / Salida, Mi perfil.
 */
package com.example.navegations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.repository.models.RolUsuario
import com.example.repository.models.Usuario
import com.example.view.ui.ClienteMovimientosScreen
import com.example.view.ui.DashboardScreen
import com.example.view.ui.FlujoFormScreen
import com.example.view.ui.FlujoListScreen
import com.example.view.ui.LoginScreen
import com.example.view.ui.PerfilScreen
import com.example.view.ui.RegistroClienteScreen
import com.example.view.ui.SegmentacionScreen
import com.example.view.utils.navEnterTransition
import com.example.view.utils.navExitTransition
import com.example.view.utils.navPopEnterTransition
import com.example.view.utils.navPopExitTransition
import com.example.viewmodels.DashboardViewModel
import com.example.viewmodels.FlujoViewModel
import com.example.viewmodels.LoginViewModel
import com.example.viewmodels.MovimientoViewModel
import com.example.viewmodels.PerfilViewModel
import com.example.viewmodels.SegmentacionViewModel
import com.example.viewmodels.UsuarioViewModel
import kotlinx.coroutines.launch

/** Rutas de navegación */
// Routes: nombres de las rutas en un solo lugar (evita errores de tipeo).
object Routes {
    // Públicas
    const val LOGIN = "login"
    const val REGISTRO = "registro_cliente"

    // ADMINISTRADOR
    const val DASHBOARD = "dashboard"
    const val SEGMENTACION = "segmentacion"
    const val FLUJOS = "flujos"
    const val FLUJO_FORM = "flujo_form"

    // CLIENTE
    const val CLIENTE_MOVIMIENTOS = "cliente_movimientos"
    const val CLIENTE_PERFIL = "cliente_perfil"

    // Conjuntos de rutas por tipo de usuario: se usan para validar permisos.
    val publicas = setOf(LOGIN, REGISTRO)
    val administrador = setOf(DASHBOARD, SEGMENTACION, FLUJOS, FLUJO_FORM)
    val cliente = setOf(CLIENTE_MOVIMIENTOS, CLIENTE_PERFIL)

    /** Pantalla inicial según el rol: Dashboard para admin, Ingreso/Salida para cliente. */
    // inicioPara: pantalla de inicio según el rol (aquí se cumple el flujo pedido:
    // admin -> Dashboard existente; cliente -> interfaz de cliente).
    fun inicioPara(rol: RolUsuario): String = when (rol) {
        RolUsuario.ADMINISTRADOR -> DASHBOARD
        RolUsuario.CLIENTE -> CLIENTE_MOVIMIENTOS
    }

    // permitida: true si la ruta pertenece al rol (un cliente no puede abrir el Dashboard).
    fun permitida(route: String, rol: RolUsuario): Boolean = when (rol) {
        RolUsuario.ADMINISTRADOR -> route in administrador
        RolUsuario.CLIENTE -> route in cliente
    }
}

// MenuItem: una opción del menú lateral (ruta, texto e ícono).
data class MenuItem(val route: String, val label: String, val icon: ImageVector)

/** Menú lateral del ADMINISTRADOR (Dashboard existente) */
val menuAdministrador = listOf(
    MenuItem(Routes.DASHBOARD, "Resumen por acceso", Icons.Default.Dashboard),
    MenuItem(Routes.SEGMENTACION, "Perfil de tránsito", Icons.Default.PieChart),
    MenuItem(Routes.FLUJOS, "Registros", Icons.AutoMirrored.Filled.ListAlt)
)

/** Menú lateral del CLIENTE */
val menuCliente = listOf(
    MenuItem(Routes.CLIENTE_MOVIMIENTOS, "Ingreso / Salida", Icons.Default.SwapHoriz),
    MenuItem(Routes.CLIENTE_PERFIL, "Mi perfil", Icons.Default.AccountCircle)
)

// menuPara: devuelve el menú del rol; sin sesión, lista vacía.
fun menuPara(rol: RolUsuario?): List<MenuItem> = when (rol) {
    RolUsuario.ADMINISTRADOR -> menuAdministrador
    RolUsuario.CLIENTE -> menuCliente
    null -> emptyList()
}

// titleFor: título de la barra superior según la pantalla actual.
private fun titleFor(route: String?): String = when (route) {
    Routes.REGISTRO -> "Registro de cliente"
    Routes.DASHBOARD -> "Resumen por acceso"
    Routes.SEGMENTACION -> "Perfil de tránsito"
    Routes.FLUJOS -> "Registros"
    Routes.FLUJO_FORM -> "Nuevo registro"
    Routes.CLIENTE_MOVIMIENTOS -> "Ingreso / Salida"
    Routes.CLIENTE_PERFIL -> "Mi perfil"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    usuarioViewModel: UsuarioViewModel,
    dashboardViewModel: DashboardViewModel,
    segmentacionViewModel: SegmentacionViewModel,
    flujoViewModel: FlujoViewModel,
    perfilViewModel: PerfilViewModel,
    movimientoViewModel: MovimientoViewModel
) {
    // navController: controla la navegación. rememberNavController lo conserva entre recomposiciones.
    // drawerState: abierto/cerrado del menú lateral. scope: para abrir/cerrar el menú
    // (son funciones suspend y necesitan una corrutina).
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // currentRoute: ruta de la pantalla visible; se actualiza en cada navegación.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // sesion: usuario logueado (null = sin sesión).
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val sesion = loginState.loggedUser
    // Versión reactiva del usuario (refleja foto/tipo de cliente actualizados).
    // Se valida el id para no mostrar datos de una sesión anterior.
    val perfil by perfilViewModel.usuario.collectAsStateWithLifecycle()
    // usuarioActual: versión "en vivo" desde Room (con la foto más reciente). takeIf evita
    // mostrar por un instante los datos de una sesión anterior.
    val usuarioActual: Usuario? = perfil?.takeIf { it.id == sesion?.id } ?: sesion

    // isTopLevel: true en pantallas del menú (muestra ícono ☰); false en subpantallas (muestra ←).
    val menu = menuPara(sesion?.rol)
    val isTopLevel = menu.any { it.route == currentRoute }

    // Los ViewModels del cliente siguen a la sesión activa
    // Cada vez que cambia el usuario de la sesión, se informa a los ViewModels del cliente
    // para que consulten los datos de ESE usuario (o se limpien al cerrar sesión).
    LaunchedEffect(sesion?.id) {
        perfilViewModel.setUsuario(sesion?.id)
        movimientoViewModel.setUsuario(sesion?.id)
    }

    // Protección de rutas: sin sesión -> Login; rol sin permiso -> su pantalla de inicio
    // Protección de rutas (se evalúa al cambiar la sesión o la pantalla):
    //  - sin sesión en una ruta privada -> al Login.
    //  - ruta de otro rol -> a la pantalla de inicio de su rol.
    // popUpTo(graph.id) inclusive vacía la pila para que "atrás" no regrese a la ruta bloqueada.
    // launchSingleTop evita apilar dos veces la misma pantalla.
    LaunchedEffect(sesion, currentRoute) {
        val route = currentRoute ?: return@LaunchedEffect
        if (route in Routes.publicas) return@LaunchedEffect
        when {
            sesion == null -> navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
            !Routes.permitida(route, sesion.rol) -> navController.navigate(Routes.inicioPara(sesion.rol)) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // navigateTopLevel: navegación desde el menú. popUpTo(inicio) evita que la pila crezca
    // infinitamente al saltar entre opciones del menú.
    fun navigateTopLevel(route: String) {
        val inicio = sesion?.let { Routes.inicioPara(it.rol) } ?: return
        navController.navigate(route) {
            popUpTo(inicio) { saveState = false }
            launchSingleTop = true
        }
    }

    // cerrarSesion: cierra el menú, borra la sesión y vuelve al Login limpiando la pila.
    fun cerrarSesion() {
        scope.launch { drawerState.close() }
        loginViewModel.logout()
        navController.navigate(Routes.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    // ModalNavigationDrawer: menú lateral deslizable. El gesto solo se habilita en pantallas
    // de primer nivel (en formularios se usa la flecha atrás).
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isTopLevel,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                CabeceraMenu(usuarioActual)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                // Una opción por cada elemento del menú del rol; la seleccionada se resalta.
                menu.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTopLevel(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = ::cerrarSesion,
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        // Scaffold: estructura base de la pantalla (barra superior + contenido).
        Scaffold(
            topBar = {
                // La barra superior se oculta en el Login. Muestra ☰ (abrir menú) o ← (volver).
                if (currentRoute != null && currentRoute != Routes.LOGIN) {
                    TopAppBar(
                        title = { Text(titleFor(currentRoute)) },
                        navigationIcon = {
                            if (isTopLevel) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            // innerPadding: margen que deja el Scaffold para no tapar el contenido con la barra.
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                loginViewModel = loginViewModel,
                usuarioViewModel = usuarioViewModel,
                dashboardViewModel = dashboardViewModel,
                segmentacionViewModel = segmentacionViewModel,
                flujoViewModel = flujoViewModel,
                perfilViewModel = perfilViewModel,
                movimientoViewModel = movimientoViewModel,
                usuarioActual = usuarioActual
            )
        }
    }
}

/** Cabecera del menú lateral: foto de perfil (si existe), nombre, email y rol. */
@Composable
// CabeceraMenu: foto (Coil) o ícono, nombre, email y rol en la parte superior del menú.
private fun CabeceraMenu(usuario: Usuario?) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (usuario?.fotoPerfilUri != null) {
            AsyncImage(
                model = usuario.fotoPerfilUri,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
        } else {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(usuario?.nombre ?: "Tienda Rincón Sur", style = MaterialTheme.typography.titleMedium)
            Text(usuario?.email ?: "", style = MaterialTheme.typography.bodySmall)
            Text(
                usuario?.rol?.etiqueta ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    usuarioViewModel: UsuarioViewModel,
    dashboardViewModel: DashboardViewModel,
    segmentacionViewModel: SegmentacionViewModel,
    flujoViewModel: FlujoViewModel,
    perfilViewModel: PerfilViewModel,
    movimientoViewModel: MovimientoViewModel,
    usuarioActual: Usuario?
) {
    // Transiciones de deslizamiento definidas en view/utils/Animations.kt
    // NavHost: asocia cada ruta con su pantalla. startDestination = LOGIN: la app siempre
    // empieza en el inicio de sesión. Las transiciones vienen de Animation.kt.
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,
        enterTransition = navEnterTransition,
        exitTransition = navExitTransition,
        popEnterTransition = navPopEnterTransition,
        popExitTransition = navPopExitTransition
    ) {
        // ---------- Públicas ----------
        // Login exitoso -> pantalla de inicio de su ROL. popUpTo(LOGIN) inclusive saca el login
        // de la pila (así "atrás" no vuelve al login estando con sesión).
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { usuario ->
                    // El ROL decide la interfaz: Dashboard (admin) o Ingreso/Salida (cliente)
                    navController.navigate(Routes.inicioPara(usuario.rol)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegistrarse = { navController.navigate(Routes.REGISTRO) }
            )
        }
        // Registro exitoso -> precarga el email en el login y vuelve atrás.
        composable(Routes.REGISTRO) {
            RegistroClienteScreen(
                viewModel = usuarioViewModel,
                onRegistrado = { email ->
                    loginViewModel.onEmailChange(email) // precarga el email en el login
                    navController.popBackStack()
                },
                onCancelar = { navController.popBackStack() }
            )
        }

        // ---------- ADMINISTRADOR ----------
        // Pantallas del ADMINISTRADOR.
        composable(Routes.DASHBOARD) { DashboardScreen(dashboardViewModel) }
        composable(Routes.SEGMENTACION) { SegmentacionScreen(segmentacionViewModel) }
        composable(Routes.FLUJOS) {
            FlujoListScreen(
                viewModel = flujoViewModel,
                onAdd = { navController.navigate(Routes.FLUJO_FORM) }
            )
        }
        composable(Routes.FLUJO_FORM) {
            FlujoFormScreen(
                viewModel = flujoViewModel,
                onDone = { navController.popBackStack() }
            )
        }

        // ---------- CLIENTE ----------
        // Pantallas del CLIENTE.
        composable(Routes.CLIENTE_MOVIMIENTOS) {
            ClienteMovimientosScreen(viewModel = movimientoViewModel, usuario = usuarioActual)
        }
        composable(Routes.CLIENTE_PERFIL) { PerfilScreen(perfilViewModel) }
    }
}
