package com.example.proyectomoviles

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvHoy: RecyclerView
    private lateinit var rvProximos: RecyclerView
    private val listaHoy = mutableListOf<Evento>()
    private val listaProximos = mutableListOf<Evento>()

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
        val vista = inflater.inflate(R.layout.fragment_home, container, false)
        dbHelper = DatabaseHelper(requireContext())

        rvHoy = vista.findViewById(R.id.rv_events_hoy)
        rvHoy.layoutManager = LinearLayoutManager(requireContext())
        rvHoy.isNestedScrollingEnabled = false

        rvProximos = vista.findViewById(R.id.rv_events_siguientes)
        rvProximos.layoutManager = LinearLayoutManager(requireContext())
        rvProximos.isNestedScrollingEnabled = false

        cargarEventosDesdeSQLite()

        return vista
    }

    private fun cargarEventosDesdeSQLite() {
        listaHoy.clear()
        listaProximos.clear()

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaHoyActual = formatoFecha.format(Calendar.getInstance().time)

        val cursor: Cursor = dbHelper.readableDatabase.rawQuery("SELECT * FROM ${DatabaseHelper.TABLA_EVENTOS}", null)

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

                val evento = Evento(id, categoria, descripcion, "$fecha - $hora", contacto, estatus, ubicacion, hora, recordatorio)

                if (fecha == fechaHoyActual) {
                    listaHoy.add(evento)
                } else {
                    listaProximos.add(evento)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        rvHoy.adapter = EventAdapter(listaHoy) { evento, pos ->
            abrirDialogoModificacionReal(evento, pos, rvHoy, listaHoy)
        }
        rvProximos.adapter = EventAdapter(listaProximos) { evento, pos ->
            abrirDialogoModificacionReal(evento, pos, rvProximos, listaProximos)
        }
    }

    private fun abrirDialogoModificacionReal(evento: Evento, posicion: Int, recyclerView: RecyclerView, listaOrigen: MutableList<Evento>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_event, null)
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
                Toast.makeText(requireContext(), "Error al actualizar en SQLite", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            val filasEliminadas = dbHelper.eliminarEvento(evento.id)

            if (filasEliminadas > 0) {
                Toast.makeText(requireContext(), "Evento eliminado permanentemente", Toast.LENGTH_SHORT).show()
                listaOrigen.removeAt(posicion)
                recyclerView.adapter?.notifyItemRemoved(posicion)
            } else {
                Toast.makeText(requireContext(), "No se pudo eliminar el registro", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}