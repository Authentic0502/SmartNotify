package com.saleh.smartnotify.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.saleh.smartnotify.data.entity.Task
import com.saleh.smartnotify.receiver.AlarmReceiver

// Utilitaire pour programmer et annuler les notifications
// Utilise AlarmManager pour fonctionner même écran éteint
object NotificationScheduler {

    // Programme une notification pour une tâche
    fun scheduleNotification(context: Context, task: Task) {
        val delay = task.dateTime - System.currentTimeMillis()
        if (delay <= 0) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = createAlarmIntent(context, task)
        val pendingIntent = PendingIntent.getBroadcast(
            context, task.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Utilise setExactAndAllowWhileIdle pour fonctionner en Doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                task.dateTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                task.dateTime,
                pendingIntent
            )
        }
    }

    // Programme un rappel récurrent
    fun scheduleRepeatingNotification(context: Context, task: Task) {
        val delay = task.dateTime - System.currentTimeMillis()
        if (delay <= 0) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = createAlarmIntent(context, task)

        // ID unique pour les récurrentes (taskId + 10000)
        val pendingIntent = PendingIntent.getBroadcast(
            context, task.id + 10000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            task.dateTime,
            task.repeatInterval * 60 * 1000,
            pendingIntent
        )
    }

    // Annule une notification programmée
    // Annule UNE notification programmée (simple et récurrente)
    fun cancelNotification(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Crée exactement le même intent pour l'annuler
        val intent = Intent(context, AlarmReceiver::class.java)

        // Annule l'alarme simple
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        // Annule aussi l'alarme récurrente (même ID)
        val pendingIntentRepeat = PendingIntent.getBroadcast(
            context, taskId + 10000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntentRepeat)
        pendingIntentRepeat.cancel()

        // Annule aussi via WorkManager au cas où
        androidx.work.WorkManager.getInstance(context)
            .cancelAllWorkByTag("task_$taskId")
    }

    // Annule toutes les notifications
    fun cancelAllNotifications(context: Context) {
        // On ne peut pas annuler toutes les alarmes directement
        // mais on annule le WorkManager
        androidx.work.WorkManager.getInstance(context).cancelAllWork()
    }

    // Crée l'intent pour AlarmReceiver
    private fun createAlarmIntent(context: Context, task: Task): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_message",
                if (task.description.isNotEmpty()) task.description
                else "Rappel : ${task.title}")
            putExtra("channel_id", task.channelId)
        }
    }
}