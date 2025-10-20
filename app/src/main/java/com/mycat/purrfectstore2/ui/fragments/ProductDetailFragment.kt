package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.databinding.FragmentProductDetailsBinding
import com.mycat.purrfectstore2.model.Product
import com.mycat.purrfectstore2.ui.adapter.ImageSliderAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailFragment : Fragment() {
    private val args: ProductDetailFragmentArgs by navArgs()
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!
    private var currentProduct: Product? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
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
                    val service = RetrofitClient.createProductService(requireContext())
                    service.getProductId(productId)
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
        setupAddToCartButton()
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
        binding.buttonIncrease.setOnClickListener {
            if (quantity < maxStock) {
                quantity++
                binding.textViewQuantity.text = quantity.toString()
            }
        }
        binding.buttonDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.textViewQuantity.text = quantity.toString()
            }
        }
    }
    private fun setupAddToCartButton() {
        binding.buttonAddToCart.setOnClickListener {
            val quantity = binding.textViewQuantity.text.toString().toIntOrNull() ?: 1
            if (currentProduct != null) {
                Toast.makeText(requireContext(), "$quantity ${currentProduct!!.name}(s) añadido(s)", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
