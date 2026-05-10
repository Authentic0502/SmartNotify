package com.saleh.smartnotify.ui.home

import com.saleh.smartnotify.utils.GeofenceHelper
import com.saleh.smartnotify.utils.NotificationScheduler
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saleh.smartnotify.R
import com.saleh.smartnotify.databinding.FragmentHomeBinding
import com.saleh.smartnotify.ui.task.TaskAdapter
import com.saleh.smartnotify.viewmodel.TaskViewModel

// HomeFragment = écran principal qui affiche la liste des tâches
class HomeFragment : Fragment() {

    // ViewBinding pour accéder aux vues
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel pour accéder aux données
    private val taskViewModel: TaskViewModel by viewModels()

    // Adaptateur pour le RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure le RecyclerView
        setupRecyclerView()

        // Observe les tâches et met à jour l'interface
        observeTasks()

        // Bouton flottant pour ajouter une tâche
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addTask)
        }
    }

    // Configure le RecyclerView avec son adaptateur
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            // Action au clic sur une tâche → ouvre l'écran de modification
            onTaskClick = { task ->
                val bundle = Bundle().apply {
                    putInt("taskId", task.id)
                }
                findNavController().navigate(R.id.action_home_to_addTask, bundle)
            },
            onTaskComplete = { task ->
                if (task.isCompleted) {
                    // Tâche décochée → réactive la notification
                    val updatedTask = task.copy(isCompleted = false)
                    taskViewModel.updateTask(updatedTask)
                    // Reprogramme la notification si la date est dans le futur
                    if (task.dateTime > System.currentTimeMillis()) {
                        if (task.isRepeating) {
                            NotificationScheduler.scheduleRepeatingNotification(
                                requireContext(), updatedTask)
                        } else {
                            NotificationScheduler.scheduleNotification(
                                requireContext(), updatedTask)
                        }
                    }
                    // Réactive le geofence si localisation
                    if (task.hasLocation) {
                        GeofenceHelper.addGeofence(
                            requireContext(), task.id, task.title,
                            task.latitude, task.longitude
                        )
                    }
                } else {
                    // Tâche cochée → annule la notification
                    NotificationScheduler.cancelNotification(requireContext(), task.id)
                    // Supprime le geofence
                    if (task.hasLocation) {
                        GeofenceHelper.removeGeofence(requireContext(), task.id)
                    }
                    taskViewModel.updateTask(task.copy(isCompleted = true))
                }
            },

            onTaskDelete = { task ->
                // Annule la notification programmée avant de supprimer
                NotificationScheduler.cancelNotification(requireContext(), task.id)
                // Supprime le geofence si existe
                if (task.hasLocation) {
                    GeofenceHelper.removeGeofence(requireContext(), task.id)
                }
                taskViewModel.deleteTask(task)
            }



        )

        // Attache l'adaptateur au RecyclerView
        binding.recyclerViewTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // Observe les changements dans les tâches
    private fun observeTasks() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)

            // Affiche un message si aucune tâche
            if (tasks.isEmpty()) {
                binding.textEmpty.visibility = View.VISIBLE
                binding.recyclerViewTasks.visibility = View.GONE
            } else {
                binding.textEmpty.visibility = View.GONE
                binding.recyclerViewTasks.visibility = View.VISIBLE
            }
        }
    }



    // Libère le binding quand le fragment est détruit
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}