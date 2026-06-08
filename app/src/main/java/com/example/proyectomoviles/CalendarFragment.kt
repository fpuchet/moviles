package com.example.proyectomoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class CalendarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_calendar, container, false)

        val calendarView: CalendarView = vista.findViewById(R.id.cv_events_calendar)
        val cardDetail: MaterialCardView = vista.findViewById(R.id.card_calendar_event_detail)
        val tvCategory: TextView = vista.findViewById(R.id.tv_cal_detail_category)
        val tvDesc: TextView = vista.findViewById(R.id.tv_cal_detail_desc)
        val tvInfo: TextView = vista.findViewById(R.id.tv_cal_detail_info)
        val tvStatus: TextView = vista.findViewById(R.id.tv_cal_detail_status)

        // Escuchar cuando el usuario cambia de día en el calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Formatear el día seleccionado
            val fechaSeleccionada = "$dayOfMonth/${month + 1}/$year"

            // Simulación requerida por el PDF: si es el día 11 (como el ejemplo del PDF), muestra un evento
            if (dayOfMonth == 11 || dayOfMonth == 15) {
                cardDetail.visibility = View.VISIBLE
                if (dayOfMonth == 11) {
                    tvCategory.text = "Cita"
                    tvDesc.text = "Cita para comer con el equipo de trabajo terminal."
                    tvInfo.text = "Hora: 14:00 | Contacto: Alejandro"
                    tvStatus.text = "Pendiente"
                    tvStatus.setBackgroundColor(requireContext().getColor(R.color.status_pending))
                } else {
                    tvCategory.text = "Junta"
                    tvDesc.text = "Revisión de requerimientos y diseño de base de datos."
                    tvInfo.text = "Hora: 10:00 | Contacto: Lorenzo Castillo"
                    tvStatus.text = "Realizado"
                    tvStatus.setBackgroundColor(requireContext().getColor(R.color.status_completed))
                }
                Toast.makeText(requireContext(), "Eventos para el: $fechaSeleccionada", Toast.LENGTH_SHORT).show()
            } else {
                // Si es cualquier otro día, ocultamos la tarjeta indicando que está libre
                cardDetail.visibility = View.GONE
                Toast.makeText(requireContext(), "No hay tareas programadas para el: $fechaSeleccionada", Toast.LENGTH_SHORT).show()
            }
        }

        return vista
    }
}