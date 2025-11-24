package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.FragmentWelcomeBinding
import com.mycat.purrfectstore2.ui.HomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        val userRole = tokenManager.getUserRole()
        val action = if (userRole.equals("admin", ignoreCase = true) || userRole.equals("supremo", ignoreCase = true)) {
            WelcomeFragmentDirections.actionWelcomeFragmentToProductsAdminFragment()
        } else {
            WelcomeFragmentDirections.actionWelcomeFragmentToProductFragment()
        }
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        (activity as? HomeActivity)?.setDrawerLocked(true)
    }

    override fun onPause() {
        super.onPause()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        (activity as? HomeActivity)?.setDrawerLocked(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
