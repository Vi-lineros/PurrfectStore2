package com.mycat.purrfectstore2.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.mycat.purrfectstore2.ui.fragments.ProductsAdminFragment
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var cancelButton: TextView
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        cancelButton = binding.toolbar.findViewById(R.id.cancel_button)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
        navController = navHostFragment.navController
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // All drawer destinations are considered top-level.
        // Any navigation away from these will show a back arrow.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.productFragment, R.id.profileFragment, R.id.cartFragment,
                R.id.myOrdersFragment, R.id.usersOrderListFragment,
                R.id.usersListFragment, R.id.productsAdminFragment
            ), drawerLayout
        )
        // This single line handles the hamburger/arrow icon, title, and drawer opening.
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()

            if (menuItem.itemId == R.id.nav_logout) {
                showLogoutConfirmationDialog()
                return@setNavigationItemSelectedListener true
            }
            val navigated = NavigationUI.onNavDestinationSelected(menuItem, navController)
            return@setNavigationItemSelectedListener navigated
        }

        setupDrawerHeader()

        cancelButton.setOnClickListener {
            val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
            if (currentFragment is ProductsAdminFragment) {
                currentFragment.exitSelectionMode()
            }
        }
    }

    private fun setupDrawerHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.textViewUserName)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        val tokenManager = TokenManager(this)
        userNameTextView.text = tokenManager.getUserName()
        userEmailTextView.text = tokenManager.getUserEmail()
    }

    // This method correctly handles back navigation for the back arrow
    // and the hamburger icon for the drawer.
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showCancelButton(show: Boolean) {
        cancelButton.visibility = if (show) View.VISIBLE else View.GONE

        // Hide the navigation icon (hamburger/arrow) when the cancel button is visible.
        supportActionBar?.setDisplayHomeAsUpEnabled(!show)

        // Lock the drawer to prevent it from opening during selection mode.
        val lockMode = if (show) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED
        binding.drawerLayout.setDrawerLockMode(lockMode)
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Cierre de Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Aceptar") { _, _ ->
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        val sharedPreferences = getSharedPreferences("session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("jwt_token")
        editor.apply()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
