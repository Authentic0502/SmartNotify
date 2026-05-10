package com.saleh.smartnotify.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.saleh.smartnotify.data.entity.Task

// @Dao indique à Room que cette interface gère les requêtes
@Dao
interface TaskDao {

    // Insère une nouvelle tâche dans la base de données
    // onConflict = remplace si la tâche existe déjà
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    // Met à jour une tâche existante
    @Update
    suspend fun updateTask(task: Task)

    // Supprime une tâche
    @Delete
    suspend fun deleteTask(task: Task)

    // Récupère toutes les tâches triées par date
    // LiveData = se met à jour automatiquement quand les données changent
    @Query("SELECT * FROM tasks ORDER BY dateTime ASC")
    fun getAllTasks(): LiveData<List<Task>>

    // Récupère uniquement les tâches non complétées
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dateTime ASC")
    fun getPendingTasks(): LiveData<List<Task>>

    // Récupère une tâche par son identifiant unique
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    // Supprime toutes les tâches complétées
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()

    // Récupère toutes les tâches sous forme de liste simple (pas LiveData)
// Utilisé par le BootReceiver qui ne peut pas observer du LiveData
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    suspend fun getAllTasksList(): List<Task>
}