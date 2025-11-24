package com.mycat.purrfectstore2.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.UploadService
import com.mycat.purrfectstore2.databinding.FragmentEditProductBinding
import com.mycat.purrfectstore2.model.ProductImage
import com.mycat.purrfectstore2.model.UpdateProductRequest
import com.mycat.purrfectstore2.ui.adapter.ImagePreviewAdapter
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!
    private val args: EditProductFragmentArgs by navArgs()
    private lateinit var productService: ProductService
    private lateinit var uploadService: UploadService

    private val imageItems = mutableListOf<Any>()
    private lateinit var imageAdapter: ImagePreviewAdapter

    private val selectImagesLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val startPosition = imageItems.size
            imageItems.addAll(uris)
            imageAdapter.notifyItemRangeInserted(startPosition, uris.size)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        productService = RetrofitClient.createProductService(requireContext())
        uploadService = RetrofitClient.createUploadService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadProductDetails()

        binding.buttonAddImages.setOnClickListener {
            selectImagesLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.buttonUpdateProduct.setOnClickListener {
            updateProduct()
        }
    }

    private fun setupRecyclerView() {
        imageAdapter = ImagePreviewAdapter(imageItems) { position ->
            imageItems.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
        }
        binding.recyclerViewImagePreview.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
    }

    private fun loadProductDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val product = productService.getProductId(args.productId)
                binding.editTextName.setText(product.name)
                binding.editTextDescription.setText(product.description)
                binding.editTextPrice.setText(product.price.toString())
                binding.editTextQuantity.setText(product.stock.toString())

                imageItems.addAll(product.images)
                imageAdapter.notifyDataSetChanged()
                binding.recyclerViewImagePreview.visibility = if (imageItems.isNotEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading product details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProduct() {
        val name = binding.editTextName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val price = binding.editTextPrice.text.toString().toDoubleOrNull()
        val stock = binding.editTextQuantity.text.toString().toIntOrNull()

        if (name.isEmpty() || description.isEmpty() || price == null || stock == null) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val newImageUris = imageItems.filterIsInstance<Uri>()
                val existingImages = imageItems.filterIsInstance<ProductImage>()
                val uploadedImages = mutableListOf<ProductImage>()

                for (uri in newImageUris) {
                    val imageResponseList = uploadImage(uri)
                    if (imageResponseList.isNotEmpty()) {
                        uploadedImages.add(imageResponseList.first())
                    }
                }

                val finalImages = existingImages + uploadedImages

                val request = UpdateProductRequest(name, description, price, stock, image = finalImages)

                productService.updateProduct(args.productId, request)

                Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating product: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): List<ProductImage> {
        val inputStream: InputStream = requireContext().contentResolver.openInputStream(uri)
            ?: throw Exception("Failed to open InputStream for URI: $uri")
        val imageBytes = inputStream.readBytes()
        inputStream.close()
        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("content", "product_image.jpg", requestBody)

        return uploadService.uploadImages(imagePart)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
