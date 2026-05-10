package com.saleh.smartnotify.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskId: Int,
    val title: String,
    val message: String,
    val sentAt: Long = System.currentTimeMillis(),
    val channelId: String = "default_channel"
)