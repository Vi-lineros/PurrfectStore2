package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.AuthService
import com.mycat.purrfectstore2.api.CartService
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.databinding.FragmentOrderDetailsBinding
import com.mycat.purrfectstore2.model.CartProduct
import com.mycat.purrfectstore2.ui.adapter.OrderDetailsAdapter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OrderDetailsFragment : Fragment() {

    private var _binding: FragmentOrderDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: OrderDetailsFragmentArgs by navArgs()

    private lateinit var cartService: CartService
    private lateinit var authService: AuthService
    private lateinit var productService: ProductService
    private lateinit var productsAdapter: OrderDetailsAdapter

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        cartService = RetrofitClient.createCartService(requireContext())
        authService = RetrofitClient.createAuthService(requireContext(), requiresAuth = true)
        productService = RetrofitClient.createProductService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadOrderDetails()
    }

    private fun setupRecyclerView() {
        productsAdapter = OrderDetailsAdapter(emptyList())
        binding.recyclerViewOrderProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadOrderDetails() {
        setLoadingState(true)
        val orderId = args.orderId

        lifecycleScope.launch {
            try {
                delay(1500) // Set delay to 1.5 seconds
                // Fetch order and user details concurrently
                val orderDeferred = async { cartService.getCart(orderId) }
                val userDeferred = async { authService.getMe() }
                
                val order = orderDeferred.await()
                val user = userDeferred.await()

                handleOrderStatus(order.status, user.shippingAddress, order.created_at)

                binding.textViewOrderTotal.text = currencyFormat.format(order.total ?: 0.0)

                val productList = order.product_id
                if (productList.isNullOrEmpty()) {
                    binding.cardViewProducts.isVisible = false
                } else {
                    binding.cardViewProducts.isVisible = true
                    val populatedProducts = fetchProductDetailsForOrder(productList)
                    productsAdapter.updateProducts(populatedProducts)
                }

            } catch (e: Exception) {
                showError("Error al cargar los detalles del pedido: ${e.message}")
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun handleOrderStatus(status: String, address: String?, createdAt: Long) {
        val statusTextView = binding.textViewOrderStatus
        val messageTextView = binding.textViewStatusMessage
        val dateTextView = binding.textViewDeliveryDate
        
        statusTextView.text = status.replaceFirstChar { it.uppercase() }
        val backgroundRes = when (status.lowercase()) {
            "aprobado" -> R.drawable.status_approved_background
            "rechazado" -> R.drawable.status_rejected_background
            "pendiente" -> R.drawable.status_pending_background
            else -> R.drawable.bg_status_active
        }
        statusTextView.setBackgroundResource(backgroundRes)

        when (status.lowercase()) {
            "aprobado" -> {
                if (address.isNullOrBlank()) {
                    messageTextView.text = "Debe agregar una dirección en su perfil para el envío."
                    dateTextView.isVisible = false
                } else {
                    messageTextView.text = "Su pedido será enviado a: $address"
                    
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = createdAt
                    calendar.add(Calendar.YEAR, 1)
                    dateTextView.text = "Fecha estimada de llegada: ${dateFormat.format(calendar.time)}"
                    dateTextView.isVisible = true
                }
            }
            "rechazado" -> {
                messageTextView.text = "Lo sentimos, su pedido fue rechazado."
                dateTextView.isVisible = false
            }
            "pendiente" -> {
                messageTextView.text = "El pedido se encuentra siendo revisado."
                dateTextView.isVisible = false
            }
            else -> {
                binding.cardViewStatusMessage.isVisible = false
            }
        }
    }

    private suspend fun fetchProductDetailsForOrder(products: List<CartProduct>): List<CartProduct> = coroutineScope {
        products.map { productItem ->
            async {
                try {
                    val productDetails = productService.getProductId(productItem.product_id)
                    productItem.product_details = productDetails
                } catch (e: Exception) {
                    // Log or handle error for a single product fetch
                }
                productItem
            }
        }.awaitAll()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBarOrderDetails.isVisible = isLoading
        binding.scrollView.isVisible = !isLoading
        binding.totalContainer.isVisible = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
