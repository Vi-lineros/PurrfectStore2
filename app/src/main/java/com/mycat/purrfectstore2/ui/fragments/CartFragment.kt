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
import com.mycat.purrfectstore2.api.CartService
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.FragmentCartBinding
import com.mycat.purrfectstore2.model.CartProduct
import com.mycat.purrfectstore2.model.UpdateCartProductsRequest
import com.mycat.purrfectstore2.ui.adapter.CartAdapter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartService: CartService
    private lateinit var productService: ProductService
    private lateinit var tokenManager: TokenManager
    private lateinit var cartAdapter: CartAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        cartService = RetrofitClient.createCartService(requireContext())
        productService = RetrofitClient.createProductService(requireContext())
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        loadCart()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter { _ ->
            binding.buttonUpdateCart.visibility = View.VISIBLE
            updateGrandTotal()
        }
        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun setupButtons() {
        binding.buttonUpdateCart.setOnClickListener {
            updateCart()
        }
        binding.buttonCheckout.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidad de 'Enviar Pedido' no implementada todavía", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadCart() {
        setLoadingState(true)
        binding.buttonUpdateCart.visibility = View.GONE

        lifecycleScope.launch {
            try {
                // Add a small delay to prevent 429 Too Many Requests errors
                delay(400)

                val cartId = tokenManager.getCartId()
                if (cartId == -1) throw IllegalStateException("Carrito no válido.")

                val cart = cartService.getCart(cartId)
                val cartProducts = cart.product_id

                if (cartProducts.isNullOrEmpty()) {
                    showEmptyState(true)
                    updateGrandTotal() // Ensure total is zero for empty cart
                } else {
                    showEmptyState(false)
                    val populatedCartProducts = fetchProductDetailsForCart(cartProducts)
                    cartAdapter.updateItems(populatedCartProducts)
                    updateGrandTotal()
                }
            } catch (e: Exception) {
                showError("Error al cargar el carrito: ${e.message}")
                showEmptyState(true)
            } finally {
                setLoadingState(false)
            }
        }
    }
    
    private fun updateGrandTotal() {
        val total = cartAdapter.getItems().sumOf { cartProduct ->
            val price = cartProduct.product_details?.price ?: 0.0
            price * cartProduct.quantity
        }
        binding.textViewGrandTotal.text = currencyFormat.format(total)
    }

    private suspend fun fetchProductDetailsForCart(cartProducts: List<CartProduct>): List<CartProduct> = coroutineScope {
        cartProducts.map { cartProduct ->
            async {
                try {
                    val productDetails = productService.getProductId(cartProduct.product_id)
                    cartProduct.product_details = productDetails
                } catch (e: Exception) {
                   // Log error if needed, but don't crash
                }
                cartProduct
            }
        }.awaitAll()
    }

    private fun updateCart() {
        val currentItems = cartAdapter.getItems()
        val itemsToKeep = currentItems.filter { it.quantity > 0 }

        val cartId = tokenManager.getCartId()
        if (cartId == -1) {
            showError("Error: No se pudo encontrar el carrito.")
            return
        }

        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val updateRequest = UpdateCartProductsRequest(products = itemsToKeep)
                cartService.updateCart(cartId, updateRequest)
                Toast.makeText(requireContext(), "Carrito actualizado con éxito", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                showError("Error al actualizar el carrito: ${e.message}")
            } finally {
                loadCart() // Reload to reflect changes and new state
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBarCart.isVisible = isLoading
        if (isLoading) {
            binding.recyclerViewCart.isVisible = false
            binding.totalContainer.isVisible = false
            binding.bottomContainer.isVisible = false
            binding.textViewEmptyCart.isVisible = false // Corrected reference
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.textViewEmptyCart.isVisible = isEmpty // Corrected reference
        binding.recyclerViewCart.isVisible = !isEmpty
        binding.totalContainer.isVisible = !isEmpty
        binding.bottomContainer.isVisible = true

        if (isEmpty) {
            binding.buttonUpdateCart.visibility = View.GONE
            binding.buttonCheckout.isEnabled = false
            binding.buttonCheckout.alpha = 0.5f
        } else {
            binding.buttonCheckout.isEnabled = true
            binding.buttonCheckout.alpha = 1.0f
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        setLoadingState(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
