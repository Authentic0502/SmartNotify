package com.saleh.smartnotify.ui.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saleh.smartnotify.data.entity.Task
import com.saleh.smartnotify.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TaskAdapter = gère l'affichage de la liste des tâches
class TaskAdapter(
    private val onTaskComplete: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit,
    private val onTaskClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.textTitle.text = task.title
            binding.textDescription.text = task.description

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.textDateTime.text = sdf.format(Date(task.dateTime))

            // Affiche l'icône récurrent
            binding.imageRepeat.visibility =
                if (task.isRepeating) android.view.View.VISIBLE
                else android.view.View.GONE

            // Case à cocher — état selon isCompleted
            binding.checkboxComplete.isChecked = task.isCompleted

            // Applique le style selon l'état de la tâche
            applyTaskStyle(task)

            // Clic sur la case à cocher
            binding.checkboxComplete.setOnClickListener {
                onTaskComplete(task)
            }

            // Clic sur la carte → modifier
            binding.root.setOnClickListener {
                onTaskClick(task)
            }

            // Bouton supprimer
            binding.buttonDelete.setOnClickListener {
                onTaskDelete(task)
            }
        }

        // Applique le style visuel selon si la tâche est complétée ou non
        private fun applyTaskStyle(task: Task) {
            if (task.isCompleted) {
                // Tâche complétée → fond grisé + texte barré
                binding.taskCard.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
                )
                binding.textTitle.apply {
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    // Barre le texte
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                binding.textDescription.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.textDateTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
            } else {
                // Tâche normale → fond blanc + texte normal
                binding.taskCard.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.textTitle.apply {
                    setTextColor(
                        ContextCompat.getColor(context, android.R.color.black)
                    )
                    // Enlève le barrage du texte
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                binding.textDescription.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
                )
                binding.textDateTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.darker_gray)
                )
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem
    }
}