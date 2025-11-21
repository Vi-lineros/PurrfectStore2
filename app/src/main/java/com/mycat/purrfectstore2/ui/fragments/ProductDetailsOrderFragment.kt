package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.databinding.FragmentProductDetailsOrderBinding
import com.mycat.purrfectstore2.model.ProductImage
import com.mycat.purrfectstore2.ui.adapter.ImageSliderAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ProductDetailsOrderFragment : Fragment() {

    private var _binding: FragmentProductDetailsOrderBinding? = null
    private val binding get() = _binding!!

    private val args: ProductDetailsOrderFragmentArgs by navArgs()
    private lateinit var productService: ProductService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsOrderBinding.inflate(inflater, container, false)
        productService = RetrofitClient.createProductService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProductDetails()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.scrollView.isVisible = !isLoading
        binding.cardViewStockInCart.isVisible = !isLoading
        binding.cardViewTotalForProduct.isVisible = !isLoading
    }

    private fun loadProductDetails() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                delay(1500) // Added delay
                val product = productService.getProductId(args.productId)
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

                binding.textViewDetailProductName.text = product.name
                binding.textViewDetailProductDescription.text = product.description
                binding.textViewDetailProductStock.text = "Stock disponible: ${product.stock}"
                binding.textViewDetailProductPrice.text = "${currencyFormat.format(product.price)} /un"

                binding.textViewStockInCart.text = "Cantidad en el pedido: ${args.quantity}"
                val totalPrice = product.price * args.quantity
                binding.textViewTotalForProduct.text = "Total en el pedido: ${currencyFormat.format(totalPrice)}"

                setupImageCarousel(product.images)

            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar los detalles del producto: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setupImageCarousel(imageList: List<ProductImage>) {
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
                prevButton.visibility = if (position == 0) View.GONE else View.VISIBLE
                nextButton.visibility = if (position == imageCount - 1) View.GONE else View.VISIBLE
            }
        })

        prevButton.setOnClickListener { viewPager.currentItem -= 1 }
        nextButton.setOnClickListener { viewPager.currentItem += 1 }

        // Set initial visibility
        prevButton.visibility = View.GONE
        nextButton.visibility = if (imageCount > 1) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
