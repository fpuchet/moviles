package com.example.proyectomoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CalendarFragment : Fragment() {

    private lateinit var adapterEvents: EventAdapter
    private val listaFiltradaMutable = mutableListOf<Evento>()

    private val todasLasTareas = listOf(
        Evento("Entrega de Proyecto", "Llevar el avance de las pantallas fijas del organizador.", "08/06/2026", "Profesor Móviles", "Pendiente"),
        Evento("Examen", "Evaluación del segundo parcial de compiladores.", "09/06/2026", "Academia", "Pendiente"),
        Evento("Junta", "Revisión del modelado de datos.", "08/06/2026", "Carlos Ortega", "Realizado"),
        Evento("Cita", "Firma de documentos de servicio social.", "15/06/2026", "INIFED", "Aplazado")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_calendar, container, false)

        val rvCalendarGrid: RecyclerView = vista.findViewById(R.id.rv_calendar_grid)
        val tvTituloDinamico: TextView = vista.findViewById(R.id.tv_calendar_selected_title)
        val rvEvents: RecyclerView = vista.findViewById(R.id.rv_calendar_events)

        // 1. Configurar la cuadrícula del calendario (7 días de la semana)
        rvCalendarGrid.layoutManager = GridLayoutManager(requireContext(), 7)

        // Generar días para Junio 2026 (Empieza en Lunes, agregamos un espacio en blanco para el Domingo)
        val diasDeJunio = mutableListOf("")
        for (i in 1..30) diasDeJunio.add(i.toString())

        val calendarAdapter = CalendarDayAdapter(diasDeJunio, todasLasTareas, "06/2026") { fecha ->
            tvTituloDinamico.text = "Eventos del $fecha:"
            filtrarEventosPorFecha(fecha)
        }
        rvCalendarGrid.adapter = calendarAdapter

        // 2. Configurar la lista de eventos de abajo
        rvEvents.layoutManager = LinearLayoutManager(requireContext())
        adapterEvents = EventAdapter(listaFiltradaMutable)
        rvEvents.adapter = adapterEvents

        // Mostrar por defecto las tareas de hoy
        filtrarEventosPorFecha("08/06/2026")
        tvTituloDinamico.text = "Eventos del 08/06/2026:"

        return vista
    }

    private fun filtrarEventosPorFecha(fecha: String) {
        listaFiltradaMutable.clear()
        listaFiltradaMutable.addAll(todasLasTareas.filter { it.fecha == fecha })
        adapterEvents.notifyDataSetChanged()
    }
}