package com.saleh.smartnotify.data.repository

import androidx.lifecycle.LiveData
import com.saleh.smartnotify.data.dao.TaskDao
import com.saleh.smartnotify.data.entity.Task

// Le Repository centralise toutes les opérations sur les tâches
// Il reçoit le DAO en paramètre pour accéder à la base de données
class TaskRepository(private val taskDao: TaskDao) {

    // Récupère toutes les tâches en temps réel (LiveData)
    // Le "val" permet d'y accéder directement depuis le ViewModel
    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()

    // Récupère uniquement les tâches non complétées
    val pendingTasks: LiveData<List<Task>> = taskDao.getPendingTasks()

    // Insère une tâche — suspend = s'exécute en arrière-plan
    // pour ne pas bloquer l'interface utilisateur
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    // Met à jour une tâche existante
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    // Supprime une tâche
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // Récupère une tâche par son ID
    suspend fun getTaskById(taskId: Int): Task? {
        return taskDao.getTaskById(taskId)
    }

    // Supprime toutes les tâches complétées
    suspend fun deleteCompletedTasks() {
        taskDao.deleteCompletedTasks()
    }
}