# Guía del código – Prueba2 (Tienda Rincón Sur)

Cada archivo `.kt` tiene al inicio un bloque **ARCHIVO / QUÉ HACE / CÓMO** y comentarios `//` antes de cada bloque importante. Esta guía da la visión general para recorrerlos en orden.

## 1. Arquitectura MVVM

```
VIEW (Compose)            VIEWMODEL                    DATA / MODEL (Room)
view/ui/*Screen.kt  --->  viewmodels/*ViewModel.kt --> repository/data/*Repository.kt --> *Dao.kt --> SQLite
      ^                          |                                                        repository/models/*.kt
      |____ StateFlow (estado) __|
```

- **Model** (`repository/models`): entidades `@Entity` = tablas SQLite.
- **Data** (`repository/data`): DAOs con el SQL, `AppDatabase` y los Repository (fuente única de datos).
- **ViewModel** (`viewmodels`): lógica y validaciones; expone el estado con `StateFlow`.
- **View** (`view/ui`, `view/utils`): pantallas Compose que solo dibujan el estado y avisan acciones.
- **Navegación** (`navegations`): rutas, menús por rol y protección de rutas.

La vista **nunca** toca la base de datos: observa el estado del ViewModel y le llama funciones.

## 2. Orden sugerido de lectura

1. `repository/models/Enums.kt` → catálogos (roles, tipos, motivos).
2. `repository/models/*.kt` → tablas.
3. `repository/data/*Dao.kt` → consultas SQL.
4. `repository/data/AppDatabase.kt` → creación de la BD y datos iniciales.
5. `repository/data/ConteoRepository.kt` → **integración cliente → Dashboard**.
6. `viewmodels/LoginViewModel.kt` → sesión.
7. `navegations/AppNavegation.kt` → cómo el rol decide la interfaz.
8. Pantallas del cliente: `ClienteMovimientoScreen.kt`, `PerfilScreen.kt`.
9. Pantallas del administrador: `DashboardScreen.kt` y las demás.
10. Cámara y animaciones: `CameraCapture.kt`, `Animation.kt`, `FotoPerfil.kt`.

## 3. Flujos principales

**Inicio de sesión:**
`LoginScreen` → `LoginViewModel.login()` → `UsuarioRepository.login()` → `UsuarioDao.login()` (SQL) → `loggedUser` en el estado → `AppNavegation` navega a `Routes.inicioPara(rol)`:
- `ADMINISTRADOR` → Dashboard.
- `CLIENTE` → Ingreso / Salida.

**Cliente marca un Ingreso:**
`ClienteMovimientoScreen` → `MovimientoViewModel.registrar()` → `ConteoRepository.registrarMovimiento()` (una transacción):
1. Valida la secuencia (no se puede marcar un ingreso estando dentro).
2. Inserta en `movimiento_cliente`.
3. Suma 1 en `registro_conteo` y 1 en `registro_flujo` (con motivo y rango etario).

Room notifica a los `Flow` → `DashboardViewModel` recalcula → el Dashboard se actualiza solo.

**Foto de perfil:**
`PerfilScreen` → `CameraCapture(camaraFrontal = true, animarCaptura = true)` → se guarda el `.jpg` en `files/fotos/perfil` → `PerfilViewModel.actualizarFoto()` guarda la ruta en Room → `FotoPerfilAnimada` anima la foto nueva.

## 4. Conceptos clave usados

| Concepto | Dónde | Para qué |
|---|---|---|
| `Flow` (Room) | DAOs | La consulta se re-emite cuando la tabla cambia |
| `StateFlow` + `stateIn` | ViewModels | Estado observable con valor actual |
| `combine` / `flatMapLatest` | Dashboard, Segmentación | Unir consultas / rehacerlas al cambiar filtros |
| `viewModelScope.launch` + `Dispatchers.IO` | ViewModels | Trabajar con la BD sin congelar la UI |
| `withTransaction` | ConteoRepository | Guardar todo o nada |
| `collectAsStateWithLifecycle` | Pantallas | Redibujar la pantalla cuando cambia el estado |
| `remember` / `rememberSaveable` | Pantallas | Estado local (el segundo sobrevive a rotaciones) |
| `LaunchedEffect` / `DisposableEffect` | Pantallas, cámara | Efectos al entrar/salir o al cambiar una clave |
| `AndroidView` | CameraCapture | Incrustar la vista previa de CameraX en Compose |

## 5. Usuarios de prueba

- Administrador: `admin@rinconsur.cl` / `admin123`
- Cliente: `cliente@rinconsur.cl` / `cliente123`
