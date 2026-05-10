package com.saleh.smartnotify.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.saleh.smartnotify.receiver.GeofenceReceiver

object GeofenceHelper {

    // Rayon de 200 mètres
    private const val GEOFENCE_RADIUS = 200f

    // Durée 7 jours
    private const val GEOFENCE_EXPIRATION = 7 * 24 * 60 * 60 * 1000L

    fun addGeofence(
        context: Context,
        taskId: Int,
        taskTitle: String,
        latitude: Double,
        longitude: Double
    ) {
        // Vérifie la permission avant d'ajouter
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val geofencingClient: GeofencingClient =
            LocationServices.getGeofencingClient(context)

        // Crée le geofence
        val geofence = Geofence.Builder()
            .setRequestId("geofence_$taskId")
            .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS)
            .setExpirationDuration(GEOFENCE_EXPIRATION)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            // Délai de 30 secondes pour éviter les faux déclenchements
            .setLoiteringDelay(30000)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val pendingIntent = getPendingIntent(context, taskId, taskTitle)

        // Ajoute le geofence avec callback
        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                android.util.Log.d("GeofenceHelper",
                    "Geofence ajouté avec succès pour tâche $taskId")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("GeofenceHelper",
                    "Erreur ajout geofence: ${e.message}")
            }
    }

    fun removeGeofence(context: Context, taskId: Int) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        geofencingClient.removeGeofences(listOf("geofence_$taskId"))
            .addOnSuccessListener {
                android.util.Log.d("GeofenceHelper",
                    "Geofence supprimé pour tâche $taskId")
            }
    }

    private fun getPendingIntent(
        context: Context,
        taskId: Int,
        taskTitle: String
    ): PendingIntent {
        val intent = Intent(context, GeofenceReceiver::class.java).apply {
            putExtra("task_id", taskId)
            putExtra("task_title", taskTitle)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}