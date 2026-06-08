package com.example.proyectomoviles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private val listaEventos: List<Evento>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    // 1. Inflar el diseño de la tarjeta individual (item_event_card.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_card, parent, false)
        return EventViewHolder(vista)
    }

    // 2. Conectar los datos de un evento específico con los TextViews de la tarjeta
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val evento = listaEventos[position]
        holder.tvCategory.text = evento.categoria
        holder.tvDescription.text = evento.descripcion
        holder.tvDateTime.text = evento.fechaHora
        holder.tvContact.text = evento.contacto
        holder.tvStatus.text = evento.status

        // Cambiar el color de fondo del Status dinámicamente según el estado
        val contexto = holder.itemView.context
        when (evento.status.lowercase()) {
            "realizado" -> holder.tvStatus.setBackgroundColor(contexto.getColor(R.color.status_completed))
            "aplazado" -> holder.tvStatus.setBackgroundColor(contexto.getColor(R.color.status_delayed))
            else -> holder.tvStatus.setBackgroundColor(contexto.getColor(R.color.status_pending))
        }
    }

    override fun getItemCount(): Int = listaEventos.size

    // 3. Clase interna corregida: Vinculación directa y limpia de componentes sin errores de tipo
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tv_card_category)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_card_status)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_card_description)
        val tvDateTime: TextView = itemView.findViewById(R.id.tv_card_datetime)
        val tvContact: TextView = itemView.findViewById(R.id.tv_card_contact)
    }
}