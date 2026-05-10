package com.saleh.smartnotify.ui.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.saleh.smartnotify.databinding.FragmentSettingsBinding

// SettingsFragment = écran de gestion des canaux de notification
class SettingsFragment : Fragment() {

    // ViewBinding pour accéder aux vues
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Gestionnaire de notifications Android
    private lateinit var notificationManager: NotificationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialise le gestionnaire de notifications
        notificationManager = requireContext()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Configure les boutons des canaux
        setupChannelButtons()

        // Met à jour l'état des canaux
        updateChannelStatus()
    }

    // Configure les boutons pour ouvrir les paramètres de chaque canal
    private fun setupChannelButtons() {

        // Canal tâches
        binding.buttonTaskChannel.setOnClickListener {
            openChannelSettings("task_channel")
        }

        // Canal rappels récurrents
        binding.buttonReminderChannel.setOnClickListener {
            openChannelSettings("reminder_channel")
        }

        // Canal contextuel
        binding.buttonContextualChannel.setOnClickListener {
            openChannelSettings("contextual_channel")
        }

        // Canal général
        binding.buttonGeneralChannel.setOnClickListener {
            openChannelSettings("general_channel")
        }
    }

    // Ouvre les paramètres système d'un canal spécifique
    private fun openChannelSettings(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Si le canal n'existe pas encore, ouvre les paramètres généraux
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                startActivity(intent)
            }
        }
    }

    // Met à jour l'affichage du statut de chaque canal
    private fun updateChannelStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Vérifie si chaque canal est activé
            val taskChannel = notificationManager
                .getNotificationChannel("task_channel")
            val reminderChannel = notificationManager
                .getNotificationChannel("reminder_channel")
            val contextualChannel = notificationManager
                .getNotificationChannel("contextual_channel")
            val generalChannel = notificationManager
                .getNotificationChannel("general_channel")

            // Affiche le statut de chaque canal
            binding.textTaskChannelStatus.text = if (
                taskChannel?.importance != NotificationManager.IMPORTANCE_NONE)
                "✅ Activé" else "❌ Désactivé"

            binding.textReminderChannelStatus.text = if (
                reminderChannel?.importance != NotificationManager.IMPORTANCE_NONE)
                "✅ Activé" else "❌ Désactivé"

            binding.textContextualChannelStatus.text = if (
                contextualChannel?.importance != NotificationManager.IMPORTANCE_NONE)
                "✅ Activé" else "❌ Désactivé"

            binding.textGeneralChannelStatus.text = if (
                generalChannel?.importance != NotificationManager.IMPORTANCE_NONE)
                "✅ Activé" else "❌ Désactivé"
        }
    }

    override fun onResume() {
        super.onResume()
        updateChannelStatus()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}