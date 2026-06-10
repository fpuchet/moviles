package com.example.proyectomoviles

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Organizador.db"
        private const val DATABASE_VERSION = 1

        // Nombre de la tabla y columnas constantes
        const val TABLA_EVENTOS = "eventos"
        const val COL_ID = "id"
        const val COL_CATEGORIA = "categoria"
        const val COL_FECHA = "fecha"
        const val COL_HORA = "hora"
        const val COL_DESCRIPCION = "descripcion"
        const val COL_ESTATUS = "estatus"
        const val COL_UBICACION = "ubicacion"
        const val COL_CONTACTO = "contacto"
        const val COL_RECORDATORIO = "recordatorio"
    }

    // Se ejecuta la primera vez que la app intenta acceder a la base de datos
    override fun onCreate(db: SQLiteDatabase?) {
        val crearTabla = """
            CREATE TABLE $TABLA_EVENTOS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CATEGORIA TEXT,
                $COL_FECHA TEXT,
                $COL_HORA TEXT,
                $COL_DESCRIPCION TEXT,
                $COL_ESTATUS TEXT,
                $COL_UBICACION TEXT,
                $COL_CONTACTO TEXT,
                $COL_RECORDATORIO TEXT
            )
        """.trimIndent()

        db?.execSQL(crearTabla)
        Log.d("DATABASE", "Tabla $TABLA_EVENTOS creada con éxito de forma física.")
    }

    // Se ejecuta cuando cambias la versión de la base de datos
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_EVENTOS")
        onCreate(db)
    }

    // =========================================================================
    //               MÉTODOS OPERATIVOS COMPLETOS (CRUD REAL)
    // =========================================================================

    /**
     * 1. INSERCIÓN: Guarda un evento nuevo en la base de datos.
     * Retorna el ID de la fila generada o -1 si ocurrió un error.
     */
    fun insertarEvento(
        categoria: String, fecha: String, hora: String, descripcion: String,
        estatus: String, ubicacion: String, contacto: String, recordatorio: String
    ): Long {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(COL_CATEGORIA, categoria)
            put(COL_FECHA, fecha)
            put(COL_HORA, hora)
            put(COL_DESCRIPCION, descripcion)
            put(COL_ESTATUS, estatus)
            put(COL_UBICACION, ubicacion)
            put(COL_CONTACTO, contacto)
            put(COL_RECORDATORIO, recordatorio)
        }

        val resultado = db.insert(TABLA_EVENTOS, null, valores)
        db.close()
        return resultado
    }

    /**
     * 2. CONSULTA ESPECÍFICA POR FECHA: Retorna un Cursor con las tareas del día.
     * Utilizado para alimentar la lista del Inicio y la del Calendario.
     */
    fun obtenerEventosPorFecha(fechaSeleccionada: String): Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLA_EVENTOS WHERE $COL_FECHA = ?"
        return db.rawQuery(query, arrayOf(fechaSeleccionada))
    }

    /**
     * 3. CONSULTA FILTRADA AVANZADA (Sección Consultas):
     * Permite buscar por rangos, mes, año y categoría usando parámetros dinámicos.
     */
    fun consultarEventosAvanzado(
        tipoConsulta: String,
        fechaInicio: String?,
        fechaFin: String?,
        mes: String?,
        ano: String?,
        categoria: String?
    ): Cursor {
        val db = this.readableDatabase
        var query = "SELECT * FROM $TABLA_EVENTOS WHERE 1=1"
        val argumentos = mutableListOf<String>()

        // Aplicar el filtro según el botón de criterio seleccionado
        when (tipoConsulta.lowercase()) {
            "por rango" -> {
                if (!fechaInicio.isNullOrEmpty() && !fechaFin.isNullOrEmpty()) {
                    query += " AND $COL_FECHA BETWEEN ? AND ?"
                    argumentos.add(fechaInicio)
                    argumentos.add(fechaFin)
                }
            }
            "por mes" -> {
                if (!mes.isNullOrEmpty() && !ano.isNullOrEmpty()) {
                    // Espera que la fecha esté guardada como dd/mm/aaaa, filtra por el patrón /mm/aaaa
                    query += " AND $COL_FECHA LIKE ?"
                    argumentos.add("%/$mes/$ano")
                }
            }
            "por año" -> {
                if (!ano.isNullOrEmpty()) {
                    query += " AND $COL_FECHA LIKE ?"
                    argumentos.add("%/%/$ano")
                }
            }
            "por dia" -> {
                if (!fechaInicio.isNullOrEmpty()) {
                    query += " AND $COL_FECHA = ?"
                    argumentos.add(fechaInicio)
                }
            }
        }

        // Filtro secundario opcional por tipo de evento (Cita, Junta, Examen, etc.)
        if (!categoria.isNullOrEmpty() && categoria.lowercase() != "todos") {
            query += " AND $COL_CATEGORIA = ?"
            argumentos.add(categoria)
        }

        return db.rawQuery(query, argumentos.toTypedArray())
    }

    /**
     * 4. ACTUALIZACIÓN (Modificación de eventos):
     * Modifica el estatus, contacto, ubicación o cualquier metadato de un evento existente.
     */
    fun actualizarEvento(
        id: Int, categoria: String, fecha: String, hora: String, descripcion: String,
        estatus: String, ubicacion: String, contacto: String, recordatorio: String
    ): Int {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(COL_CATEGORIA, categoria)
            put(COL_FECHA, fecha)
            put(COL_HORA, hora)
            put(COL_DESCRIPCION, descripcion)
            put(COL_ESTATUS, estatus)
            put(COL_UBICACION, ubicacion)
            put(COL_CONTACTO, contacto)
            put(COL_RECORDATORIO, recordatorio)
        }

        val filasAfectadas = db.update(TABLA_EVENTOS, valores, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return filasAfectadas
    }

    /**
     * 5. ELIMINACIÓN: Borra definitivamente un evento de la base de datos por su ID.
     */
    fun eliminarEvento(id: Int): Int {
        val db = this.writableDatabase
        val filasEliminadas = db.delete(TABLA_EVENTOS, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return filasEliminadas
    }
}