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

        binding.textInputPassword.editText?.setOnFocusChangeListener { _, hasFocus ->
            binding.textInputPassword.placeholderText = if (hasFocus) "Dejar en blanco para no cambiar" else null
        }

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
    }

    private fun saveChanges() {
        val originalUser = currentUser ?: return

        val newUsername = binding.textInputUsername.editText?.text.toString().trim()
        val newEmail = binding.textInputEmail.editText?.text.toString().trim()

        // 1. Mandatory fields validation
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Es obligatorio que tenga nombre y el email", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(requireContext(), "Por favor, introduce un email válido", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Build map of changed data, but always include name and email
        val updateData = mutableMapOf<String, Any>()
        var hasChanges = false

        // Always add name and email
        updateData["name"] = newUsername
        updateData["email"] = newEmail

        // Add other fields only if they have changed
        val newPassword = binding.textInputPassword.editText?.text.toString().trim()
        if (newPassword.isNotEmpty()) {
            updateData["password"] = newPassword
            hasChanges = true
        }

        val newFirstName = binding.textInputFirstName.editText?.text.toString().trim()
        if (newFirstName != originalUser.firstName.orEmpty()) {
            updateData["first_name"] = newFirstName
            hasChanges = true
        }

        val newLastName = binding.textInputLastName.editText?.text.toString().trim()
        if (newLastName != originalUser.lastName.orEmpty()) {
            updateData["last_name"] = newLastName
            hasChanges = true
        }

        val newAddress = binding.textInputAddress.editText?.text.toString().trim()
        if (newAddress != originalUser.shippingAddress.orEmpty()) {
            updateData["shipping_address"] = newAddress
            hasChanges = true
        }

        val newPhone = binding.textInputPhone.editText?.text.toString().trim()
        if (newPhone != originalUser.phoneNumber.orEmpty()) {
            updateData["phone"] = newPhone
            hasChanges = true
        }

        val newRole = if (binding.radiogroupRole.checkedRadioButtonId == R.id.radio_role_admin) "admin" else "cliente"
        if (newRole != originalUser.role) {
            updateData["role"] = newRole
            hasChanges = true
        }
        
        if(newUsername != originalUser.username || newEmail != originalUser.email){
             hasChanges = true
        }

        // 3. Check for effective changes
        if (!hasChanges) {
            Toast.makeText(requireContext(), "No hay cambios para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. Send the update to the API
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updatedUser = userService.updateUser(originalUser.id, updateData)
                Toast.makeText(requireContext(), "Usuario '${updatedUser.username}' actualizado con éxito", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
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
