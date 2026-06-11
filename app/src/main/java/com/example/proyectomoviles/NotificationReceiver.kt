package com.example.proyectomoviles

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "canal_organizador_eventos"
        const val CHANNEL_NAME = "Recordatorios del Organizador"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        android.widget.Toast.makeText(
            context,
            "Receiver ejecutado",
            android.widget.Toast.LENGTH_LONG
        ).show()

        android.util.Log.d(
            "NOTIF_DEBUG",
            "Receiver ejecutado"
        )

        // Extraer los datos del evento
        val titulo = intent?.getStringExtra("titulo") ?: "Recordatorio de Evento"
        val descripcion = intent?.getStringExtra("descripcion") ?: "Tienes una tarea pendiente ahora."

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // CORREGIDO: Se cambió la coma por el punto en VERSION_CODES.O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true)
            channel.description = "Canal para las alertas del organizador de tareas"
            notificationManager.createNotificationChannel(channel)
        }

        // Construir la notificación compatible
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usamos un icono nativo del sistema para asegurar
            .setContentTitle(titulo)
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        // Lanzar la notificación con ID único
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}