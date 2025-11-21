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

        // Validate the session on app start
        validateSession()
    }

    private fun validateSession() {
        // If a token exists, we need to validate it with the server
        if (tokenManager.isLoggedIn()) {
            binding.progress.visibility = View.VISIBLE
            
            lifecycleScope.launch {
                try {
                    // Create a service that requires authentication using the stored token
                    val authService = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = true)
                    
                    // Make a silent call to /me to check if token is still valid
                    withContext(Dispatchers.IO) {
                        authService.getMe() 
                    }
                    
                    // If the call succeeds, the token is valid. Go to Home without any message.
                    goToHome()

                } catch (e: Exception) {
                    // If the call fails (likely a 401 Unauthorized), the token is expired/invalid
                    Log.w("MainActivity_Validation", "Session validation failed. Token is likely expired.", e)
                    
                    tokenManager.clear() // Clear the invalid/expired session data
                    
                    // Show a message and prepare the UI for a manual login
                    Toast.makeText(this@MainActivity, "Tu sesión ha expirado", Toast.LENGTH_SHORT).show()
                    binding.progress.visibility = View.GONE
                    setupLoginAndRegisterUI()
                }
            }
        } else {
            // No token exists, just prepare the UI for a manual login
            setupLoginAndRegisterUI()
        }
    }
    
    // Encapsulates the setup of login and register buttons
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
                    // Step 1: Login with the public service to get a token
                    val publicAuthService = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = false)
                    val loginResponse = withContext(Dispatchers.IO) {
                        publicAuthService.login(LoginRequest(email = email, password = password))
                    }
                    val authToken = loginResponse.authToken

                    // Step 2: Create a private service using the NEW token to get user details
                    val privateAuthClient = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = true, token = authToken)
                    val userProfile = withContext(Dispatchers.IO) {
                        privateAuthClient.getMe()
                    }

                    if (userProfile.status?.lowercase() == "banned") {
                        Toast.makeText(this@MainActivity, "Usuario baneado", Toast.LENGTH_LONG).show()
                        tokenManager.clear() // Clear session for banned user
                    } else {
                        // Step 3: Now that we have all info, save the complete session
                        tokenManager.saveAuthWithRole(
                            token = authToken,
                            userId = userProfile.id,
                            userName = userProfile.username,
                            userEmail = userProfile.email,
                            userRole = userProfile.role
                        )

                        // Find the active cart from the list and save its ID
                        val activeCart = userProfile.cart?.find { it.status == "en proceso" }
                        activeCart?.id?.let {
                            tokenManager.saveCartId(it)
                        } ?: run {
                            Log.w("MainActivity_Login", "User ${userProfile.id} has no active cart in the list.")
                        }
                        
                        // ONLY show welcome message on manual login
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
