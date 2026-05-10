package com.saleh.smartnotify.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.saleh.smartnotify.databinding.FragmentNotificationBinding
import com.saleh.smartnotify.viewmodel.TaskViewModel

// NotificationFragment = affiche toutes les notifications actives programmées
class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe les tâches non complétées avec notifications actives
        taskViewModel.pendingTasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isEmpty()) {
                binding.textEmptyNotif.visibility = View.VISIBLE
                binding.recyclerViewNotif.visibility = View.GONE
            } else {
                binding.textEmptyNotif.visibility = View.GONE
                binding.recyclerViewNotif.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}