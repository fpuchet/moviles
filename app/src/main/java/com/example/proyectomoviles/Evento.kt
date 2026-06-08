package com.example.proyectomoviles

data class Evento(
    val categoria: String,
    val descripcion: String,
    val fecha: String,
    val contacto: String,
    val estatus: String,
    val ubicacion: String = "No especificada"
)