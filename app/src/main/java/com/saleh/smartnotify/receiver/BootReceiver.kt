package com.saleh.smartnotify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.saleh.smartnotify.data.AppDatabase
import com.saleh.smartnotify.utils.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// BroadcastReceiver = écoute les événements système
// Ici il écoute le redémarrage du téléphone
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // Vérifie que c'est bien un événement de démarrage
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            // Lance une coroutine pour accéder à la base de données
            // Dispatchers.IO = s'exécute sur un thread d'entrée/sortie
            CoroutineScope(Dispatchers.IO).launch {

                // Récupère toutes les tâches non complétées
                val db = AppDatabase.getDatabase(context)
                val tasks = db.taskDao().getAllTasksList()

                // Reprogramme les notifications pour chaque tâche
                tasks.forEach { task ->
                    if (!task.isCompleted) {
                        if (task.isRepeating) {
                            // Reprogramme les rappels récurrents
                            NotificationScheduler.scheduleRepeatingNotification(
                                context, task
                            )
                        } else {
                            // Reprogramme les notifications simples
                            NotificationScheduler.scheduleNotification(
                                context, task
                            )
                        }
                    }
                }
            }
        }
    }
}