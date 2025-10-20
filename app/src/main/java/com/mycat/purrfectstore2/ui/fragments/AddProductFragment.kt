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
import androidx.recyclerview.widget.LinearLayoutManager
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.api.UploadService
import com.mycat.purrfectstore2.databinding.FragmentNavAddProductBinding
import com.mycat.purrfectstore2.model.CreateProductRequest
import com.mycat.purrfectstore2.model.ProductImage
import com.mycat.purrfectstore2.ui.adapter.ImagePreviewAdapter
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class AddProductFragment : Fragment() {
    private var _binding: FragmentNavAddProductBinding? = null
    private val binding get() = _binding!!
    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImagePreviewAdapter
    private lateinit var uploadService: UploadService
    private lateinit var productService: ProductService
    private val selectImagesLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val startPosition = selectedImageUris.size
            selectedImageUris.addAll(uris)
            imageAdapter.notifyItemRangeInserted(startPosition, uris.size)
            if (binding.recyclerViewImagePreview.visibility == View.GONE) {
                binding.recyclerViewImagePreview.visibility = View.VISIBLE
            }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNavAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadService = RetrofitClient.createUploadService(requireContext())
        productService = RetrofitClient.createProductService(requireContext())
        imageAdapter = ImagePreviewAdapter(selectedImageUris)
        binding.recyclerViewImagePreview.adapter = imageAdapter
        binding.recyclerViewImagePreview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.buttonAddImages.setOnClickListener {
            selectImagesLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        binding.buttonAddProduct.setOnClickListener {
            createProductFlow()
        }
    }
    private fun createProductFlow() {
        val name = binding.editTextName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val priceStr = binding.editTextPrice.text.toString().trim()
        val stockStr = binding.editTextQuantity.text.toString().trim()
        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre, Precio y Stock son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, selecciona al menos una imagen", Toast.LENGTH_SHORT).show()
            return
        }
        binding.buttonAddProduct.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val uploadedImageResponses = mutableListOf<ProductImage>()
                for (uri in selectedImageUris) {
                    val imageResponseList = uploadImage(uri)
                    if (imageResponseList.isNotEmpty()) {
                        uploadedImageResponses.add(imageResponseList.first())
                    }
                }
                if (uploadedImageResponses.isEmpty()) {
                    throw Exception("No se pudo subir ninguna imagen al servidor")
                }
                val createRequest = CreateProductRequest(
                    name = name,
                    description = description,
                    price = priceStr.toDoubleOrNull(),
                    stock = stockStr.toIntOrNull(),
                    image = uploadedImageResponses
                )
                val productResponse = productService.createProduct(createRequest)
                val createdProductName = productResponse.name
                Toast.makeText(requireContext(), "¡Producto '$createdProductName' creado con éxito!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.buttonAddProduct.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    private suspend fun uploadImage(uri: Uri): List<ProductImage> {
        val inputStream: InputStream = requireContext().contentResolver.openInputStream(uri)
            ?: throw Exception("Error")
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
