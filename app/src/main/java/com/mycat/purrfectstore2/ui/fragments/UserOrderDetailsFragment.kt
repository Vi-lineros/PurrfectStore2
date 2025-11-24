package com.mycat.purrfectstore2.ui.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mycat.purrfectstore2.api.CartService
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.model.UpdateCartStatusRequest
import com.mycat.purrfectstore2.databinding.FragmentUserOrderDetailsBinding
import com.mycat.purrfectstore2.model.CartProduct
import com.mycat.purrfectstore2.model.UpdateProductRequest
import com.mycat.purrfectstore2.ui.adapter.OrderDetailProductAdapter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class UserOrderDetailsFragment : Fragment() {

    private var _binding: FragmentUserOrderDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: UserOrderDetailsFragmentArgs by navArgs()

    private lateinit var cartService: CartService
    private lateinit var productService: ProductService
    private lateinit var productAdapter: OrderDetailProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserOrderDetailsBinding.inflate(inflater, container, false)
        cartService = RetrofitClient.createCartService(requireContext())
        productService = RetrofitClient.createProductService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupActionButtons()
        loadOrderDetails()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.layoutActionButtons.isVisible = !isLoading
        binding.recyclerViewOrderProducts.isVisible = !isLoading
        binding.cardViewOrderTotals.isVisible = !isLoading
    }

    private fun setupRecyclerView() {
        productAdapter = OrderDetailProductAdapter(emptyList())
        binding.recyclerViewOrderProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }

    private fun setupActionButtons() {
        binding.buttonApprove.setOnClickListener {
            showConfirmationDialog("aprobado")
        }
        binding.buttonReject.setOnClickListener {
            showConfirmationDialog("rechazado")
        }
    }

    private fun loadOrderDetails() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                delay(500)
                val cart = cartService.getCart(args.orderId)
                val populatedProducts = fetchProductDetailsForCart(cart.product_id ?: emptyList())
                productAdapter.updateProducts(populatedProducts)

                val total = populatedProducts.sumOf { (it.product_details?.price ?: 0.0) * it.quantity }
                binding.textViewTotalValue.text = NumberFormat.getCurrencyInstance(Locale.US).format(total)

                updateButtonState(cart.status)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar el pedido: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                if (_binding != null) {
                    setLoading(false)
                }
            }
        }
    }

    private suspend fun fetchProductDetailsForCart(products: List<CartProduct>): List<CartProduct> = coroutineScope {
        products.map { cartProduct ->
            async {
                try {
                    cartProduct.product_details = productService.getProductId(cartProduct.product_id)
                } catch (e: Exception) {}
                cartProduct
            }
        }.awaitAll()
    }

    private fun showConfirmationDialog(newStatus: String) {
        val actionText = if (newStatus == "aprobado") "aprobar" else "rechazar"
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Acción")
            .setMessage("¿Desea $actionText este pedido?")
            .setPositiveButton("Confirmar") { _, _ ->
                updateOrderStatus(newStatus)
            }
            .setNegativeButton("Cancelar", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
    }

    private fun updateOrderStatus(newStatus: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                delay(500)

                if (newStatus == "aprobado") {
                    val cart = cartService.getCart(args.orderId)
                    updateStockForOrder(cart.product_id ?: emptyList())
                }

                cartService.updateCartStatus(args.orderId, UpdateCartStatusRequest(newStatus))
                Toast.makeText(context, "Pedido ${newStatus.replaceFirstChar { it.uppercase() }}", Toast.LENGTH_SHORT).show()
                updateButtonState(newStatus)

            } catch (e: Exception) {
                Toast.makeText(context, "Error al actualizar el estado: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                if (_binding != null) {
                    setLoading(false)
                }
            }
        }
    }

    private suspend fun updateStockForOrder(products: List<CartProduct>) = coroutineScope {
        products.forEach { cartProduct ->
            launch {
                try {
                    val product = productService.getProductId(cartProduct.product_id)
                    val newStock = product.stock - cartProduct.quantity
                    
                    val updateRequest = UpdateProductRequest(stock = newStock)

                    productService.updateProduct(product.id, updateRequest)

                } catch (e: Exception) {}
            }
        }
    }

    private fun updateButtonState(status: String) {
        val isActionable = status.lowercase(Locale.getDefault()) == "pendiente"
        binding.buttonApprove.isVisible = isActionable
        binding.buttonReject.isVisible = isActionable

        if (!isActionable) {
            val finalButton = if (status.lowercase(Locale.getDefault()) == "aprobado") binding.buttonApprove else binding.buttonReject
            val otherButton = if (status.lowercase(Locale.getDefault()) == "aprobado") binding.buttonReject else binding.buttonApprove

            otherButton.isVisible = false
            finalButton.isVisible = true
            finalButton.isEnabled = false
            finalButton.text = status.replaceFirstChar { it.uppercase() }
            centerButton(finalButton)
        }
    }

    private fun centerButton(button: View) {
        val params = button.layoutParams as LinearLayout.LayoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.gravity = Gravity.CENTER
        button.layoutParams = params
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
