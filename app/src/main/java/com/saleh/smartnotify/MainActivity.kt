package com.saleh.smartnotify

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.saleh.smartnotify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private val menuItems = listOf(
        Pair("Accueil", R.drawable.ic_home),
        Pair("Historique", R.drawable.ic_history),
        Pair("Paramètres", R.drawable.ic_settings),
        Pair("À propos", R.drawable.ic_about)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force couleur status bar - méthode agressive pour Samsung
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = android.graphics.Color.parseColor("#2BA898")
        window.navigationBarColor = android.graphics.Color.parseColor("#2BA898")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                android.view.View.SYSTEM_UI_FLAG_VISIBLE
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Toggle du drawer
        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.app_name, R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // Header du drawer
        val headerView = LayoutInflater.from(this).inflate(R.layout.nav_header, null)
        binding.drawerList.addHeaderView(headerView)

        // Adapter du menu
        val adapter = object : ArrayAdapter<Pair<String, Int>>(this, 0, menuItems) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_drawer_menu, parent, false)
                val item = getItem(position)!!
                view.findViewById<TextView>(R.id.textMenuItem).text = item.first
                view.findViewById<ImageView>(R.id.iconMenuItem).setImageResource(item.second)
                return view
            }
        }

        binding.drawerList.adapter = adapter

        // Clics sur les items
        binding.drawerList.setOnItemClickListener { _, _, position, _ ->
            val realPosition = position - 1
            if (realPosition < 0) return@setOnItemClickListener
            when (realPosition) {
                0 -> navController.navigate(R.id.homeFragment)
                1 -> navController.navigate(R.id.historyFragment)
                2 -> navController.navigate(R.id.settingsFragment)
                3 -> navController.navigate(R.id.aboutFragment)
            }
            binding.drawerLayout.closeDrawers()
        }

        // Permission notification Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}