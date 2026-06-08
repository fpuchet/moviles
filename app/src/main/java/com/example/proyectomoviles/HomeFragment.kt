package com.example.proyectomoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. Inflar la vista de fragment_home
        val vista = inflater.inflate(R.layout.fragment_home, container, false)

        // 2. Crear datos de ejemplo (Citas, Juntas, Exámenes, etc. para los próximos días)
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
}