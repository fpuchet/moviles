package com.example.proyectomoviles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class CalendarDayAdapter(
    private val diasDelMes: List<String>,
    private val eventos: List<Evento>,
    private val mesAnoActual: String, // Ejemplo: "06/2026"
    private val onDayClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.CalendarDayViewHolder>() {

    class CalendarDayViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvDayNumber: TextView = vista.findViewById(R.id.tv_day_number)

        // Mapear los 3 círculos independientes
        val dotPending: View = vista.findViewById(R.id.dot_pending)
        val dotCompleted: View = vista.findViewById(R.id.dot_completed)
        val dotDelayed: View = vista.findViewById(R.id.dot_delayed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarDayViewHolder(vista)
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        val dia = diasDelMes[position]
        holder.tvDayNumber.text = dia

        // Apagar todos los puntos por defecto (para limpiar el reciclaje de celdas)
        holder.dotPending.visibility = View.GONE
        holder.dotCompleted.visibility = View.GONE
        holder.dotDelayed.visibility = View.GONE

        if (dia.isNotEmpty()) {
            val diaFormateado = String.format("%02d", dia.toInt())
            val fechaCompleta = "$diaFormateado/$mesAnoActual"

            // Buscar todos los eventos que pertenezcan a este día
            val eventosDelDia = eventos.filter { it.fecha == fechaCompleta }

            if (eventosDelDia.isNotEmpty()) {
                // Validación independiente por cada estado (se pueden encender 1, 2 o los 3 juntos)
                val tienePendientes = eventosDelDia.any { it.estatus.lowercase(Locale.ROOT) == "pendiente" }
                val tieneRealizados = eventosDelDia.any { it.estatus.lowercase(Locale.ROOT) == "realizado" }
                val tieneAplazados = eventosDelDia.any { it.estatus.lowercase(Locale.ROOT) == "aplazado" }

                if (tienePendientes) holder.dotPending.visibility = View.VISIBLE
                if (tieneRealizados) holder.dotCompleted.visibility = View.VISIBLE
                if (tieneAplazados)  holder.dotDelayed.visibility = View.VISIBLE
            }

            holder.itemView.setOnClickListener { onDayClick(fechaCompleta) }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = diasDelMes.size
}