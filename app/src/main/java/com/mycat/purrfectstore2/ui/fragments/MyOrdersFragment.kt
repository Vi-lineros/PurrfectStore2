package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mycat.purrfectstore2.api.AuthService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.databinding.FragmentMyOrdersBinding
import com.mycat.purrfectstore2.ui.HomeActivity
import com.mycat.purrfectstore2.ui.adapter.MyOrdersAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyOrdersFragment : Fragment() {

    private var _binding: FragmentMyOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var authService: AuthService
    private lateinit var ordersAdapter: MyOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyOrdersBinding.inflate(inflater, container, false)
        authService = RetrofitClient.createAuthService(requireContext(), requiresAuth = true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToRefresh()
        loadOrders()
    }

    private fun setupRecyclerView() {
        ordersAdapter = MyOrdersAdapter(emptyList())
        binding.recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ordersAdapter
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayoutOrders.setOnRefreshListener {
            loadOrders()
        }
    }

    private fun loadOrders() {
        if (!binding.swipeRefreshLayoutOrders.isRefreshing) {
            setLoadingState(true)
        }

        lifecycleScope.launch {
            try {
                delay(1500) // Set delay to 1.5 seconds
                val userProfile = authService.getMe()
                
                val completedOrders = userProfile.cart?.filter { it.status != "en proceso" } ?: emptyList()

                if (completedOrders.isEmpty()) {
                    showEmptyState(true)
                } else {
                    showEmptyState(false)
                    ordersAdapter.updateOrders(completedOrders.sortedByDescending { it.created_at })
                }

            } catch (e: Exception) {
                showError("Error al cargar los pedidos: ${e.message}")
                showEmptyState(true)
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBarOrders.isVisible = isLoading
        (activity as? HomeActivity)?.setDrawerLocked(isLoading) // Lock/Unlock Drawer

        // Hide content while loading
        if (isLoading) {
            binding.recyclerViewOrders.isVisible = false
            binding.textViewNoOrders.isVisible = false
        }
        
        if (!isLoading) {
            binding.swipeRefreshLayoutOrders.isRefreshing = false
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.textViewNoOrders.isVisible = isEmpty
        binding.recyclerViewOrders.isVisible = !isEmpty
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
