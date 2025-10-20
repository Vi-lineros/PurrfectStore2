package com.mycat.purrfectstore2.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.ActivityMainBinding
import com.mycat.purrfectstore2.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = TokenManager(this)
        if (tokenManager.isLoggedIn()) {
            goToHome()
            return
        }
        setupLoginButton()
        setupRegisterTextView()
    }
    private fun setupLoginButton() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextfieldEmail.text?.toString()?.trim().orEmpty()
            val password = binding.editTextfieldPassword.text?.toString()?.trim().orEmpty()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Completa email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.progress.visibility = View.VISIBLE
            binding.buttonLogin.isEnabled = false
            lifecycleScope.launch {
                try {
                    val publicAuthService = RetrofitClient.createAuthService(this@MainActivity)
                    val loginResponse = withContext(Dispatchers.IO) {
                        publicAuthService.login(LoginRequest(email = email, password = password))
                    }
                    val authToken = loginResponse.authToken
                    tokenManager.saveAuth(token = authToken, userName = "", userEmail = "")
                    val privateAuthClient = RetrofitClient.createAuthService(this@MainActivity, true)
                    val userProfile = withContext(Dispatchers.IO) {
                        privateAuthClient.getMe()
                    }
                    tokenManager.saveAuth(
                        token = authToken,
                        userName = userProfile.name,
                        userEmail = userProfile.email
                    )
                    Toast.makeText(this@MainActivity, "¡Bienvenido, ${userProfile.name}!", Toast.LENGTH_SHORT).show()
                    goToHome()
                } catch (e: Exception) {
                    Log.e("MainActivity_Login", "Error en el proceso de login: ${e.message}", e)
                    Toast.makeText(this@MainActivity, "Email o Contraseña incorrectos", Toast.LENGTH_LONG).show()
                    tokenManager.clear()
                } finally {
                    binding.progress.visibility = View.GONE
                    binding.buttonLogin.isEnabled = true
                }
            }
        }
    }
    private fun setupRegisterTextView() {
        binding.textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
