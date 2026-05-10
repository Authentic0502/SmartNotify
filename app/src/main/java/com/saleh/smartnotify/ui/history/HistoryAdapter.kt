package com.saleh.smartnotify.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saleh.smartnotify.data.entity.NotificationHistory
import com.saleh.smartnotify.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// HistoryAdapter = gère l'affichage de l'historique des notifications
class HistoryAdapter : ListAdapter<NotificationHistory,
        HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder = représente une ligne dans la liste
    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(history: NotificationHistory) {
            // Affiche le titre et le message
            binding.textHistoryTitle.text = history.title
            binding.textHistoryMessage.text = history.message

            // Formate et affiche la date d'envoi
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.textHistoryDate.text = sdf.format(Date(history.sentAt))

            // Affiche le canal utilisé
            binding.textHistoryChannel.text = history.channelId
        }
    }

    // Compare les éléments pour optimiser les mises à jour
    class HistoryDiffCallback : DiffUtil.ItemCallback<NotificationHistory>() {
        override fun areItemsTheSame(
            oldItem: NotificationHistory,
            newItem: NotificationHistory
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: NotificationHistory,
            newItem: NotificationHistory
        ) = oldItem == newItem
    }
}