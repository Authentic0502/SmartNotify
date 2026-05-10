package com.saleh.smartnotify.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.saleh.smartnotify.databinding.FragmentAboutBinding

// AboutFragment = écran "À propos" de l'application
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nom cliquable → ouvre les liens professionnels
        binding.textDeveloperName.setOnClickListener {
            showLinksDialog()
        }
    }

    // Affiche un dialogue avec les liens professionnels
    private fun showLinksDialog() {
        val links = arrayOf(
            "🐙 GitHub",
            "💼 LinkedIn",
            "📧 Email"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Saleh Ali — Liens")
            .setItems(links) { _, which ->
                val url = when (which) {
                    0 -> "https://github.com/SalehAli"
                    1 -> "https://www.linkedin.com/in/saleh-ali-0b99093aa"
                    2 -> "mailto:aliabissot@gmail.com"
                    else -> ""
                }
                if (url.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }
            .setNegativeButton("Fermer", null)
            .show()
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}