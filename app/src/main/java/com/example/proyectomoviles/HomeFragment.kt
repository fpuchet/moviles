package com.example.proyectomoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. Inflar la vista de fragment_home
        val vista = inflater.inflate(R.layout.fragment_home, container, false)

        // 2. Crear datos de ejemplo (Eventos para el día actual y los próximos 4 días)
        val listaDePrueba = listOf(
            Evento("Entrega de Proyecto", "Llevar el avance de las pantallas fijas del organizador.", "Hoy - 12:00", "Profesor Móviles", "Pendiente"),
            Evento("Examen", "Evaluación del segundo parcial de compiladores.", "Mañana - 08:30", "Academia", "Pendiente"),
            Evento("Junta", "Reunión de equipo para definir requerimientos del sistema.", "08/06 - 16:00", "Carlos Ortega", "Realizado"),
            Evento("Cita", "Revisión médica general programada.", "09/06 - 11:00", "Dr. Martínez", "Aplazado"),
            Evento("Otros", "Comprar los timbres/stamps para el álbum del mundial.", "10/06 - 18:00", "Tienda", "Pendiente")
        )

        // 3. Vincular el RecyclerView del XML y asignarle un formato lineal (vertical)
        val recyclerView: RecyclerView = vista.findViewById(R.id.rv_events_home)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 4. Instanciar el adaptador con los datos y asignárselo al RecyclerView
        val adaptador = EventAdapter(listaDePrueba)
        recyclerView.adapter = adaptador



        return vista
    }

    // Función encapsulada para abrir el cuadro de diálogo Dark Premium (Consistencia UX)
    private fun abrirDialogoModificacion() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_event, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // 1. Configurar e inflar el Spinner de Estatus del diálogo
        val spinnerStatus: Spinner = dialogView.findViewById(R.id.sp_dialog_status)
        val listaEstados = listOf("Pendiente", "Realizado", "Aplazado")
        val adapterEstados = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaEstados)
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapterEstados

        // 2. Lógica del botón "Actualizar"
        dialogView.findViewById<MaterialButton>(R.id.btn_dialog_update).setOnClickListener {
            Toast.makeText(requireContext(), "Evento actualizado desde Inicio", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // 3. Lógica del botón "Eliminar"
        dialogView.findViewById<MaterialButton>(R.id.btn_dialog_delete).setOnClickListener {
            Toast.makeText(requireContext(), "Evento eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Mostrar la ventana emergente modificadora
        dialog.show()
    }
}