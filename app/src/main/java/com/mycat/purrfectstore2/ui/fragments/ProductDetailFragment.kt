package com.mycat.purrfectstore2.ui.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.mycat.purrfectstore2.api.CartService
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.TokenManager
import com.mycat.purrfectstore2.databinding.FragmentProductDetailsBinding
import com.mycat.purrfectstore2.model.CartProduct
import com.mycat.purrfectstore2.model.Product
import com.mycat.purrfectstore2.model.UpdateCartProductsRequest
import com.mycat.purrfectstore2.ui.adapter.ImageSliderAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailFragment : Fragment() {
    private val args: ProductDetailFragmentArgs by navArgs()
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartService: CartService
    private lateinit var productService: ProductService
    private lateinit var tokenManager: TokenManager
    private var currentProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        cartService = RetrofitClient.createCartService(requireContext())
        productService = RetrofitClient.createProductService(requireContext())
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = args.productId
        if (productId != -1) {
            loadProductDetails(productId)
        } else {
            Toast.makeText(context, "Error: ID de producto no válido.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadProductDetails(productId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val product = withContext(Dispatchers.IO) {
                    productService.getProductId(productId)
                }
                currentProduct = product
                updateUI(product)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar el producto: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(product: Product) {
        binding.textViewDetailProductName.text = product.name
        binding.textViewDetailProductDescription.text = product.description
        binding.textViewDetailProductStock.text = "Stock: ${product.stock}"
        setupImageCarousel(product)
        setupQuantityButtons(product.stock)
        setupAddToCartButton(product.id)
    }

    private suspend fun fetchProductDetailsForList(products: List<CartProduct>): List<CartProduct> = coroutineScope {
        products.map { cartProduct ->
            async {
                if (cartProduct.product_details == null) {
                    try {
                        cartProduct.product_details = productService.getProductId(cartProduct.product_id)
                    } catch (e: Exception) {
                        // Could not fetch details, it won't be part of total calculation
                    }
                }
                cartProduct
            }
        }.awaitAll()
    }

    private fun setupAddToCartButton(productId: Int) {
        binding.buttonAddToCart.setOnClickListener { 
            binding.buttonAddToCart.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    delay(500) // Add a delay to prevent rate limiting
                    val cartId = tokenManager.getCartId()
                    val stock = currentProduct?.stock ?: 0
                    if (cartId == -1) throw IllegalStateException("Sesión o carrito no válidos.")

                    val currentCart = cartService.getCart(cartId)
                    val updatedProductList = currentCart.product_id?.toMutableList() ?: mutableListOf()
                    val quantityToAdd = binding.textViewQuantity.text.toString().toInt()

                    val existingProduct = updatedProductList.find { it.product_id == productId }
                    val currentQuantityInCart = existingProduct?.quantity ?: 0

                    if (currentQuantityInCart + quantityToAdd > stock) {
                        Toast.makeText(requireContext(), "No hay suficiente stock", Toast.LENGTH_SHORT).show()
                    } else {
                        if (existingProduct != null) {
                            existingProduct.quantity += quantityToAdd
                        } else {
                            val newCartProduct = CartProduct(
                                product_id = productId, 
                                quantity = quantityToAdd,
                                product_details = currentProduct
                            )
                            updatedProductList.add(newCartProduct)
                        }

                        val populatedList = fetchProductDetailsForList(updatedProductList)
                        val newTotal = populatedList.sumOf { (it.product_details?.price ?: 0.0) * it.quantity }

                        val cleanProductList = populatedList.map { 
                            CartProduct(product_id = it.product_id, quantity = it.quantity)
                        }

                        val updateRequest = UpdateCartProductsRequest(products = cleanProductList, total = newTotal)
                        cartService.updateCart(cartId, updateRequest)

                        Toast.makeText(requireContext(), "Producto añadido al carrito", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al actualizar el carrito: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    binding.buttonAddToCart.isEnabled = true
                }
            }
        }
    }

    private fun setupImageCarousel(product: Product) {
        val imageList = product.images
        if (imageList.isNotEmpty()) {
            binding.cardViewImages.visibility = View.VISIBLE
            val imageAdapter = ImageSliderAdapter(imageList)
            binding.viewPagerProductImages.adapter = imageAdapter
            setupCarouselButtons(imageList.size)
        } else {
            binding.cardViewImages.visibility = View.GONE
        }
    }

    private fun setupCarouselButtons(imageCount: Int) {
        val viewPager = binding.viewPagerProductImages
        val prevButton = binding.buttonPreviousImage
        val nextButton = binding.buttonNextImage
        if (imageCount <= 1) {
            prevButton.visibility = View.GONE
            nextButton.visibility = View.GONE
            return
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                prevButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                nextButton.visibility = if (position == imageCount - 1) View.INVISIBLE else View.VISIBLE
            }
        })
        prevButton.setOnClickListener { viewPager.currentItem -= 1 }
        nextButton.setOnClickListener { viewPager.currentItem += 1 }
        prevButton.visibility = View.INVISIBLE
        nextButton.visibility = View.VISIBLE
    }

    private fun setupQuantityButtons(maxStock: Int) {
        var quantity = 1
        binding.textViewQuantity.text = quantity.toString()

        val blackColor = ContextCompat.getColor(requireContext(), android.R.color.black)
        binding.buttonDecrease.setColorFilter(blackColor, PorterDuff.Mode.SRC_ATOP)
        binding.buttonIncrease.setColorFilter(blackColor, PorterDuff.Mode.SRC_ATOP)

        binding.buttonIncrease.setOnClickListener {
            if (quantity < maxStock) {
                quantity++
                binding.textViewQuantity.text = quantity.toString()
            } else {
                Toast.makeText(requireContext(), "No hay más stock disponible", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.textViewQuantity.text = quantity.toString()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
