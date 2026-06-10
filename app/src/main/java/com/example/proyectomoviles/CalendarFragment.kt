package com.example.proyectomoviles

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapterEvents: EventAdapter
    private val listaFiltradaMutable = mutableListOf<Evento>()
    private val todasLasTareasDeSQLite = mutableListOf<Evento>()
    private var fechaSeleccionadaActual: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_calendar, container, false)

        // 0. Inicializar el ayudante de la Base de Datos SQLite local
        dbHelper = DatabaseHelper(requireContext())

        // 1. Vincular tus componentes XML originales con sus IDs exactos
        val rvCalendarGrid: RecyclerView = vista.findViewById(R.id.rv_calendar_grid)
        val tvTituloDinamico: TextView = vista.findViewById(R.id.tv_calendar_selected_title)
        val rvEvents: RecyclerView = vista.findViewById(R.id.rv_calendar_events)

        // 2. AUTOMATIZACIÓN: Calcular dinámicamente la fecha de hoy (Formato dd/MM/yyyy)
        val formateador = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        fechaSeleccionadaActual = formateador.format(Calendar.getInstance().time)

        // 3. Recuperar todos los eventos de la BD para pintar los puntitos en la cuadrícula
        cargarTodosLosEventosDeSQLite()

        // 4. Configurar tu cuadrícula personalizada de 7 columnas
        rvCalendarGrid.layoutManager = GridLayoutManager(requireContext(), 7)

        // Generar días para Junio 2026 (Empieza en Lunes, espacio en blanco para el Domingo)
        val diasDeJunio = mutableListOf("")
        for (i in 1..30) diasDeJunio.add(i.toString())

        // Inicializar el adaptador de celdas pasándole la lista dinámica de la BD
        val calendarAdapter = CalendarDayAdapter(diasDeJunio, todasLasTareasDeSQLite, "06/2026") { fecha ->
            fechaSeleccionadaActual = fecha
            tvTituloDinamico.text = "Eventos del $fecha:"
            filtrarEventosPorFecha(fecha)
        }
        rvCalendarGrid.adapter = calendarAdapter

        // 5. Configurar la lista de eventos de abajo (RecyclerView)
        rvEvents.layoutManager = LinearLayoutManager(requireContext())
        adapterEvents = EventAdapter(listaFiltradaMutable)
        rvEvents.adapter = adapterEvents

        // 6. AUTOMATIZACIÓN AL ENTRAR: Mostrar directamente las tareas de HOY sin esperar el clic
        tvTituloDinamico.text = "Eventos del $fechaSeleccionadaActual:"
        filtrarEventosPorFecha(fechaSeleccionadaActual)

        return vista
    }

    /**
     * Consulta la base de datos de manera global para que el CalendarDayAdapter
     * sepa qué días tienen tareas y pueda encender los 3 puntitos de estatus.
     */
    private fun cargarTodosLosEventosDeSQLite() {
        todasLasTareasDeSQLite.clear()

        // Consultamos todo el mes de junio usando el método avanzado de tu helper
        val cursor: Cursor = dbHelper.consultarEventosAvanzado("por mes", null, null, "06", "2026", "todos")

        if (cursor.moveToFirst()) {
            do {
                val categoria = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORIA))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FECHA))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPCION))
                val estatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESTATUS))
                val contacto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACTO))

                val evento = Evento(categoria, descripcion, fecha, contacto, estatus)
                todasLasTareasDeSQLite.add(evento)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    /**
     * Filtra los eventos de la fecha seleccionada leyendo directamente de SQLite
     */
    private fun filtrarEventosPorFecha(fecha: String) {
        listaFiltradaMutable.clear()

        // CORRECCIÓN: Llamada exacta al método en español de tu DatabaseHelper
        val cursor: Cursor = dbHelper.obtenerEventosPorFecha(fecha)

        if (cursor.moveToFirst()) {
            do {
                val categoria = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORIA))
                val fechaReg = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FECHA))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPCION))
                val estatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESTATUS))
                val contacto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACTO))

                val evento = Evento(categoria, descripcion, fechaReg, contacto, estatus)
                listaFiltradaMutable.add(evento)
            } while (cursor.moveToNext())
        }
        cursor.close()

        adapterEvents.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        // Recargar de SQLite por si el usuario guardó un evento nuevo y regresó a esta pantalla
        cargarTodosLosEventosDeSQLite()

        // Refrescar la cuadrícula del calendario completa para actualizar los puntitos
        val rvCalendarGrid: RecyclerView? = view?.findViewById(R.id.rv_calendar_grid)
        rvCalendarGrid?.adapter?.notifyDataSetChanged()

        // Refrescar la lista de tareas del día que estaba seleccionado
        if (fechaSeleccionadaActual.isNotEmpty()) {
            filtrarEventosPorFecha(fechaSeleccionadaActual)
        }
    }
}