package com.example.proyectomoviles

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper

    // Adaptadores y listas separadas para cumplir la regla del profesor
    private lateinit var adapterHoy: EventAdapter
    private lateinit var adapterSiguientes: EventAdapter

    private val listaEventosHoy = mutableListOf<Evento>()
    private val listaEventosSiguientes = mutableListOf<Evento>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_home, container, false)

        // 1. Inicializar el ayudante de la Base de Datos SQLite local
        dbHelper = DatabaseHelper(requireContext())

        // 2. BÚSQUEDA DINÁMICA DE COMPONENTES: Localizar los contenedores por jerarquía de tipo
        val listaRecyclerViews = mutableListOf<RecyclerView>()
        encontrarTodosLosRecyclerViews(vista, listaRecyclerViews)

        val listaTextViews = mutableListOf<TextView>()
        encontrarTodosLosTextViews(vista, listaTextViews)

        // 3. Configurar los títulos de las dos secciones si existen en tu diseño XML
        if (listaTextViews.size > 0) listaTextViews[0].text = "Eventos del Día de Hoy"
        if (listaTextViews.size > 1) listaTextViews[1].text = "Eventos Siguientes (Próximos Días)"

        // 4. Mapear y configurar los dos RecyclerViews independientes de forma segura
        if (listaRecyclerViews.size >= 2) {
            // Configuración del primer bloque (Eventos de Hoy)
            val rvHoy = listaRecyclerViews[0]
            rvHoy.layoutManager = LinearLayoutManager(requireContext())
            adapterHoy = EventAdapter(listaEventosHoy)
            rvHoy.adapter = adapterHoy

            // Configuración del segundo bloque (Eventos Siguientes)
            val rvSiguientes = listaRecyclerViews[1]
            rvSiguientes.layoutManager = LinearLayoutManager(requireContext())
            adapterSiguientes = EventAdapter(listaEventosSiguientes)
            rvSiguientes.adapter = adapterSiguientes
        } else if (listaRecyclerViews.size == 1) {
            // Respaldo de seguridad: si solo tienes un RecyclerView general en tu layout actual,
            // vinculamos el adaptador principal para evitar un crash.
            val rvUnico = listaRecyclerViews[0]
            rvUnico.layoutManager = LinearLayoutManager(requireContext())
            adapterHoy = EventAdapter(listaEventosHoy)
            rvUnico.adapter = adapterHoy
        }

        // 5. Cargar datos reales clasificados directamente de SQLite
        clasificarYCarcarEventosDesdeSQLite()

        return vista
    }

    /**
     * Recupera de forma secuencial los registros de la base de datos y los divide
     * en dos colecciones independientes según la fecha actual del sistema.
     */
    private fun clasificarYCarcarEventosDesdeSQLite() {
        listaEventosHoy.clear()
        listaEventosSiguientes.clear()

        val formateador = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendario = Calendar.getInstance()

        // === SECCIÓN 1: Obtener exclusivamente los eventos de HOY ===
        val fechaHoy = formateador.format(calendario.time)
        val cursorHoy: Cursor = dbHelper.obtenerEventosPorFecha(fechaHoy)

        if (cursorHoy.moveToFirst()) {
            do {
                val categoria = cursorHoy.getString(cursorHoy.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORIA))
                val fecha = cursorHoy.getString(cursorHoy.getColumnIndexOrThrow(DatabaseHelper.COL_FECHA))
                val descripcion = cursorHoy.getString(cursorHoy.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPCION))
                val estatus = cursorHoy.getString(cursorHoy.getColumnIndexOrThrow(DatabaseHelper.COL_ESTATUS))
                val contacto = cursorHoy.getString(cursorHoy.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACTO))

                listaEventosHoy.add(Evento(categoria, descripcion, fecha, contacto, estatus))
            } while (cursorHoy.moveToNext())
        }
        cursorHoy.close()

        // === SECCIÓN 2: Obtener eventos de los Próximos 4 días ===
        for (i in 1..4) {
            calendario.add(Calendar.DAY_OF_YEAR, 1) // Avanzar al siguiente día secuencial
            val fechaSiguiente = formateador.format(calendario.time)

            val cursorSiguiente: Cursor = dbHelper.obtenerEventosPorFecha(fechaSiguiente)
            if (cursorSiguiente.moveToFirst()) {
                do {
                    val categoria = cursorSiguiente.getString(cursorSiguiente.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORIA))
                    val fecha = cursorSiguiente.getString(cursorSiguiente.getColumnIndexOrThrow(DatabaseHelper.COL_FECHA))
                    val descripcion = cursorSiguiente.getString(cursorSiguiente.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPCION))
                    val estatus = cursorSiguiente.getString(cursorSiguiente.getColumnIndexOrThrow(DatabaseHelper.COL_ESTATUS))
                    val contacto = cursorSiguiente.getString(cursorSiguiente.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACTO))

                    listaEventosSiguientes.add(Evento(categoria, descripcion, fecha, contacto, estatus))
                } while (cursorSiguiente.moveToNext())
            }
            cursorSiguiente.close()
        }

        // Notificar los cambios a los adaptadores mapeados de forma segura
        if (::adapterHoy.isInitialized) {
            adapterHoy.notifyDataSetChanged()
        }
        if (::adapterSiguientes.isInitialized) {
            adapterSiguientes.notifyDataSetChanged()
        }
    }

    /**
     * Función recursiva para recolectar todos los RecyclerView de la interfaz
     */
    private fun encontrarTodosLosRecyclerViews(root: View, lista: MutableList<RecyclerView>) {
        if (root is RecyclerView) {
            lista.add(root)
        } else if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                encontrarTodosLosRecyclerViews(root.getChildAt(i), lista)
            }
        }
    }

    /**
     * Función recursiva para recolectar todos los TextView de la interfaz que sirvan de etiquetas
     */
    private fun encontrarTodosLosTextViews(root: View, lista: MutableList<TextView>) {
        if (root is TextView && root !is RecyclerView) {
            lista.add(root)
        } else if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                encontrarTodosLosTextViews(root.getChildAt(i), lista)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Mantener la sincronización al día si el usuario agrega nuevos eventos
        clasificarYCarcarEventosDesdeSQLite()
    }
}