package com.mycat.purrfectstore2.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.AuthService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.FragmentProfileBinding
import com.mycat.purrfectstore2.model.User
import com.mycat.purrfectstore2.ui.HomeActivity
import com.mycat.purrfectstore2.ui.MainActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authService: AuthService
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        authService = RetrofitClient.createAuthService(requireContext(), true, tokenManager.getToken())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
        setupEditButton()
        setupLogoutButton()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBarProfile.isVisible = isLoading
        binding.contentContainer.isVisible = !isLoading
        binding.fabEditProfile.isVisible = !isLoading
        (activity as? HomeActivity)?.setDrawerLocked(isLoading)
    }

    private fun loadUserProfile() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val userProfile = authService.getMe()
                populateUI(userProfile)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar el perfil: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun populateUI(user: User) {
        binding.textViewProfileNameTitle.text = user.username
        binding.textInputEmail.setText(user.email)
        binding.textInputFirstName.setText(user.firstName ?: "")
        binding.textInputLastName.setText(user.lastName ?: "")
        binding.textInputAddress.setText(user.shippingAddress ?: "")
        binding.textInputPhone.setText(user.phoneNumber ?: "")

        Glide.with(this)
            .load(user.photoUrl)
            .placeholder(R.drawable.fresa)
            .error(R.drawable.fresa)
            .into(binding.imageViewProfile)
    }

    private fun setupEditButton() {
        binding.fabEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }

    private fun setupLogoutButton() {
        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar Cierre de Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Aceptar") { _, _ ->
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        val activityContext = requireActivity()
        val tokenManager = TokenManager(activityContext)
        tokenManager.clear()
        val intent = Intent(activityContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activityContext.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
