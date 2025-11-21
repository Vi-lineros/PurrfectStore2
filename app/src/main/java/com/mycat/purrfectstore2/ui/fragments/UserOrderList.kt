package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mycat.purrfectstore2.api.CartService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.UserService
import com.mycat.purrfectstore2.databinding.FragmentUserOrderListBinding
import com.mycat.purrfectstore2.model.User
import com.mycat.purrfectstore2.ui.HomeActivity
import com.mycat.purrfectstore2.ui.adapter.OrderWithUser
import com.mycat.purrfectstore2.ui.adapter.UserOrderListAdapter
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Locale

class UserOrderList : Fragment() {

    private var _binding: FragmentUserOrderListBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: UserOrderListAdapter
    private lateinit var cartService: CartService
    private lateinit var userService: UserService
    private var allOrders: List<OrderWithUser> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserOrderListBinding.inflate(inflater, container, false)
        cartService = RetrofitClient.createCartService(requireContext())
        userService = RetrofitClient.createUserService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        loadOrdersAndUsers()
    }

    private fun setupRecyclerView() {
        orderAdapter = UserOrderListAdapter(emptyList())
        binding.recyclerViewUserOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchViewUserOrders.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (binding.recyclerViewUserOrders.adapter as? UserOrderListAdapter)?.filter(newText)
                return true
            }
        })
    }

    private fun loadOrdersAndUsers() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                // Fetch all carts and all users in parallel
                val cartsDeferred = async { cartService.getCarritos() }
                val usersDeferred = async { userService.getUsers() }

                val allCarts = cartsDeferred.await()
                val allUsers = usersDeferred.await()

                // Filter out carts with "en proceso" status
                val filteredCarts = allCarts.filter { it.status.lowercase(Locale.getDefault()) != "en proceso" }

                // Create a map of user IDs to usernames for quick lookup
                val userMap = allUsers.associateBy(User::id, User::username)

                // Combine carts with usernames
                val ordersWithUsers = filteredCarts.mapNotNull { cart ->
                    userMap[cart.user_id]?.let { username ->
                        OrderWithUser(cart, username)
                    }
                }

                allOrders = ordersWithUsers.sortedByDescending { it.cart.created_at }
                orderAdapter.updateOrders(allOrders)
                updateEmptyState(allOrders.isEmpty())

            } catch (e: Exception) {
                updateEmptyState(true)
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBarUserOrders.isVisible = isLoading
        (activity as? HomeActivity)?.setDrawerLocked(isLoading) // Lock/Unlock Drawer
        binding.recyclerViewUserOrders.isVisible = !isLoading
        if (isLoading) {
             binding.textViewNoOrders.isVisible = false
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.textViewNoOrders.isVisible = isEmpty
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
