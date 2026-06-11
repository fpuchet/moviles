package com.example.proyectomoviles

data class Evento(
    val id: Int,
    var categoria: String,
    var descripcion: String,
    var fecha: String,
    var contacto: String,
    var estatus: String,
    var ubicacion: String = "ESCOM IPN",
    var horaOriginal: String = "",
    var recordatorioOriginal: String = "Ninguno"
)