package com.saleh.smartnotify.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.saleh.smartnotify.MainActivity
import com.saleh.smartnotify.R
import com.saleh.smartnotify.data.AppDatabase
import com.saleh.smartnotify.data.entity.NotificationHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// AlarmReceiver = reçoit les alarmes et affiche les notifications
// Fonctionne même quand l'écran est éteint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("task_id", -1)
        val title = intent.getStringExtra("task_title") ?: "Rappel"
        val message = intent.getStringExtra("task_message") ?: "Tu as une tâche"
        val channelId = intent.getStringExtra("channel_id") ?: "task_channel"

        if (taskId == -1) return

        // Affiche la notification
        showNotification(context, taskId, title, message, channelId)

        // Sauvegarde dans l'historique
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            db.notificationHistoryDao().insertHistory(
                NotificationHistory(
                    taskId = taskId,
                    title = title,
                    message = message,
                    channelId = channelId
                )
            )
        }
    }

    private fun showNotification(
        context: Context,
        taskId: Int,
        title: String,
        message: String,
        channelId: String
    ) {
        // Intent pour ouvrir l'app au clic
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construction de la notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            // Permet la notification même en mode Ne pas déranger
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            // Son et vibration
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId, notification)
    }
}