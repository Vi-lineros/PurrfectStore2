package com.mycat.purrfectstore2.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.FragmentProfileBinding
import com.mycat.purrfectstore2.ui.MainActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserData()
        setupLogoutButton()
    }

    private fun setupUserData() {
        val tokenManager = TokenManager(requireContext())
        binding.textViewProfileName.text = tokenManager.getUserName()
        binding.textViewProfileEmail.text = tokenManager.getUserEmail()
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
