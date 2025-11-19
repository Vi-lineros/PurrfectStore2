package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.AuthService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.api.UserService
import com.mycat.purrfectstore2.databinding.FragmentEditProfileBinding
import com.mycat.purrfectstore2.model.User
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authService: AuthService
    private lateinit var userService: UserService
    private lateinit var tokenManager: TokenManager
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        // Use the saved token to create a private (authenticated) auth service
        authService = RetrofitClient.createAuthService(requireContext(), true, tokenManager.getToken())
        // The user service is needed to update the user
        userService = RetrofitClient.createUserService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
        setupSaveButton()
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                // Use the /auth/me endpoint to get the user's own data
                val userProfile = authService.getMe()
                currentUser = userProfile
                populateUI(userProfile)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar el perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populateUI(user: User) {
        binding.textInputUsernameEdit.setText(user.username)
        binding.textInputEmailEdit.setText(user.email)
        binding.textInputFirstNameEdit.setText(user.firstName ?: "")
        binding.textInputLastNameEdit.setText(user.lastName ?: "")
        binding.textInputAddressEdit.setText(user.shippingAddress ?: "")
        binding.textInputPhoneEdit.setText(user.phoneNumber ?: "")

        Glide.with(this)
            .load(user.photoUrl)
            .placeholder(R.drawable.fresa)
            .error(R.drawable.fresa)
            .into(binding.imageViewProfileEdit)
    }

    private fun setupSaveButton() {
        binding.buttonSaveProfile.setOnClickListener {
            currentUser?.let {
                updateUserProfile(it.id)
            }
        }
    }

    private fun updateUserProfile(userId: Int) {
        lifecycleScope.launch {
            try {
                val updatedData = mutableMapOf<String, @JvmSuppressWildcards Any>()
                updatedData["name"] = binding.textInputUsernameEdit.text.toString()
                updatedData["email"] = binding.textInputEmailEdit.text.toString()
                updatedData["first_name"] = binding.textInputFirstNameEdit.text.toString()
                updatedData["last_name"] = binding.textInputLastNameEdit.text.toString()
                updatedData["shipping_address"] = binding.textInputAddressEdit.text.toString()
                updatedData["phone"] = binding.textInputPhoneEdit.text.toString()

                // Password update logic is now completely removed

                userService.updateUser(userId, updatedData)
                Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()

            } catch (e: Exception) {
                Toast.makeText(context, "Error al actualizar el perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
