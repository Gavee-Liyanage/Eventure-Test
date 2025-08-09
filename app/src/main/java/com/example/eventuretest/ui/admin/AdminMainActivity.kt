package com.example.eventuretest.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.eventuretest.MainActivity
import com.example.eventuretest.R
import com.example.eventuretest.databinding.ActivityAdminMainBinding
import com.example.eventuretest.viewmodels.AdminMainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private val viewModel: AdminMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()

        viewModel.loadDashboardData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Admin Dashboard"
    }

    private fun setupClickListeners() {
        binding.cardAddEvent.setOnClickListener {
            Log.d("AdminMain", "Add Event clicked")
            startActivity(Intent(this, AddEventActivity::class.java))
        }

        binding.cardViewEvents.setOnClickListener {
            Log.d("AdminMain", "View Events clicked")
            startActivity(Intent(this, EventListActivity::class.java))
        }



        binding.btnRefresh.setOnClickListener {
            Log.d("AdminMain", "Refresh clicked")
            viewModel.loadDashboardData()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.dashboardState.collect { state ->
                updateUI(state)
            }
        }


        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    showErrorDialog(it)
                }
            }
        }
    }

    private fun updateUI(analytics: Map<String, Any>) {
        binding.apply {
            TotalEvents.text = (analytics["totalEvents"] as? Number)?.toString() ?: "0"
            ActiveEvents.text = (analytics["activeEvents"] as? Number)?.toString() ?: "0"

            val categoryData = analytics["categoryData"] as? Map<String, Number>
            categoryData?.let { data ->
                MusicalCount.text = data["Musical"]?.toString() ?: "0"
                SportsCount.text = data["Sports"]?.toString() ?: "0"
                FoodCount.text = data["Food"]?.toString() ?: "0"
                ArtCount.text = data["Art"]?.toString() ?: "0"
            }
        }
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                true
            }
            R.id.action_settings -> {
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDashboardData()
    }
}