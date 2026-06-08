package com.example.proyectomoviles

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class CalendarFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val textView = TextView(requireContext()).apply {
            text = "Pantalla: Mostrar Calendario"
            textSize = 24f // CORREGIDO: Se usa 24f en lugar de 24sp
            gravity = Gravity.CENTER
        }
        return textView
    }
}