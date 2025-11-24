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

        validateSession()
    }

    private fun validateSession() {
        if (tokenManager.isLoggedIn()) {
            binding.progress.visibility = View.VISIBLE
            
            lifecycleScope.launch {
                try {
                    val authService = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = true)

                    withContext(Dispatchers.IO) {
                        authService.getMe() 
                    }

                    goToHome()

                } catch (e: Exception) {
                    Log.w("MainActivity_Validation", "Session validation failed. Token is likely expired.", e)
                    
                    tokenManager.clear()

                    Toast.makeText(this@MainActivity, "Tu sesión ha expirado", Toast.LENGTH_SHORT).show()
                    binding.progress.visibility = View.GONE
                    setupLoginAndRegisterUI()
                }
            }
        } else {
            setupLoginAndRegisterUI()
        }
    }

    private fun setupLoginAndRegisterUI() {
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
                    val publicAuthService = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = false)
                    val loginResponse = withContext(Dispatchers.IO) {
                        publicAuthService.login(LoginRequest(email = email, password = password))
                    }
                    val authToken = loginResponse.authToken

                    val privateAuthClient = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = true, token = authToken)
                    val userProfile = withContext(Dispatchers.IO) {
                        privateAuthClient.getMe()
                    }

                    if (userProfile.status?.lowercase() == "banned") {
                        Toast.makeText(this@MainActivity, "Usuario baneado", Toast.LENGTH_LONG).show()
                        tokenManager.clear()
                    } else {
                        tokenManager.saveAuthWithRole(
                            token = authToken,
                            userId = userProfile.id,
                            userName = userProfile.username,
                            userEmail = userProfile.email,
                            userRole = userProfile.role
                        )

                        val activeCart = userProfile.cart?.find { it.status == "en proceso" }
                        activeCart?.id?.let {
                            tokenManager.saveCartId(it)
                        } ?: run {
                            Log.w("MainActivity_Login", "User ${userProfile.id} has no active cart in the list.")
                        }

                        Toast.makeText(this@MainActivity, "¡Bienvenido, ${userProfile.username}!", Toast.LENGTH_SHORT).show()
                        goToHome()
                    }

                } catch (e: Exception) {
                    Log.e("MainActivity_Login", "Error during login process: ", e)
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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
