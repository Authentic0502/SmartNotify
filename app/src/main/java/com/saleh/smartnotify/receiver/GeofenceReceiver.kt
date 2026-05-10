package com.saleh.smartnotify.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.saleh.smartnotify.MainActivity
import com.saleh.smartnotify.R
import com.saleh.smartnotify.data.AppDatabase
import com.saleh.smartnotify.data.entity.NotificationHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// GeofenceReceiver = reçoit les événements de geofencing
// Se déclenche quand l'utilisateur entre dans une zone définie
class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("task_id", -1)
        val taskTitle = intent.getStringExtra("task_title") ?: "Rappel"

        // Vérifie l'événement geofencing
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        // Vérifie que c'est bien une entrée dans la zone
        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Affiche la notification contextuelle
            showContextualNotification(context, taskId, taskTitle)

            // Sauvegarde dans l'historique
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                db.notificationHistoryDao().insertHistory(
                    NotificationHistory(
                        taskId = taskId,
                        title = taskTitle,
                        message = "Tu es arrivé à l'endroit de la tâche : $taskTitle",
                        channelId = "contextual_channel"
                    )
                )
            }
        }
    }

    // Affiche la notification contextuelle
    private fun showContextualNotification(
        context: Context,
        taskId: Int,
        taskTitle: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "contextual_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📍 Rappel contextuel")
            .setContentText("Tu es arrivé à l'endroit de : $taskTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId + 10000, notification)
    }
}