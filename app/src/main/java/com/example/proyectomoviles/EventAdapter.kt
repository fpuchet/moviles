package com.example.proyectomoviles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class EventAdapter(private val listaEventos: List<Evento>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvCategory: TextView = vista.findViewById(R.id.tv_card_category)
        val tvStatus: TextView = vista.findViewById(R.id.tv_card_status)
        val tvDescription: TextView = vista.findViewById(R.id.tv_card_description)
        val tvDateTime: TextView = vista.findViewById(R.id.tv_card_datetime)
        val tvContact: TextView = vista.findViewById(R.id.tv_card_contact)
        val tvLocation: TextView = vista.findViewById(R.id.tv_card_location)
        val btnLocationClick: LinearLayout = vista.findViewById(R.id.btn_card_location_click)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_card, parent, false)
        return EventViewHolder(vista)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val evento = listaEventos[position]

        // 1. Pintar los datos en los componentes visuales
        holder.tvCategory.text = evento.categoria
        holder.tvDescription.text = evento.descripcion
        holder.tvDateTime.text = evento.fecha
        holder.tvContact.text = evento.contacto

        val estadoActual = if (evento.estatus.isEmpty()) "Pendiente" else evento.estatus
        holder.tvStatus.text = estadoActual

        val ubicacionFinal = if (evento.ubicacion == "No especificada" || evento.ubicacion.isEmpty()) {
            "ESCOM IPN"
        } else {
            evento.ubicacion
        }
        holder.tvLocation.text = ubicacionFinal

        // 2. Controlar los colores de las etiquetas de estatus
        val contexto = holder.itemView.context
        val estadoMinuscula = estadoActual.lowercase(Locale.ROOT)

        try {
            when (estadoMinuscula) {
                "pendiente" -> holder.tvStatus.setBackgroundColor(contexto.getColor(R.color.status_pending))
                "realizado" -> holder.tvStatus.setBackgroundColor(contexto.getColor(R.color.status_completed))
                else -> holder.tvStatus.setBackgroundColor(contexto.getColor(R.color.status_delayed))
            }
            holder.tvStatus.setTextColor(contexto.getColor(R.color.black))
        } catch (e: Exception) {
            holder.tvStatus.setBackgroundColor(android.graphics.Color.YELLOW)
            holder.tvStatus.setTextColor(android.graphics.Color.BLACK)
        }

        // 3. CLIC EN LA TARJETA COMPLETA: Abre el diálogo de edición (Moverlo aquí evita el choque)
        holder.itemView.setOnClickListener { view ->
            // Invocamos la ventana emergente de modificación de forma segura
            val inflater = LayoutInflater.from(view.context)
            val dialogView = inflater.inflate(R.layout.dialog_edit_event, null)
            val dialog = AlertDialog.Builder(view.context)
                .setView(dialogView)
                .create()

            // Configurar el spinner del diálogo
            val spinnerStatus: android.widget.Spinner = dialogView.findViewById(R.id.sp_dialog_status)
            val listaEstados = listOf("Pendiente", "Realizado", "Aplazado")
            val adapterEstados = android.widget.ArrayAdapter(view.context, android.R.layout.simple_spinner_item, listaEstados)
            adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = adapterEstados

            // Botón actualizar del diálogo
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_dialog_update).setOnClickListener {
                Toast.makeText(view.context, "Evento actualizado con éxito", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            // Botón eliminar del diálogo
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_dialog_delete).setOnClickListener {
                Toast.makeText(view.context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        }

        // 4. CLIC EXCLUSIVO DEL MAPA: Al procesar el clic aquí, anulamos la propagación al padre
        holder.btnLocationClick.setOnClickListener { view ->
            Toast.makeText(
                view.context,
                " Redirigiendo a Google Maps...\nDestino: $ubicacionFinal",
                Toast.LENGTH_LONG
            ).show()

            // IMPORTANTE: Esto le dice a Android que el clic muere aquí y no debe activar el holder.itemView
            view.clearFocus()
        }
    }

    override fun getItemCount(): Int = listaEventos.size
}