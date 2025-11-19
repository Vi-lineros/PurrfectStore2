package com.mycat.purrfectstore2.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.ActivityRegisterBinding
import com.mycat.purrfectstore2.model.SignupBody
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRegisterButton()
    }

    private fun setupRegisterButton() {
        binding.buttonRegister.setOnClickListener {
            val name = binding.editTextNameRegister.text.toString().trim()
            val email = binding.editTextEmailRegister.text.toString().trim()
            val password = binding.editTextPasswordRegister.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Por favor, introduce un email válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            performRegistration(name, email, password)
        }
    }

    private fun performRegistration(name: String, email: String, password: String) {
        binding.buttonRegister.isEnabled = false
        lifecycleScope.launch {
            try {
                val authService = RetrofitClient.createAuthService(this@RegisterActivity)
                val signupBody = SignupBody(name, email, password)
                val response = authService.signUp(signupBody)

                // Use the new function and assign a default role of "cliente"
                TokenManager(this@RegisterActivity).saveAuthWithRole(response.authToken, name, email, "cliente")

                Toast.makeText(this@RegisterActivity, "¡Registro exitoso!", Toast.LENGTH_LONG).show()
                val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error"
                Log.e("RegisterActivity", "Error en el registro: $errorMessage")
                Toast.makeText(this@RegisterActivity, "Error en el registro: $errorMessage", Toast.LENGTH_LONG).show()
            } finally {
                binding.buttonRegister.isEnabled = true
            }
        }
    }
}
