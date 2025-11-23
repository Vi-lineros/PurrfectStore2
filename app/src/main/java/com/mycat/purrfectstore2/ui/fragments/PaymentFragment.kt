package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.AuthService
import com.mycat.purrfectstore2.api.CartService
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.FragmentPaymentBinding
import com.mycat.purrfectstore2.model.CartProduct
import com.mycat.purrfectstore2.model.CreateCartRequest
import com.mycat.purrfectstore2.model.UpdateCartStatusRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartService: CartService
    private lateinit var authService: AuthService
    private lateinit var productService: ProductService
    private lateinit var tokenManager: TokenManager
    private val args: PaymentFragmentArgs by navArgs()
    private var totalAmount: Float = 0.0f
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        cartService = RetrofitClient.createCartService(requireContext())
        authService = RetrofitClient.createAuthService(requireContext(), requiresAuth = true)
        productService = RetrofitClient.createProductService(requireContext())
        tokenManager = TokenManager(requireContext())
        totalAmount = args.totalAmount
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBankOptions()
        setupBackButton()
        setupPayButton()
        setupInstallmentsListener()
        updateInstallmentValue()
        loadUserData()
        loadOrderSummary()
    }

    private fun loadOrderSummary() {
        lifecycleScope.launch {
            try {
                val cartId = tokenManager.getCartId()
                if (cartId == -1) return@launch

                val cart = cartService.getCart(cartId)
                val cartProducts = fetchProductDetailsForCart(cart.product_id ?: emptyList())

                // Build summary string
                val summaryText = StringBuilder()
                val itemsToShow = cartProducts.take(2)
                itemsToShow.forEach {
                    summaryText.append("• ${it.quantity}x ${it.product_details?.name}\n")
                }
                if (cartProducts.size > 2) {
                    summaryText.append("y ${cartProducts.size - 2} más...")
                }

                binding.textViewOrderDetails.text = summaryText.toString().trimEnd()
                binding.textViewOrderTotal.text = "Total del pedido: ${currencyFormat.format(totalAmount)}"

            } catch (e: Exception) {
                binding.textViewOrderDetails.text = "No se pudo cargar el resumen del pedido."
            }
        }
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

    private fun loadUserData(){
        lifecycleScope.launch {
            try {
                val user = authService.getMe()
                if (!user.shippingAddress.isNullOrBlank()) {
                    binding.textViewShippingAddress.text = user.shippingAddress
                    binding.textViewShippingAddress.visibility = View.VISIBLE
                    binding.textViewNoAddress.visibility = View.GONE
                    binding.buttonGoToProfile.text = "Cambiar"
                } else {
                    binding.textViewShippingAddress.visibility = View.GONE
                    binding.textViewNoAddress.visibility = View.VISIBLE
                    binding.buttonGoToProfile.text = "Configurar"
                }
                binding.buttonGoToProfile.setOnClickListener {
                    findNavController().navigate(R.id.profileFragment)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar la dirección.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBankOptions() {
        val banks = resources.getStringArray(R.array.bank_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, banks)
        (binding.textFieldBank.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupPayButton() {
        binding.buttonPayNow.setOnClickListener {
            if (validateFields()) {
                handlePayment()
            }
        }
    }

    private fun validateFields(): Boolean {
        if (binding.textFieldCardHolderName.editText?.text.isNullOrBlank()) {
            Toast.makeText(context, "Ingresa el nombre del titular", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.textFieldRut.editText?.text.isNullOrBlank()) {
            Toast.makeText(context, "Ingresa el RUT del titular", Toast.LENGTH_SHORT).show()
            return false
        }
        if ((binding.textFieldBank.editText as? AutoCompleteTextView)?.text.isNullOrBlank()) {
            Toast.makeText(context, "Selecciona un banco", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.textFieldCardNumber.editText?.text.isNullOrBlank()) {
            Toast.makeText(context, "Ingresa el número de tarjeta", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.textFieldExpiryDate.editText?.text.isNullOrBlank()) {
            Toast.makeText(context, "Ingresa la fecha de vencimiento", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.textFieldCVC.editText?.text.isNullOrBlank()) {
            Toast.makeText(context, "Ingresa el CVC", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun handlePayment() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val user = authService.getMe()
                // Essential check for shipping address
                if (user.shippingAddress.isNullOrBlank()) {
                    Toast.makeText(context, "¡Configura tu direccion de envio!", Toast.LENGTH_LONG).show()
                    setLoading(false)
                    return@launch
                }

                delay(1500) // Simulate payment processing
                val currentCartId = tokenManager.getCartId()
                if (currentCartId == -1) throw IllegalStateException("Carrito actual no válido")

                cartService.updateCartStatus(currentCartId, UpdateCartStatusRequest("pendiente"))

                val newCartRequest = CreateCartRequest(user_id = user.id)
                val newCart = cartService.createCart(newCartRequest)

                tokenManager.saveCartId(newCart.id)

                Toast.makeText(context, "¡Pago procesado con éxito!", Toast.LENGTH_LONG).show()

                findNavController().navigate(R.id.action_paymentFragment_to_myOrdersFragment)

            } catch (e: Exception) {
                Toast.makeText(context, "Error en el pago: ${e.message}", Toast.LENGTH_LONG).show()
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.buttonPayNow.isEnabled = !isLoading
        binding.buttonPayNow.text = if (isLoading) "Procesando..." else "Pagar"
    }

    private fun setupBackButton() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showCancelConfirmationDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun showCancelConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancelar Pago")
            .setMessage("¿Estás seguro de que quieres cancelar el pago?")
            .setPositiveButton("Aceptar") { _, _ ->
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupInstallmentsListener() {
        binding.radioGroupInstallments.setOnCheckedChangeListener { _, _ ->
            updateInstallmentValue()
        }
    }

    private fun updateInstallmentValue() {
        val checkedId = binding.radioGroupInstallments.checkedRadioButtonId
        val installments = when (checkedId) {
            R.id.radio_6_installments -> 6
            R.id.radio_12_installments -> 12
            R.id.radio_24_installments -> 24
            R.id.radio_32_installments -> 32
            else -> 1
        }

        val installmentValue = totalAmount / installments
        val formattedValue = currencyFormat.format(installmentValue.toDouble())
        
        binding.textViewInstallmentValue.text = "Valor de cuotas: $formattedValue"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
