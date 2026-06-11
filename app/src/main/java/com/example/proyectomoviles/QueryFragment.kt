package com.example.proyectomoviles

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContracts
import android.app.DatePickerDialog
import android.database.Cursor
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
import java.util.Locale

class QueryFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private val listaResultadosDinamicos = mutableListOf<Evento>()
    private var adapterQuery: EventAdapter? = null

    private var criterioConsulta: String = "por rango"
    private var categoriaFiltro: String = "todos"

    private var etContactoActual: TextInputEditText? = null

    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            val contactUri = result.data?.data ?: return@registerForActivityResult

            try {

                val cursor = requireContext().contentResolver.query(
                    contactUri,
                    arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
                    null,
                    null,
                    null
                )

                if (cursor != null && cursor.moveToFirst()) {

                    val nombreContacto = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            ContactsContract.Contacts.DISPLAY_NAME
                        )
                    )

                    etContactoActual?.setText(nombreContacto)
                }

                cursor?.close()

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    "Error al recuperar contacto",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_query, container, false)

        dbHelper = DatabaseHelper(requireContext())

        val cgQueryType: ChipGroup = vista.findViewById(R.id.cg_query_type)
        val cgQueryCategories: ChipGroup = vista.findViewById(R.id.cg_query_categories)
        val tilFechaInicial: TextInputLayout = vista.findViewById(R.id.til_fecha_inicial)
        val tilFechaFinal: TextInputLayout = vista.findViewById(R.id.til_fecha_final)

        val etStart: TextInputEditText = vista.findViewById(R.id.et_query_start_date)
        val etEnd: TextInputEditText = vista.findViewById(R.id.et_query_end_date)

        vista.findViewById<Chip>(R.id.chip_by_range).isChecked = true
        vista.findViewById<Chip>(R.id.chip_q_all).isChecked = true

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

                criterioConsulta = selectedChip.text.toString().lowercase(Locale.getDefault())

                etStart.setText("")
                etEnd.setText("")

                when (selectedChip.id) {
                    R.id.chip_by_day -> {
                        tilFechaInicial.hint = "Seleccionar Día"
                        tilFechaFinal.visibility = View.GONE
                    }
                    R.id.chip_by_mes -> {
                        tilFechaInicial.hint = "Seleccionar Mes"
                        tilFechaFinal.visibility = View.GONE
                    }
                    R.id.chip_by_year -> {
                        tilFechaInicial.hint = "Seleccionar Año"
                        tilFechaFinal.visibility = View.GONE
                    }
                    else -> {
                        tilFechaInicial.hint = "Fecha Inicial"
                        tilFechaFinal.visibility = View.VISIBLE
                    }
                }
            }
        }

        cgQueryCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.setChipBackgroundColorResource(R.color.bg_card)
                chip.setTextColor(requireContext().getColor(R.color.white))
            }
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds.first())
                selectedChip.setChipBackgroundColorResource(R.color.yellow_primary)
                selectedChip.setTextColor(requireContext().getColor(R.color.black))

                categoriaFiltro = selectedChip.text.toString().lowercase(Locale.getDefault())
            }
        }

        etStart.setOnClickListener {
            when (criterioConsulta) {
                "por día", "por dia", "por rango" -> {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(requireContext(), { _, y, m, d ->
                        val f = String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y)
                        etStart.setText(f)
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                }
                "por mes" -> {
                    val meses = arrayOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
                    AlertDialog.Builder(requireContext())
                        .setTitle("Seleccionar Número de Mes")
                        .setItems(meses) { _, pos -> etStart.setText(meses[pos]) }.show()
                }
                "por año", "por ano" -> {
                    val anios = arrayOf("2025", "2026", "2027", "2028", "2029", "2030")
                    AlertDialog.Builder(requireContext())
                        .setTitle("Seleccionar Año")
                        .setItems(anios) { _, pos -> etStart.setText(anios[pos]) }.show()
                }
            }
        }

        etEnd.setOnClickListener {
            if (criterioConsulta.contains("rango")) {
                val cal = Calendar.getInstance()
                DatePickerDialog(requireContext(), { _, y, m, d ->
                    val f = String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y)
                    etEnd.setText(f)
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        }

        val rvResults: RecyclerView = vista.findViewById(R.id.rv_query_results)
        rvResults.layoutManager = LinearLayoutManager(requireContext())

        adapterQuery = EventAdapter(listaResultadosDinamicos) { eventoSeleccionado, posicion ->
            abrirDialogoModificacionReal(eventoSeleccionado, posicion, rvResults)
        }
        rvResults.adapter = adapterQuery

        val btnRunQuery: MaterialButton = vista.findViewById(R.id.btn_run_query)
        btnRunQuery.setOnClickListener {
            val inicio = etStart.text.toString().trim()
            val fin = etEnd.text.toString().trim()

            if (inicio.isEmpty() && criterioConsulta == "por rango") {
                Toast.makeText(requireContext(), "Por favor indica las fechas", Toast.LENGTH_SHORT).show()
            } else {
                ejecutarConsultaLocalDefinitiva(rvResults)
            }
        }

        return vista
    }

    private fun ejecutarConsultaLocalDefinitiva(rvResults: RecyclerView) {
        listaResultadosDinamicos.clear()

        val etStart: TextInputEditText? = view?.findViewById(R.id.et_query_start_date)
        val etEnd: TextInputEditText? = view?.findViewById(R.id.et_query_end_date)

        val inicio = etStart?.text.toString().trim()
        val fin = etEnd?.text.toString().trim()

        var fInicio: String? = inicio.ifEmpty { null }
        var fFin: String? = fin.ifEmpty { null }
        var mesStr: String? = null
        var anioStr: String? = null

        var queryCriterioLimpio = criterioConsulta
        if (criterioConsulta.contains("día") || criterioConsulta.contains("dia")) {
            queryCriterioLimpio = "por dia"
        } else if (criterioConsulta.contains("mes")) {
            queryCriterioLimpio = "por mes"
            mesStr = inicio
            fInicio = null
        } else if (criterioConsulta.contains("año") || criterioConsulta.contains("ano")) {
            queryCriterioLimpio = "por año"
            anioStr = inicio
            fInicio = null
        }

        val categoriaFinal = if (categoriaFiltro.equals("Todos", ignoreCase = true)) "todos" else categoriaFiltro

        val cursor: Cursor = dbHelper.consultarEventosAvanzado(queryCriterioLimpio, fInicio, fFin, mesStr, anioStr, categoriaFinal)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID))
                val categoria = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORIA))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FECHA))
                val hora = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HORA))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPCION))
                val estatus = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ESTATUS))
                val contacto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACTO))

                val ubicacion = try {
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UBICACION))
                } catch (e: Exception) { "ESCOM IPN" }

                val recordatorio = try {
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RECORDATORIO))
                } catch (e: Exception) { "Ninguno" }

                listaResultadosDinamicos.add(Evento(id, categoria, descripcion, "$fecha - $hora", contacto, estatus, ubicacion, hora, recordatorio))
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (listaResultadosDinamicos.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontraron eventos.", Toast.LENGTH_SHORT).show()
        }

        adapterQuery = EventAdapter(listaResultadosDinamicos) { eventoSeleccionado, posicion ->
            abrirDialogoModificacionReal(eventoSeleccionado, posicion, rvResults)
        }
        rvResults.adapter = adapterQuery
    }

    private fun abrirDialogoModificacionReal(evento: Evento, posicion: Int, recyclerView: RecyclerView) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_event, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val spinnerStatus: Spinner = dialogView.findViewById(R.id.sp_dialog_status)
        val etContacto: TextInputEditText = dialogView.findViewById(R.id.et_dialog_contact)
        val btnLocation: MaterialButton = dialogView.findViewById(R.id.btn_dialog_location)
        val btnUpdate: MaterialButton = dialogView.findViewById(R.id.btn_dialog_update)
        val btnDelete: MaterialButton = dialogView.findViewById(R.id.btn_dialog_delete)

        // ==========================================
        // CONVERTIMOS EL TEXT INPUT EN UN SELECTOR
        // ==========================================
        etContacto.isFocusable = false
        etContacto.isClickable = true
        etContacto.setText(evento.contacto)

        etContacto.setOnClickListener {

            etContactoActual = etContacto

            try {

                val intentContactos = Intent(
                    Intent.ACTION_PICK,
                    ContactsContract.Contacts.CONTENT_URI
                )

                pickContactLauncher.launch(intentContactos)

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    "No se pudo abrir la agenda",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        // ==========================================

        val listaEstados = listOf("Pendiente", "Realizado", "Aplazado")
        val adapterEstados = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaEstados)
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapterEstados

        val indiceEstatus = listaEstados.indexOf(evento.estatus)
        if (indiceEstatus != -1) {
            spinnerStatus.setSelection(indiceEstatus)
        }

        var nuevaUbicacion = evento.ubicacion
        btnLocation.text = "Ubicación: $nuevaUbicacion"

        btnLocation.setOnClickListener {
            val opcionesLugares = arrayOf("ESCOM IPN", "Laboratorio de Móviles", "Cubículo de Profesores", "Biblioteca", "Casa", "Trabajo")
            AlertDialog.Builder(requireContext())
                .setTitle("Cambiar Ubicación")
                .setItems(opcionesLugares) { _, pos ->
                    nuevaUbicacion = opcionesLugares[pos]
                    btnLocation.text = "Ubicación: $nuevaUbicacion"
                }.show()
        }

        val fechaLimpia = evento.fecha.split(" - ")[0].trim()

        btnUpdate.setOnClickListener {
            val nuevoEstatus = spinnerStatus.selectedItem.toString()
            val nuevoContacto = etContacto.text.toString().trim()

            val filasAfectadas = dbHelper.actualizarEvento(
                id = evento.id,
                categoria = evento.categoria,
                fecha = fechaLimpia,
                hora = evento.horaOriginal,
                descripcion = evento.descripcion,
                estatus = nuevoEstatus,
                ubicacion = nuevaUbicacion,
                contacto = nuevoContacto,
                recordatorio = evento.recordatorioOriginal
            )

            if (filasAfectadas > 0) {
                Toast.makeText(requireContext(), "¡Evento actualizado con éxito!", Toast.LENGTH_SHORT).show()
                evento.estatus = nuevoEstatus
                evento.contacto = nuevoContacto
                evento.ubicacion = nuevaUbicacion
                recyclerView.adapter?.notifyItemChanged(posicion)
            } else {
                Toast.makeText(requireContext(), "Error al actualizar en la base de datos", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            val filasEliminadas = dbHelper.eliminarEvento(evento.id)

            if (filasEliminadas > 0) {
                Toast.makeText(requireContext(), "Evento eliminado permanentemente", Toast.LENGTH_SHORT).show()
                listaResultadosDinamicos.removeAt(posicion)
                recyclerView.adapter?.notifyItemRemoved(posicion)
            } else {
                Toast.makeText(requireContext(), "No se pudo eliminar el registro", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}