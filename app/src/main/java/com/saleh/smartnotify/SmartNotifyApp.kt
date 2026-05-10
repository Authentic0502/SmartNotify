package com.saleh.smartnotify

import com.saleh.smartnotify.utils.NotificationScheduler
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.saleh.smartnotify.data.AppDatabase
import com.saleh.smartnotify.data.repository.NotificationRepository
import com.saleh.smartnotify.data.repository.TaskRepository

// Application Class — s'exécute en premier au démarrage de l'app
// Hérite de Application pour avoir accès au contexte global
class SmartNotifyApp : Application() {

    // Initialisation paresseuse de la base de données
    // Elle n'est créée que quand on y accède pour la première fois
    val database by lazy { AppDatabase.getDatabase(this) }

    // Initialisation paresseuse des repositories
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val notificationRepository by lazy {
        NotificationRepository(database.notificationHistoryDao())
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // Annule toutes les anciennes notifications au démarrage
        NotificationScheduler.cancelAllNotifications(this)
    }

    // Crée les canaux de notification
    // Obligatoire depuis Android 8.0 (API 26)
    private fun createNotificationChannels() {

        // Les canaux ne sont disponibles qu'à partir d'Android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Canal 1 — Tâches importantes (priorité haute)
            val taskChannel = NotificationChannel(
                "task_channel",           // ID unique du canal
                "Rappels de tâches",      // Nom visible dans les paramètres
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les rappels de tâches"
                enableVibration(true)
            }

            // Canal 2 — Rappels récurrents (priorité normale)
            val reminderChannel = NotificationChannel(
                "reminder_channel",
                "Rappels récurrents",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les rappels récurrents"
            }

            // Canal 3 — Notifications contextuelles (priorité haute)
            val contextualChannel = NotificationChannel(
                "contextual_channel",
                "Notifications contextuelles",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications basées sur la localisation"
                enableVibration(true)
            }

            // Canal 4 — Notifications générales (priorité basse)
            val generalChannel = NotificationChannel(
                "general_channel",
                "Notifications générales",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications générales de l'application"
            }

            // Enregistre tous les canaux dans le système Android
            notificationManager.createNotificationChannels(
                listOf(taskChannel, reminderChannel, contextualChannel, generalChannel)
            )
        }
    }
}