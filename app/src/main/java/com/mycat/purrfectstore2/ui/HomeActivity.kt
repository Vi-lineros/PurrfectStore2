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
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.ActivityHomeBinding
import com.mycat.purrfectstore2.ui.fragments.ProductsAdminFragment
import com.mycat.purrfectstore2.ui.fragments.UsersListFragment

class HomeActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var cancelButton: TextView
    private lateinit var navController: NavController
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        tokenManager = TokenManager(this)
        cancelButton = binding.toolbar.findViewById(R.id.cancel_button)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
        navController = navHostFragment.navController
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.productFragment, R.id.profileFragment, R.id.cartFragment,
                R.id.myOrdersFragment, R.id.usersOrderListFragment,
                R.id.usersListFragment, R.id.productsAdminFragment
            ), drawerLayout
        )
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
        setupRoleBasedMenu()

        // This listener will now handle both fragment types
        cancelButton.setOnClickListener {
            val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
            when (currentFragment) {
                is ProductsAdminFragment -> currentFragment.exitSelectionMode()
                is UsersListFragment -> currentFragment.exitSelectionMode()
            }
        }
    }

    private fun setupDrawerHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.textViewUserName)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        userNameTextView.text = tokenManager.getUserName()
        userEmailTextView.text = tokenManager.getUserEmail()
    }

    private fun setupRoleBasedMenu() {
        val userRole = tokenManager.getUserRole()
        val isAdmin = userRole.equals("admin", ignoreCase = true)
        
        val navView: NavigationView = binding.navView
        navView.menu.setGroupVisible(R.id.group_admin, isAdmin)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showCancelButton(show: Boolean) {
        cancelButton.visibility = if (show) View.VISIBLE else View.GONE
        supportActionBar?.setDisplayHomeAsUpEnabled(!show)
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
        tokenManager.clear()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
