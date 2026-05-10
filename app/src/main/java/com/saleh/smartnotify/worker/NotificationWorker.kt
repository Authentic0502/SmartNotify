package com.saleh.smartnotify.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.saleh.smartnotify.MainActivity
import com.saleh.smartnotify.R
import com.saleh.smartnotify.data.AppDatabase
import com.saleh.smartnotify.data.entity.NotificationHistory

// CoroutineWorker = Worker qui supporte les coroutines (opérations asynchrones)
// S'exécute en arrière-plan même si l'app est fermée
class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Récupère les données passées au Worker
        val taskId = inputData.getInt("task_id", -1)
        val title = inputData.getString("task_title") ?: "Rappel"
        val message = inputData.getString("task_message") ?: "Tu as une tâche à faire"
        val channelId = inputData.getString("channel_id") ?: "task_channel"

        // Si l'ID de la tâche est invalide, on arrête
        if (taskId == -1) return Result.failure()

        // Affiche la notification
        showNotification(taskId, title, message, channelId)

        // Enregistre la notification dans l'historique
        saveToHistory(taskId, title, message, channelId)

        // Retourne succès
        return Result.success()
    }

    // Crée et affiche la notification sur le téléphone
    private fun showNotification(
        taskId: Int,
        title: String,
        message: String,
        channelId: String
    ) {
        // Intent pour ouvrir MainActivity quand on clique sur la notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }

        // PendingIntent = intention différée (exécutée au clic)
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construction de la notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Icône de notification
            .setContentTitle(title)                    // Titre
            .setContentText(message)                   // Message
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)                       // Disparaît au clic
            .setContentIntent(pendingIntent)           // Action au clic
            .build()

        // Affiche la notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId, notification)
    }

    // Enregistre la notification envoyée dans la base de données
    private suspend fun saveToHistory(
        taskId: Int,
        title: String,
        message: String,
        channelId: String
    ) {
        val db = AppDatabase.getDatabase(context)
        val history = NotificationHistory(
            taskId = taskId,
            title = title,
            message = message,
            channelId = channelId
        )
        db.notificationHistoryDao().insertHistory(history)
    }
}