package com.mycat.purrfectstore2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.databinding.ActivityHomeBinding
import android.content.Context
import android.content.Intent
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mycat.purrfectstore2.api.TokenManager

class HomeActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.productFragment, R.id.profileFragment, R.id.cartFragment,
                R.id.addProductFragment, R.id.modifyListFragment, R.id.deleteProductFragment,
                R.id.myOrdersFragment, R.id.usersOrderListFragment, R.id.usersListFragment,
                R.id.BannedUsersListFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> {
                    androidx.navigation.ui.NavigationUI.onNavDestinationSelected(menuItem, navController)
                }
            }
        }
        setupDrawerHeader()
    }
    private fun setupDrawerHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.textViewUserName)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        val tokenManager = TokenManager(this)
        userNameTextView.text = tokenManager.getUserName()
        userEmailTextView.text = tokenManager.getUserEmail()
    }
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_home) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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
