package com.saleh.smartnotify.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.saleh.smartnotify.data.entity.NotificationHistory

// @Dao pour gérer l'historique des notifications envoyées
@Dao
interface NotificationHistoryDao {

    // Enregistre une notification dans l'historique
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: NotificationHistory)

    // Récupère tout l'historique, du plus récent au plus ancien
    @Query("SELECT * FROM notification_history ORDER BY sentAt DESC")
    fun getAllHistory(): LiveData<List<NotificationHistory>>

    // Supprime tout l'historique
    @Query("DELETE FROM notification_history")
    suspend fun clearHistory()

    // Supprime l'historique lié à une tâche spécifique
    @Query("DELETE FROM notification_history WHERE taskId = :taskId")
    suspend fun deleteHistoryByTask(taskId: Int)
}