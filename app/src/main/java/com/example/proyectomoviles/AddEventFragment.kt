package com.example.proyectomoviles

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.provider.ContactsContract
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.util.Locale

class AddEventFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper

    // Variables asignadas exactamente a tus tipos de componentes del XML
    private lateinit var btnSelectContact: MaterialButton
    private lateinit var btnSelectLocation: MaterialButton

    private var contactoSeleccionado: String = ""
    private var ubicacionSeleccionada: String = "ESCOM IPN" // Valor base por defecto

    // Registrador moderno para capturar el contacto seleccionado de la agenda nativa
    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data ?: return@registerForActivityResult
            val proyeccion = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

            try {
                val cursor = requireContext().contentResolver.query(contactUri, proyeccion, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val columnaNombre = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                    val nombreContacto = cursor.getString(columnaNombre)

                    // Guardar el valor internamente y actualizar visualmente tu MaterialButton
                    contactoSeleccionado = nombreContacto
                    btnSelectContact.text = "Contacto: $nombreContacto"
                }
                cursor?.close()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al recuperar el contacto de la agenda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_add_event, container, false)

        // 0. Inicializar el ayudante de la Base de Datos local
        dbHelper = DatabaseHelper(requireContext())

        // 1. Configurar Spinner de Recordatorios (Sincronizado con sp_reminder)
        val spinnerReminder: Spinner = vista.findViewById(R.id.sp_reminder)
        val adaptadorReminder = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.reminder_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerReminder.adapter = adaptadorReminder

        // 2. Lógica interactiva original de los Chips de Categorías (cg_categories)
        val chipGroup: ChipGroup = vista.findViewById(R.id.cg_categories)
        var categoriaSeleccionada = "Cita"

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
                categoriaSeleccionada = selectedChip.text.toString()
            }
        }

        // 3. Selector de Fecha (et_event_date)
        val etEventDate: TextInputEditText = vista.findViewById(R.id.et_event_date)
        etEventDate.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, anioSeleccionado, mesSeleccionado, diaSeleccionado ->
                    val fechaFormateada = String.format(Locale.getDefault(), "%02d/%02d/%d", diaSeleccionado, mesSeleccionado + 1, anioSeleccionado)
                    etEventDate.setText(fechaFormateada)
                },
                anio, mes, dia
            )
            datePicker.show()
        }

        // 4. Selector de Hora (et_event_time)
        val etEventTime: TextInputEditText = vista.findViewById(R.id.et_event_time)
        etEventTime.setOnClickListener {
            val calendario = Calendar.getInstance()
            val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
            val minutoActual = calendario.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(
                requireContext(),
                { _, horaSeleccionada, minutoSeleccionado ->
                    val horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", horaSeleccionada, minutoSeleccionado)
                    etEventTime.setText(horaFormateada)
                },
                horaActual, minutoActual, true
            )
            timePicker.show()
        }

        // 5. Configurar Spinner de Estatus (sp_add_status)
        val spinnerStatus: Spinner = vista.findViewById(R.id.sp_add_status)
        val listaEstados = listOf("Pendiente", "Realizado", "Aplazado")
        val adapterEstados = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaEstados)
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapterEstados

        // 6. ASIGNACIÓN EXACTA DE TUS BOTONES DEL XML
        btnSelectContact = vista.findViewById(R.id.btn_select_contact)
        btnSelectLocation = vista.findViewById(R.id.btn_select_location)

        // Evento de clic para disparar la agenda de contactos nativa de la tablet
        btnSelectContact.setOnClickListener {
            try {
                val intentContactos = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                pickContactLauncher.launch(intentContactos)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No se pudo abrir la agenda de contactos", Toast.LENGTH_SHORT).show()
            }
        }

        // Evento de clic para la ubicación (Como marca la indicación: "puede faltar el mapa")
        btnSelectLocation.setOnClickListener {
            ubicacionSeleccionada = "Laboratorio de Móviles ESCOM"
            btnSelectLocation.text = "Ubicación: $ubicacionSeleccionada"
            Toast.makeText(requireContext(), "Ubicación fijada de forma local", Toast.LENGTH_SHORT).show()
        }

        // 7. VALIDACIÓN, CAPTURA Y GUARDADO REAL EN SQLITE
        val etEventDescription: TextInputEditText = vista.findViewById(R.id.et_event_description)
        val btnSaveEvent: MaterialButton = vista.findViewById(R.id.btn_save_event)

        btnSaveEvent.setOnClickListener {
            val fecha = etEventDate.text.toString().trim()
            val hora = etEventTime.text.toString().trim()
            val descripcion = etEventDescription.text.toString().trim()
            val estatus = spinnerStatus.selectedItem.toString()
            val recordatorio = spinnerReminder.selectedItem.toString()

            // REGLAS DE VALIDACIÓN BASADAS EN TU ESTRUCTURA
            if (fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, selecciona una fecha para el evento", Toast.LENGTH_SHORT).show()
            } else if (hora.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, selecciona una hora para el evento", Toast.LENGTH_SHORT).show()
            } else if (descripcion.isEmpty()) {
                Toast.makeText(requireContext(), "La descripción del evento no puede quedar vacía", Toast.LENGTH_SHORT).show()
            } else if (contactoSeleccionado.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, selecciona un contacto de la agenda", Toast.LENGTH_SHORT).show()
            } else {
                // GUARDADO FÍSICO DIRECTO EN TU BASE DE DATOS SQLITE
                val idGenerado = dbHelper.insertarEvento(
                    categoria = categoriaSeleccionada,
                    fecha = fecha,
                    hora = hora,
                    descripcion = descripcion,
                    estatus = estatus,
                    ubicacion = ubicacionSeleccionada,
                    contacto = contactoSeleccionado,
                    recordatorio = recordatorio
                )

                if (idGenerado != -1L) {

                    val partesFecha = fecha.split("/")
                    val partesHora = hora.split(":")

                    val calendarioEvento = Calendar.getInstance().apply {
                        set(
                            partesFecha[2].toInt(),           // año
                            partesFecha[1].toInt() - 1,       // mes
                            partesFecha[0].toInt(),           // día
                            partesHora[0].toInt(),           // hora
                            partesHora[1].toInt(),           // minuto
                            0
                        )
                    }
                    Toast.makeText(
                        requireContext(),
                        "Recordatorio seleccionado: $recordatorio",
                        Toast.LENGTH_LONG
                    ).show()

                    NotificationManagerHelper(requireContext())
                        .programarRecordatorio(
                            idEvento = idGenerado.toInt(),
                            titulo = categoriaSeleccionada,
                            descripcion = descripcion,
                            calendarTarget = calendarioEvento,
                            tipoRecordatorio = recordatorio
                        )
                    val mensajeExito = "¡Evento Guardado en SQLite!\nID: $idGenerado | Con: $contactoSeleccionado"

                    Toast.makeText(requireContext(), mensajeExito, Toast.LENGTH_LONG).show()

                    // Limpiar el formulario completo tras guardar de forma exitosa
                    etEventDate.setText("")
                    etEventTime.setText("")
                    etEventDescription.setText("")
                    contactoSeleccionado = ""
                    btnSelectContact.text = "Seleccionar contacto de la agenda"
                    btnSelectLocation.text = "Seleccionar ubicación en el mapa"

                    // Regresar a la pantalla anterior
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error crítico al guardar en SQLite", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return vista
    }
}