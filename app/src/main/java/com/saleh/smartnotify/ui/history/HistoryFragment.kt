package com.saleh.smartnotify.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.saleh.smartnotify.databinding.FragmentHistoryBinding
import com.saleh.smartnotify.viewmodel.NotificationViewModel

// HistoryFragment = affiche l'historique des notifications envoyées
class HistoryFragment : Fragment() {

    // ViewBinding pour accéder aux vues
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // ViewModel pour accéder à l'historique
    private val notificationViewModel: NotificationViewModel by viewModels()

    // Adaptateur pour le RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure le RecyclerView
        setupRecyclerView()

        // Observe l'historique
        observeHistory()

        // Bouton effacer tout l'historique
        binding.buttonClearHistory.setOnClickListener {
            notificationViewModel.clearHistory()
        }
    }

    // Configure le RecyclerView
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.recyclerViewHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // Observe les changements dans l'historique
    private fun observeHistory() {
        notificationViewModel.allHistory.observe(viewLifecycleOwner) { history ->
            historyAdapter.submitList(history)

            // Affiche un message si historique vide
            if (history.isEmpty()) {
                binding.textEmptyHistory.visibility = View.VISIBLE
                binding.recyclerViewHistory.visibility = View.GONE
                binding.buttonClearHistory.visibility = View.GONE
            } else {
                binding.textEmptyHistory.visibility = View.GONE
                binding.recyclerViewHistory.visibility = View.VISIBLE
                binding.buttonClearHistory.visibility = View.VISIBLE
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}