package com.mycat.purrfectstore2.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.ActivityRegisterBinding
import com.mycat.purrfectstore2.model.CreateCartRequest
import com.mycat.purrfectstore2.model.SignupBody
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRegisterButton()
        setupPasswordValidation()
    }

    private fun setupPasswordValidation() {
        binding.editTextPasswordRegister.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                val hasMinLength = password.length >= 8
                val hasNumber = password.any { it.isDigit() }

                updateRequirementView(binding.textViewLengthRequirement, hasMinLength)
                updateRequirementView(binding.textViewNumberRequirement, hasNumber)

                binding.buttonRegister.isEnabled = hasMinLength && hasNumber
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateRequirementView(textView: TextView, isMet: Boolean) {
        if (isMet) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.green))
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0)
        } else {
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_close, 0, 0, 0)
        }
    }

    private fun setupRegisterButton() {
        binding.buttonRegister.setOnClickListener {
            val name = binding.editTextNameRegister.text.toString().trim()
            val email = binding.editTextEmailRegister.text.toString().trim()
            val password = binding.editTextPasswordRegister.text.toString().trim()
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "El nombre y el email son obligatorios", Toast.LENGTH_SHORT).show()
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
                val cartService = RetrofitClient.createCartService(this@RegisterActivity)
                val tokenManager = TokenManager(this@RegisterActivity)

                val signupBody = SignupBody(name, email, password)
                val signupResponse = authService.signUp(signupBody)
                val authToken = signupResponse.authToken

                val privateAuthClient = RetrofitClient.createAuthService(this@RegisterActivity, true, token = authToken)
                val userProfile = privateAuthClient.getMe()

                tokenManager.saveAuthWithRole(
                    token = authToken,
                    userId = userProfile.id,
                    userName = userProfile.username,
                    userEmail = userProfile.email,
                    userRole = "cliente"
                )

                val createCartRequest = CreateCartRequest(user_id = userProfile.id)
                val newCart = cartService.createCart(createCartRequest)
                tokenManager.saveCartId(newCart.id)

                val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error"
                Log.e("RegisterActivity", "Error en el registro: $errorMessage")
                Toast.makeText(this@RegisterActivity, "Error en el registro: $errorMessage", Toast.LENGTH_LONG).show()
            } finally {
                // Re-enable button only if validation is still met, handled by TextWatcher
            }
        }
    }
}
