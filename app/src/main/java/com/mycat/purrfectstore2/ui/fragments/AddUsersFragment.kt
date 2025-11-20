package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.CartService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.UserCreationRequest
import com.mycat.purrfectstore2.api.UserService
import com.mycat.purrfectstore2.databinding.FragmentAddUsersBinding
import com.mycat.purrfectstore2.model.CreateCartRequest
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class AddUsersFragment : Fragment() {

    private var _binding: FragmentAddUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var userService: UserService
    private lateinit var cartService: CartService // Service to create the cart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddUsersBinding.inflate(inflater, container, false)
        userService = RetrofitClient.createUserService(requireContext())
        cartService = RetrofitClient.createCartService(requireContext()) // Initialize CartService
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreateUser.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {
        val username = binding.textInputUsername.editText?.text.toString().trim()
        val email = binding.textInputEmail.editText?.text.toString().trim()
        val password = binding.textInputPassword.editText?.text.toString().trim()
        val firstName = binding.textInputFirstName.editText?.text.toString().trim()
        val lastName = binding.textInputLastName.editText?.text.toString().trim()
        val address = binding.textInputAddress.editText?.text.toString().trim()
        val phone = binding.textInputPhone.editText?.text.toString().trim()

        val selectedRoleId = binding.radiogroupRole.checkedRadioButtonId
        val role = if (selectedRoleId == R.id.radio_role_admin) "admin" else "cliente"

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val userRequest = UserCreationRequest(
            name = username,
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            role = role,
            status = "normal",
            shippingAddress = address,
            phoneNumber = phone
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Create the user first
                val newUser = userService.createUser(userRequest)
                
                // Immediately create a cart for the newly created user
                val createCartRequest = CreateCartRequest(user_id = newUser.id)
                cartService.createCart(createCartRequest)

                Toast.makeText(requireContext(), "¡Usuario creado con éxito!", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                var errorMessage = "Error al crear usuario"
                if (e is HttpException && e.code() == 400) {
                    try {
                        val errorBody = e.response()?.errorBody()?.string() ?: "{}"
                        val json = JSONObject(errorBody)
                        val serverMessage = json.optString("message", "").lowercase()

                        if (serverMessage.contains("password") || serverMessage.contains("contraseña") || serverMessage.contains("weak") || serverMessage.contains("debil")) {
                            errorMessage = "Contraseña muy débil"
                        } else {
                            errorMessage = json.optString("message", errorMessage)
                        }
                    } catch (jsonE: Exception) {
                        errorMessage = "Error en los datos enviados (400)"
                    }
                } else {
                     errorMessage = e.message ?: errorMessage
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
