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
        android.util.Log.d(
            "NOTIF_DEBUG",
            "Entró a programarRecordatorio con: $tipoRecordatorio"
        )

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

                "Al momento",
                "Al momento de la tarea" -> {
                    // No modificar la hora
                }

                "10 minutos antes" -> {
                    add(Calendar.MINUTE, -10)
                }

                "1 día antes" -> {
                    add(Calendar.DAY_OF_YEAR, -1)
                }

                "Sin recordatorio" -> {
                    return
                }

                else -> {
                    return
                }
            }
        }

        // Si por error la hora calculada ya pasó en el tiempo real, no agendamos nada
        if (tiempoAlarma.timeInMillis <= System.currentTimeMillis()) return
        android.util.Log.d(
            "NOTIF_DEBUG",
            "Alarma programada para: ${tiempoAlarma.time}"
        )
        // Agendar de forma exacta en el sistema operativo
        if (alarmManager.canScheduleExactAlarms()) {

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                tiempoAlarma.timeInMillis,
                pendingIntent
            )

        } else {

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                tiempoAlarma.timeInMillis,
                pendingIntent
            )
        }
    }
}