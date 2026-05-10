package com.saleh.smartnotify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.saleh.smartnotify.SmartNotifyApp
import com.saleh.smartnotify.data.entity.Task
import com.saleh.smartnotify.data.repository.TaskRepository
import kotlinx.coroutines.launch

// AndroidViewModel reçoit le contexte de l'application
// Utile pour accéder au Repository via SmartNotifyApp
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    // Récupère le repository depuis l'Application Class
    private val repository: TaskRepository =
        (application as SmartNotifyApp).taskRepository

    // Toutes les tâches — se mettent à jour automatiquement
    val allTasks: LiveData<List<Task>> = repository.allTasks

    // Tâches non complétées uniquement
    val pendingTasks: LiveData<List<Task>> = repository.pendingTasks

    // Insère une tâche en arrière-plan (coroutine)
    // viewModelScope = la coroutine s'arrête si le ViewModel est détruit
    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    // Met à jour une tâche existante
    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }

    // Supprime une tâche
    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    // Récupère une tâche par son ID
    suspend fun getTaskById(taskId: Int): Task? {
        return repository.getTaskById(taskId)
    }

    // Supprime toutes les tâches complétées
    fun deleteCompletedTasks() = viewModelScope.launch {
        repository.deleteCompletedTasks()
    }
}