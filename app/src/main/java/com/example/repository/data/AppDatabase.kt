/*
 * ARCHIVO: AppDatabase.kt  (capa DATA)
 * QUÉ HACE: define la base de datos SQLite de la app y entrega sus DAOs.
 * CÓMO:
 *  - @Database lista las entidades (tablas) y la versión del esquema.
 *  - Patrón Singleton: existe UNA sola instancia de la BD para toda la app.
 *  - Un Callback inserta los datos base (accesos y usuarios de prueba).
 */
package com.example.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.repository.models.Acceso
import com.example.repository.models.MovimientoCliente
import com.example.repository.models.RegistroConteo
import com.example.repository.models.RegistroFlujo
import com.example.repository.models.Usuario

// entities: cada clase @Entity se convierte en una tabla.
// version = 3: se debe aumentar cada vez que cambia la estructura de las tablas.
// exportSchema = false: no genera archivos JSON del esquema (proyecto académico).
@Database(
    entities = [
        Acceso::class,
        RegistroConteo::class,
        RegistroFlujo::class,
        Usuario::class,
        MovimientoCliente::class
    ],
    version = 3, // v2: RegistroFlujo.fotoUri | v3: usuarios, roles, movimiento_cliente, usuarioId en conteo/flujo
    exportSchema = false
)
// Clase abstracta: Room genera la implementación real (AppDatabase_Impl).
abstract class AppDatabase : RoomDatabase() {
    // Cada función abstracta entrega un DAO. Room crea su implementación.
    abstract fun accesoDao(): AccesoDao
    abstract fun registroConteoDao(): RegistroConteoDao
    abstract fun registroFlujoDao(): RegistroFlujoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun movimientoClienteDao(): MovimientoClienteDao

    // companion object = equivalente a "static" en Java.
    companion object {
        // @Volatile: todos los hilos ven el valor actualizado de INSTANCE de inmediato.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // getDatabase: si ya existe la instancia la devuelve; si no, la crea.
        // synchronized evita que dos hilos creen dos bases de datos al mismo tiempo.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // databaseBuilder crea el archivo "rincon_sur_database" en el almacenamiento interno.
                // Se usa applicationContext para no retener una Activity en memoria.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rincon_sur_database"
                )
                    // Proyecto académico (MVP): al cambiar la versión se recrea la BD
                    // Si la versión cambió y no hay migración escrita, Room BORRA y RECREA la BD.
                    // Válido para un MVP académico (se pierden los datos anteriores).
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Datos base. INSERT OR IGNORE: se ejecuta en cada apertura sin duplicar
        // (también se recargan después de una migración destructiva).
        // seedCallback: onOpen se ejecuta CADA VEZ que se abre la base de datos.
        // Usa "INSERT OR IGNORE": si la fila ya existe (mismo id o mismo email único),
        // SQLite la ignora, así nunca se duplican los datos base.
        private val seedCallback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Los 4 accesos existen siempre. Nombres y ubicaciones son de ejemplo.
                db.execSQL(
                    "INSERT OR IGNORE INTO accesos (id, nombreAcceso, ubicacionDescripcion) VALUES " +
                            "(1, 'Acceso 1', 'Mall - entrada principal'), " +
                            "(2, 'Acceso 2', 'Mall - entrada lateral'), " +
                            "(3, 'Acceso 3', 'Tienda vecina (mejoramiento del hogar)'), " +
                            "(4, 'Acceso 4', 'Estacionamiento')"
                )
                // Usuarios de prueba (el email es único, por eso OR IGNORE no los duplica)
                db.execSQL(
                    // Usuarios de prueba. Los enums se escriben como texto ('ADMINISTRADOR', 'COMPRA'),
                    // que es exactamente como Room los guarda.
                    "INSERT OR IGNORE INTO usuarios " +
                            "(nombre, email, password, rol, tipoCliente, rangoEtario, fotoPerfilUri) VALUES " +
                            "('Administrador', 'admin@rinconsur.cl', 'admin123', 'ADMINISTRADOR', NULL, 'SIN_DATO', NULL), " +
                            "('Cliente Demo', 'cliente@rinconsur.cl', 'cliente123', 'CLIENTE', 'COMPRA', 'ADULTO_30_44', NULL)"
                )
            }
        }
    }
}
