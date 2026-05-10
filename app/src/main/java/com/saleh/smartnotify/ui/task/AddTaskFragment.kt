package com.saleh.smartnotify.ui.task

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleh.smartnotify.data.entity.Task
import com.saleh.smartnotify.databinding.FragmentAddTaskBinding
import com.saleh.smartnotify.utils.GeofenceHelper
import com.saleh.smartnotify.utils.NotificationScheduler
import com.saleh.smartnotify.viewmodel.TaskViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private val taskViewModel: TaskViewModel by viewModels()
    private val calendar = Calendar.getInstance()
    private var taskId: Int = -1
    private var existingTask: Task? = null

    // Coordonnées du lieu sélectionné
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0
    private var selectedLocationName: String = ""
    private var locationMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Configure osmdroid
        Configuration.getInstance().userAgentValue = requireContext().packageName
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskId = arguments?.getInt("taskId", -1) ?: -1

        if (taskId != -1) loadExistingTask()

        setupMap()
        setupButtons()
        updateDateTimeDisplay()
    }

    // Configure la carte OpenStreetMap
    private fun setupMap() {
        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            // Centré sur N'Djamena par défaut
            controller.setCenter(GeoPoint(12.1048, 15.0445))
        }

        // Appui long sur la carte pour sélectionner un lieu
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean = false

            override fun longPressHelper(p: GeoPoint): Boolean {
                // Place le marqueur
                placeMarker(p)
                return true
            }
        })
        binding.mapView.overlays.add(mapEventsOverlay)
    }

    // Place un marqueur sur la carte
    private fun placeMarker(point: GeoPoint) {
        // Supprime l'ancien marqueur
        locationMarker?.let { binding.mapView.overlays.remove(it) }

        // Crée un nouveau marqueur
        locationMarker = Marker(binding.mapView).apply {
            position = point
            title = "Lieu sélectionné"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        binding.mapView.overlays.add(locationMarker)
        binding.mapView.invalidate()

        // Sauvegarde les coordonnées
        selectedLatitude = point.latitude
        selectedLongitude = point.longitude
        selectedLocationName = "Lat: %.4f, Lon: %.4f".format(point.latitude, point.longitude)

        // Affiche les coordonnées
        binding.textSelectedLocation.text = "📍 $selectedLocationName"

        Toast.makeText(requireContext(), "Lieu sélectionné !", Toast.LENGTH_SHORT).show()
    }

    private fun loadExistingTask() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            existingTask = tasks.find { it.id == taskId }
            existingTask?.let { task ->
                binding.editTitle.setText(task.title)
                binding.editDescription.setText(task.description)
                binding.switchRepeating.isChecked = task.isRepeating
                binding.editRepeatInterval.setText(task.repeatInterval.toString())
                calendar.timeInMillis = task.dateTime
                updateDateTimeDisplay()

                // Charge la localisation si elle existe
                if (task.hasLocation) {
                    binding.switchLocation.isChecked = true
                    binding.layoutMap.visibility = View.VISIBLE
                    selectedLatitude = task.latitude
                    selectedLongitude = task.longitude
                    selectedLocationName = task.locationName
                    binding.textSelectedLocation.text = "📍 $selectedLocationName"
                    val point = GeoPoint(task.latitude, task.longitude)
                    placeMarker(point)
                    binding.mapView.controller.setCenter(point)
                }
            }
        }
    }

    private fun setupButtons() {
        binding.buttonDate.setOnClickListener { showDatePicker() }
        binding.buttonTime.setOnClickListener { showTimePicker() }

        binding.switchRepeating.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutRepeatInterval.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }

        // Switch localisation
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutMap.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) checkLocationPermission()
        }

        binding.buttonSave.setOnClickListener { saveTask() }
        binding.buttonCancel.setOnClickListener { findNavController().navigateUp() }
    }

    // Vérifie la permission de localisation
    // Vérifie et demande toutes les permissions nécessaires
    private fun checkLocationPermission() {
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasBackgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        when {
            // Tout est accordé → vérifie GPS
            hasFineLocation && hasBackgroundLocation -> checkGpsEnabled()

            // Localisation précise manquante → demande standard
            !hasFineLocation -> {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST
                )
            }

            // Localisation arrière-plan manquante → ouvre paramètres
            !hasBackgroundLocation -> {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Permission requise")
                    .setMessage(
                        "Pour les notifications contextuelles, allez dans :\n\n" +
                                "Paramètres → Applications → SmartNotify → " +
                                "Autorisations → Localisation\n\n" +
                                "Puis sélectionnez 'Toujours autoriser'"
                    )
                    .setPositiveButton("Ouvrir les paramètres") { _, _ ->
                        // Ouvre directement les paramètres de l'app
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        ).apply {
                            data = android.net.Uri.fromParts(
                                "package", requireContext().packageName, null
                            )
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Annuler") { _, _ ->
                        binding.switchLocation.isChecked = false
                        binding.layoutMap.visibility = View.GONE
                    }
                    .show()
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateDateTimeDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                updateDateTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeDisplay() {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.textSelectedDateTime.text = sdf.format(calendar.time)
    }

    private fun saveTask() {
        val title = binding.editTitle.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()
        val isRepeating = binding.switchRepeating.isChecked
        val hasLocation = binding.switchLocation.isChecked
        val repeatInterval = if (isRepeating) {
            binding.editRepeatInterval.text.toString().toLongOrNull() ?: 60L
        } else 0L

        if (title.isEmpty()) {
            binding.editTitle.error = "Le titre est obligatoire"
            return
        }

        if (hasLocation && selectedLatitude == 0.0) {
            Toast.makeText(
                requireContext(),
                "Appuie longuement sur la carte pour choisir un lieu !",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val task = Task(
            id = if (taskId != -1) taskId else 0,
            title = title,
            description = description,
            dateTime = calendar.timeInMillis,
            isRepeating = isRepeating,
            repeatInterval = repeatInterval,
            channelId = when {
                hasLocation -> "contextual_channel"
                isRepeating -> "reminder_channel"
                else -> "task_channel"
            },
            latitude = selectedLatitude,
            longitude = selectedLongitude,
            locationName = selectedLocationName,
            hasLocation = hasLocation
        )

        // Sauvegarde le contexte avant navigation
        val appContext = requireContext().applicationContext

        // Programme les notifications AVANT de naviguer
        if (hasLocation) {
            GeofenceHelper.addGeofence(
                appContext,
                task.id,
                task.title,
                selectedLatitude,
                selectedLongitude
            )
        }

        if (isRepeating) {
            NotificationScheduler.scheduleRepeatingNotification(appContext, task)
        } else {
            NotificationScheduler.scheduleNotification(appContext, task)
        }

        // Sauvegarde dans la DB
        taskViewModel.insertTask(task)

        Toast.makeText(appContext, "Tâche sauvegardée !", Toast.LENGTH_SHORT).show()

        // Navigation après tout
        findNavController().navigateUp()
    }
//Vérifier que le GPS est activé
    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1002
        private const val GPS_REQUEST = 1003
    }

    // Vérifie si le GPS est activé
    private fun checkGpsEnabled() {
        val locationManager = requireContext()
            .getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("GPS désactivé")
                .setMessage("Le GPS est nécessaire pour les notifications contextuelles. Voulez-vous l'activer ?")
                .setPositiveButton("Activer") { _, _ ->
                    startActivityForResult(
                        android.content.Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        ),
                        GPS_REQUEST
                    )
                }
                .setNegativeButton("Annuler") { _, _ ->
                    binding.switchLocation.isChecked = false
                    binding.layoutMap.visibility = View.GONE
                }
                .setCancelable(false)
                .show()
        }
    }

    // Appelé quand on revient des paramètres GPS
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPS_REQUEST) {
            val locationManager = requireContext()
                .getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                Toast.makeText(requireContext(), "GPS activé !", Toast.LENGTH_SHORT).show()
            } else {
                binding.switchLocation.isChecked = false
                binding.layoutMap.visibility = View.GONE
                Toast.makeText(requireContext(), "GPS toujours désactivé", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Résultat de la demande de permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée → vérifie background location
                checkLocationPermission()
            } else {
                binding.switchLocation.isChecked = false
                binding.layoutMap.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Activez la localisation dans les paramètres de l'app",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}