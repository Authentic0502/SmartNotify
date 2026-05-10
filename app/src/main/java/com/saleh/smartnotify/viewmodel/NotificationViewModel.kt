package com.saleh.smartnotify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.saleh.smartnotify.SmartNotifyApp
import com.saleh.smartnotify.data.entity.NotificationHistory
import com.saleh.smartnotify.data.repository.NotificationRepository
import kotlinx.coroutines.launch

// ViewModel pour gérer l'historique des notifications
class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    // Récupère le repository depuis l'Application Class
    private val repository: NotificationRepository =
        (application as SmartNotifyApp).notificationRepository

    // Tout l'historique des notifications — se met à jour automatiquement
    val allHistory: LiveData<List<NotificationHistory>> = repository.allHistory

    // Enregistre une notification dans l'historique
    fun insertHistory(history: NotificationHistory) = viewModelScope.launch {
        repository.insertHistory(history)
    }

    // Supprime tout l'historique
    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
    }

    // Supprime l'historique d'une tâche spécifique
    fun deleteHistoryByTask(taskId: Int) = viewModelScope.launch {
        repository.deleteHistoryByTask(taskId)
    }
}