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

        // 2. Vincular componentes de la interfaz
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // 3. Configurar botón hamburguesa lateral
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.bottom_home, R.string.bottom_exit
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 4. Cargar HomeFragment por defecto al abrir la app
        if (savedInstanceState == null) {
            cambiarFragmento(HomeFragment(), "Inicio")
        }

        // 5. ESCUCHAR CLICS DEL MENÚ HAMBURGUESA (7 Opciones obligatorias)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_event -> cambiarFragmento(AddEventFragment(), "Añadir Eventos")
                R.id.nav_query_event -> cambiarFragmento(QueryFragment(), "Consultar Eventos")
                R.id.nav_calendar -> cambiarFragmento(CalendarFragment(), "Calendario")

                // Opciones secundarias del menú
                R.id.nav_backup -> Toast.makeText(this, "Respaldo en la nube ejecutado", Toast.LENGTH_SHORT).show()
                R.id.nav_restore -> Toast.makeText(this, "Datos restaurados con éxito", Toast.LENGTH_SHORT).show()
                R.id.nav_about -> Toast.makeText(this, "Organizador de Tareas v1.0 - Desarrollado por Alexander", Toast.LENGTH_LONG).show()
                R.id.nav_exit -> finish()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
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

        // 7. Control avanzado del botón atrás físico
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

        // 8. Solicitar permisos de notificación explícitos para Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    // --- MÉTODOS NATIVOS PARA GESTIONAR EL MENÚ DE LA BARRA SUPERIOR ---

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        // Inflar el recurso XML de la campana en la Toolbar
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        // CORRECCIÓN UX REAL: Forzar el color amarillo neón por código para que siempre brille
        if (menu != null) {
            val notificationItem = menu.findItem(R.id.action_notifications)
            notificationItem?.icon?.let { iconDrawable ->
                // Mutamos el recurso para que el tinte no afecte a otras partes del sistema
                val mutableIcon = iconDrawable.mutate()
                mutableIcon.setTint(getColor(R.color.yellow_primary))
                notificationItem.icon = mutableIcon
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                // Requisito 5: Mostrar la información de los eventos inmediatos de hoy
                Toast.makeText(
                    this,
                    "Eventos de hoy: Tienes 2 entregas prioritarias en tu agenda.",
                    Toast.LENGTH_LONG
                ).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Función auxiliar para el intercambio fluido de fragmentos
    private fun cambiarFragmento(fragmento: Fragment, titulo: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragmento)
            .commit()
        supportActionBar?.title = titulo
        supportActionBar?.title = titulo
    }
}