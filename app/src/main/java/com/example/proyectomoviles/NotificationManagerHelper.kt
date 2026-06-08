package com.example.proyectomoviles

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class NotificationManagerHelper(private val context: Context) {

    fun programarRecordatorio(
        idEvento: Int,
        titulo: String,
        descripcion: String,
        calendarTarget: Calendar,
        tipoRecordatorio: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("titulo", titulo)
            putExtra("descripcion", descripcion)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            idEvento, // ID único para poder cancelarla o modificarla después si el estatus cambia
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Clonar el calendario para restar el tiempo del recordatorio sin alterar la fecha real
        val tiempoAlarma = (calendarTarget.clone() as Calendar).apply {
            when (tipoRecordatorio) {
                "10 minutos antes" -> add(Calendar.MINUTE, -10)
                "1 día antes" -> add(Calendar.DAY_OF_YEAR, -1)
                "Al momento" -> { /* Se queda exactamente a la hora del evento */ }
                else -> return // "Sin recordatorio" -> Termina la función sin agendar nada
            }
        }

        // Si por error la hora calculada ya pasó en el tiempo real, no agendamos nada
        if (tiempoAlarma.timeInMillis <= System.currentTimeMillis()) return

        // Agendar de forma exacta en el sistema operativo
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            tiempoAlarma.timeInMillis,
            pendingIntent
        )
    }
}