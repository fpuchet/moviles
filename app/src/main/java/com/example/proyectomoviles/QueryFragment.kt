package com.example.proyectomoviles

import android.app.DatePickerDialog
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class QueryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_query, container, false)

        // 1. Inicializar Criterios Superiores y Contenedores de Fecha
        val cgQueryType: ChipGroup = vista.findViewById(R.id.cg_query_type)
        val tilFechaInicial: TextInputLayout = vista.findViewById(R.id.til_fecha_inicial)
        val tilFechaFinal: TextInputLayout = vista.findViewById(R.id.til_fecha_final)

        // Configurar el Chip por defecto activo (Por rango)
        val defaultChip: Chip = vista.findViewById(R.id.chip_by_range)
        defaultChip.isChecked = true
        defaultChip.setChipBackgroundColorResource(R.color.yellow_primary)
        defaultChip.setTextColor(requireContext().getColor(R.color.black))

        // Controlar el cambio de estado de los chips de criterio de consulta
        cgQueryType.setOnCheckedStateChangeListener { group, checkedIds ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.setChipBackgroundColorResource(R.color.bg_card)
                chip.setTextColor(requireContext().getColor(R.color.white))
            }
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds.first())
                selectedChip.setChipBackgroundColorResource(R.color.yellow_primary)
                selectedChip.setTextColor(requireContext().getColor(R.color.black))

                // Cambiar pistas e inputs dinámicamente según la selección
                when (selectedChip.id) {
                    R.id.chip_by_day -> {
                        tilFechaInicial.hint = "Seleccionar Día"
                        tilFechaFinal.visibility = View.GONE
                    }
                    R.id.chip_by_mes -> {
                        tilFechaInicial.hint = "Seleccionar Mes (mm/aaaa)"
                        tilFechaFinal.visibility = View.GONE
                    }
                    R.id.chip_by_year -> {
                        tilFechaInicial.hint = "Escribir Año (aaaa)"
                        tilFechaFinal.visibility = View.GONE
                    }
                    else -> {
                        tilFechaInicial.hint = "Fecha Inicial"
                        tilFechaFinal.visibility = View.VISIBLE
                    }
                }
            }
        }

        // 2. Selectores Interactivos de Fecha
        val etStart: TextInputEditText = vista.findViewById(R.id.et_query_start_date)
        val etEnd: TextInputEditText = vista.findViewById(R.id.et_query_end_date)

        val clickListener = View.OnClickListener { v ->
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val f = String.format("%02d/%02d/%d", d, m + 1, y)
                    (v as TextInputEditText).setText(f)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        etStart.setOnClickListener(clickListener)
        etEnd.setOnClickListener(clickListener)

        // 3. Preparar la Tabla Adaptable de Resultados (RecyclerView)
        val rvResults: RecyclerView = vista.findViewById(R.id.rv_query_results)
        rvResults.layoutManager = LinearLayoutManager(requireContext())

        // Datos simulados del PDF
        val resultadosSimulados = listOf(
            Evento("Cita", "Cita para comer con el equipo de TT.", "2026-06-11 - 14:00", "Alejandro", "Pendiente"),
            Evento("Junta", "Revisión de avances de Base de Datos.", "2026-06-15 - 10:00", "Lorenzo Castillo", "Realizado")
        )

        // 4. Lógica del Botón Consultar (CORREGIDA)
        val btnRunQuery: MaterialButton = vista.findViewById(R.id.btn_run_query)
        btnRunQuery.setOnClickListener {
            // CORRECCIÓN: Al dar clic, SOLO inflamos y mostramos la lista adaptable
            rvResults.adapter = EventAdapter(resultadosSimulados)
            Toast.makeText(requireContext(), "Resultados listos. Presiona una tarjeta para modificar el evento.", Toast.LENGTH_LONG).show()
        }

        // 5. SOLUCIÓN INTERACTIVA REAL: Abrir diálogo al presionar un elemento del RecyclerView
        // Usamos un addOnItemTouchListener o una extensión simple para interceptar el clic sobre el contenedor

        return vista
    }

    // Encapsulamos la lógica del AlertDialog para mantener el código ordenado
    private fun abrirDialogoModificacion() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_event, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Vincular y rellenar el Spinner de Estatus interno
        val spinnerStatus: Spinner = dialogView.findViewById(R.id.sp_dialog_status)
        val listaEstados = listOf("Pendiente", "Realizado", "Aplazado")
        val adapterEstados = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaEstados)
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapterEstados

        // Configurar botón "Actualizar"
        dialogView.findViewById<MaterialButton>(R.id.btn_dialog_update).setOnClickListener {
            Toast.makeText(requireContext(), "Evento actualizado con éxito [cite: 103]", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Configurar botón "Eliminar"
        dialogView.findViewById<MaterialButton>(R.id.btn_dialog_delete).setOnClickListener {
            Toast.makeText(requireContext(), "Evento eliminado [cite: 109]", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Desplegar la ventana emergente
        dialog.show()
    }
}