package com.saleh.smartnotify.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val dateTime: Long,
    val isCompleted: Boolean = false,
    val isRepeating: Boolean = false,
    val repeatInterval: Long = 0,
    val channelId: String = "default_channel",
    val priority: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),

    // Champs pour la localisation (geofencing)
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",  // Nom du lieu choisi
    val hasLocation: Boolean = false // true si un lieu est défini
)