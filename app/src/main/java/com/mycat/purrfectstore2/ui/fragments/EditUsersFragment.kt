package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
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
        
        binding.textInputPassword.editText?.hint = "Dejar en blanco para no cambiar"
    }

    private fun saveChanges() {
        val userToUpdate = currentUser ?: return

        val updatedFields = mutableMapOf<String, @JvmSuppressWildcards Any>()

        val username = binding.textInputUsername.editText?.text.toString().trim()
        if (username != userToUpdate.username) updatedFields["name"] = username

        val email = binding.textInputEmail.editText?.text.toString().trim()
        if (email != userToUpdate.email) updatedFields["email"] = email

        val firstName = binding.textInputFirstName.editText?.text.toString().trim()
        if (firstName != userToUpdate.firstName) updatedFields["first_name"] = firstName

        val lastName = binding.textInputLastName.editText?.text.toString().trim()
        if (lastName != userToUpdate.lastName) updatedFields["last_name"] = lastName

        val address = binding.textInputAddress.editText?.text.toString().trim()
        if (address != userToUpdate.shippingAddress) updatedFields["shipping_address"] = address

        val phone = binding.textInputPhone.editText?.text.toString().trim()
        if (phone != userToUpdate.phoneNumber) updatedFields["phone"] = phone

        val selectedRoleId = binding.radiogroupRole.checkedRadioButtonId
        val role = if (selectedRoleId == R.id.radio_role_admin) "admin" else "cliente"
        if (role != userToUpdate.role) updatedFields["role"] = role

        // As per your request, re-enabling password changes from the admin panel.
        val password = binding.textInputPassword.editText?.text.toString().trim()
        if (password.isNotEmpty()) {
            updatedFields["password"] = password
        }

        if (updatedFields.isEmpty()) {
            Toast.makeText(requireContext(), "No se han realizado cambios", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updatedUser = userService.updateUser(userToUpdate.id, updatedFields)
                Toast.makeText(requireContext(), "Usuario '${updatedUser.username}' actualizado con éxito", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al actualizar el usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
