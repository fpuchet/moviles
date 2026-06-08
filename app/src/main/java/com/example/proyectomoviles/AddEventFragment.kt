package com.example.proyectomoviles

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.Fragment
import java.util.Calendar

class AddEventFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_add_event, container, false)

        // 1. Configurar Spinner de Recordatorios
        val spinnerReminder: Spinner = vista.findViewById(R.id.sp_reminder)
        val adaptadorReminder = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.reminder_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerReminder.adapter = adaptadorReminder

        // 2. Lógica interactiva de los Chips de Categorías
        val chipGroup: ChipGroup = vista.findViewById(R.id.cg_categories)
        var categoriaSeleccionada = "Cita" // Valor por defecto inicial

        val defaultChip: Chip = vista.findViewById(R.id.chip_cita)
        defaultChip.isChecked = true
        defaultChip.setChipBackgroundColorResource(R.color.yellow_primary)
        defaultChip.setTextColor(requireContext().getColor(R.color.black))

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.setChipBackgroundColorResource(R.color.bg_card)
                chip.setTextColor(requireContext().getColor(R.color.white))
            }
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds.first())
                selectedChip.setChipBackgroundColorResource(R.color.yellow_primary)
                selectedChip.setTextColor(requireContext().getColor(R.color.black))
                categoriaSeleccionada = selectedChip.text.toString() // Guardamos el nombre de la categoría activa
            }
        }

        // 3. INTERACTIVIDAD: Selector de Fecha
        val etEventDate: TextInputEditText = vista.findViewById(R.id.et_event_date)
        etEventDate.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, anioSeleccionado, mesSeleccionado, diaSeleccionado ->
                    val fechaFormateada = String.format("%02d/%02d/%d", diaSeleccionado, mesSeleccionado + 1, anioSeleccionado)
                    etEventDate.setText(fechaFormateada)
                },
                anio, mes, dia
            )
            datePicker.show()
        }

        // 4. INTERACTIVIDAD: Selector de Hora
        val etEventTime: TextInputEditText = vista.findViewById(R.id.et_event_time)
        etEventTime.setOnClickListener {
            val calendario = Calendar.getInstance()
            val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
            val minutoActual = calendario.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(
                requireContext(),
                { _, horaSeleccionada, minutoSeleccionado ->
                    val horaFormateada = String.format("%02d:%02d", horaSeleccionada, minutoSeleccionado)
                    etEventTime.setText(horaFormateada)
                },
                horaActual, minutoActual, true
            )
            timePicker.show()
        }
        // 1. Vincular el Spinner de Estatus del XML
        val spinnerStatus: Spinner = vista.findViewById(R.id.sp_add_status)

        // 2. Definir los estados obligatorios del PDF
        val listaEstados = listOf("Pendiente", "Realizado", "Aplazado")

        // 3. Crear el adaptador nativo y asignarlo
        val adapterEstados = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaEstados)
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapterEstados
        // 5. VALIDACIÓN Y CAPTURA: Botón Guardar Evento
        val etEventDescription: TextInputEditText = vista.findViewById(R.id.et_event_description)
        val btnSaveEvent: MaterialButton = vista.findViewById(R.id.btn_save_event)

        btnSaveEvent.setOnClickListener {
            // Extraer el texto ingresado por el usuario quitando espacios vacíos extra (.trim())
            val fecha = etEventDate.text.toString().trim()
            val hora = etEventTime.text.toString().trim()
            val descripcion = etEventDescription.text.toString().trim()
            val recordatorio = spinnerReminder.selectedItem.toString()

            // REGLAS DE VALIDACIÓN DE CAMPOS OBLIGATORIOS
            if (fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, selecciona una fecha para el evento", Toast.LENGTH_SHORT).show()
            } else if (hora.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, selecciona una hora para el evento", Toast.LENGTH_SHORT).show()
            } else if (descripcion.isEmpty()) {
                Toast.makeText(requireContext(), "La descripción del evento no puede quedar vacía", Toast.LENGTH_SHORT).show()
            } else {
                // Si todo está lleno, simulamos una captura exitosa con toda la información limpia
                val mensajeExito = "¡Evento Guardado!\nCategoría: $categoriaSeleccionada\nFecha: $fecha - $hora\nNotificación: $recordatorio"
                Toast.makeText(requireContext(), mensajeExito, Toast.LENGTH_LONG).show()

                // Limpiar el campo de descripción después de guardar para que quede listo para otro registro
                etEventDescription.setText("")
            }
        }

        return vista
    }
}