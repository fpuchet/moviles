package com.example.proyectomoviles

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Configurar Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 2. Vincular componentes
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // 3. Configurar botón hamburguesa
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.bottom_home, R.string.bottom_exit
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 4. Cargar HomeFragment por defecto al abrir
        if (savedInstanceState == null) {
            cambiarFragmento(HomeFragment(), "Inicio")
        }

        // 5. ESCUCHAR CLICS DEL MENÚ HAMBURGUESA (7 Opciones)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_event -> cambiarFragmento(AddEventFragment(), "Añadir Eventos")
                R.id.nav_query_event -> cambiarFragmento(QueryFragment(), "Consultar Eventos")
                R.id.nav_calendar -> cambiarFragmento(CalendarFragment(), "Calendario")

                // Opciones secundarias temporales con un Toast informativo
                R.id.nav_backup -> Toast.makeText(this, "Respaldo en la nube ejecutado", Toast.LENGTH_SHORT).show()
                R.id.nav_restore -> Toast.makeText(this, "Datos restaurados con éxito", Toast.LENGTH_SHORT).show()
                R.id.nav_about -> Toast.makeText(this, "Organizador de Tareas v1.0 - Desarrollado por Alexander", Toast.LENGTH_LONG).show()
                R.id.nav_exit -> finish() // Cierra la aplicación de forma nativa
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Cierra el menú lateral tras dar clic
            true
        }

        // 6. ESCUCHAR CLICS DE LA BARRA INFERIOR (Inicio, Consultar, Salir)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> cambiarFragmento(HomeFragment(), "Inicio")
                R.id.bottom_query -> cambiarFragmento(QueryFragment(), "Consultar Eventos")
                R.id.bottom_exit -> finish()
            }
            true
        }

        // 7. Control del botón atrás físico
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // Función auxiliar para reutilizar el código de intercambio de fragmentos
    private fun cambiarFragmento(fragmento: Fragment, titulo: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragmento)
            .commit()
        supportActionBar?.title = titulo
    }
}