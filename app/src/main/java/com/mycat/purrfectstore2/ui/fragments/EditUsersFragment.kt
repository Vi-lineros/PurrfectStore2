package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.UserService
import com.mycat.purrfectstore2.databinding.FragmentEditUsersBinding
import com.mycat.purrfectstore2.model.User
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class EditUsersFragment : Fragment() {

    private var _binding: FragmentEditUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var userService: UserService
    private val args: EditUsersFragmentArgs by navArgs()
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUsersBinding.inflate(inflater, container, false)
        userService = RetrofitClient.createUserService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.btnSaveUser.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                currentUser = userService.getUserById(args.userId)
                currentUser?.let { populateUI(it) }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error cargando datos del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateUI(user: User) {
        binding.textInputUsername.editText?.setText(user.username)
        binding.textInputEmail.editText?.setText(user.email)
        binding.textInputFirstName.editText?.setText(user.firstName)
        binding.textInputLastName.editText?.setText(user.lastName)
        binding.textInputAddress.editText?.setText(user.shippingAddress)
        binding.textInputPhone.editText?.setText(user.phoneNumber)

        if (user.role.equals("admin", ignoreCase = true)) {
            binding.radiogroupRole.check(R.id.radio_role_admin)
        } else {
            binding.radiogroupRole.check(R.id.radio_role_client)
        }
        
        // Removed hint to fix overlapping text issue.
    }

    private fun saveChanges() {
        // --- Start of Strict Validation ---
        val fields = mapOf(
            "El nombre de usuario" to binding.textInputUsername.editText?.text.toString().trim(),
            "El email" to binding.textInputEmail.editText?.text.toString().trim(),
            "La contraseña" to binding.textInputPassword.editText?.text.toString().trim(),
            "El nombre" to binding.textInputFirstName.editText?.text.toString().trim(),
            "El apellido" to binding.textInputLastName.editText?.text.toString().trim(),
            "La dirección" to binding.textInputAddress.editText?.text.toString().trim(),
            "El teléfono" to binding.textInputPhone.editText?.text.toString().trim()
        )

        for ((fieldName, value) in fields) {
            if (value.isEmpty()) {
                Toast.makeText(requireContext(), "$fieldName no puede estar vacío", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val email = fields["El email"]!!
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Por favor, introduce un email válido", Toast.LENGTH_SHORT).show()
            return
        }
        // --- End of Strict Validation ---

        // --- Build the full data map ---
        val fullUserData = mutableMapOf<String, @JvmSuppressWildcards Any>()
        fullUserData["name"] = fields["El nombre de usuario"]!!
        fullUserData["email"] = email
        fullUserData["password"] = fields["La contraseña"]!!
        fullUserData["first_name"] = fields["El nombre"]!!
        fullUserData["last_name"] = fields["El apellido"]!!
        fullUserData["shipping_address"] = fields["La dirección"]!!
        fullUserData["phone"] = fields["El teléfono"]!!

        val selectedRoleId = binding.radiogroupRole.checkedRadioButtonId
        fullUserData["role"] = if (selectedRoleId == R.id.radio_role_admin) "admin" else "cliente"

        // --- Send the full object ---
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updatedUser = userService.updateUser(currentUser!!.id, fullUserData)
                Toast.makeText(requireContext(), "Usuario '${updatedUser.username}' actualizado con éxito", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                // --- Start of Enhanced Error Handling ---
                var errorMessage = "Error al actualizar el usuario"
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
                        // Could not parse, use a generic 400 error message
                        errorMessage = "Error en los datos enviados (400)"
                    }
                } else {
                     errorMessage = e.message ?: errorMessage
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                 // --- End of Enhanced Error Handling ---
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
