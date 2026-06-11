package com.example.proyectomoviles

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

// EL TRUCO INFALIBLE: Le pasamos una función de clic opcional al adaptador
class EventAdapter(
    private val listaEventos: List<Evento>,
    private val onEventoClick: ((Evento, Int) -> Unit)? = null
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvCategoria: TextView = vista.findViewById(R.id.tv_card_category)
        val tvDescripcion: TextView = vista.findViewById(R.id.tv_card_description)
        val tvFecha: TextView = vista.findViewById(R.id.tv_card_datetime)
        val tvStatus: TextView = vista.findViewById(R.id.tv_card_status)
        val tvContacto: TextView = vista.findViewById(R.id.tv_card_contact)
        val tvLocation: TextView = vista.findViewById(R.id.tv_card_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_event_card, parent, false)
        return EventViewHolder(vista)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val evento = listaEventos[position]

        holder.tvCategoria.text = evento.categoria
        holder.tvDescripcion.text = evento.descripcion
        holder.tvFecha.text = evento.fecha
        holder.tvContacto.text = evento.contacto

        holder.tvLocation.text = evento.ubicacion.ifEmpty { "ESCOM IPN" }

        val estadoActual = if (evento.estatus.isEmpty()) "Pendiente" else evento.estatus
        holder.tvStatus.text = estadoActual

        val estadoMinuscula = estadoActual.lowercase(Locale.ROOT)

        when (estadoMinuscula) {
            "realizado" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            "aplazado" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
            else -> holder.tvStatus.setTextColor(Color.parseColor("#FFB300"))
        }

        // ASIGNACIÓN DIRECTA DEL CLIC A LA TARJETA (Ignora el ScrollView)
        holder.itemView.setOnClickListener {
            onEventoClick?.invoke(evento, position)
        }
    }

    override fun getItemCount(): Int = listaEventos.size
}