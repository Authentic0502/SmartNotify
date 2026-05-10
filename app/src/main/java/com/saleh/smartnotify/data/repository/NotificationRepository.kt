package com.saleh.smartnotify.data.repository

import androidx.lifecycle.LiveData
import com.saleh.smartnotify.data.dao.NotificationHistoryDao
import com.saleh.smartnotify.data.entity.NotificationHistory

// Le Repository de l'historique des notifications
// Gère toutes les opérations liées aux notifications envoyées
class NotificationRepository(private val historyDao: NotificationHistoryDao) {

    // Récupère tout l'historique en temps réel
    val allHistory: LiveData<List<NotificationHistory>> = historyDao.getAllHistory()

    // Enregistre une notification dans l'historique
    suspend fun insertHistory(history: NotificationHistory) {
        historyDao.insertHistory(history)
    }

    // Supprime tout l'historique
    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    // Supprime l'historique d'une tâche spécifique
    suspend fun deleteHistoryByTask(taskId: Int) {
        historyDao.deleteHistoryByTask(taskId)
    }
}