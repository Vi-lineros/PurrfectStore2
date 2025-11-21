package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.databinding.FragmentProductBinding
import com.mycat.purrfectstore2.model.Product
import com.mycat.purrfectstore2.ui.HomeActivity
import com.mycat.purrfectstore2.ui.adapter.ProductAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductFragment : Fragment() {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onProductClicked = { product ->
                if (product.stock > 0) {
                    val action = ProductFragmentDirections.actionNavProductToNavProductDetails2(product.id)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(context, "Este producto no tiene stock", Toast.LENGTH_SHORT).show()
                }
            },
            onProductLongClicked = {},
            onSelectionChanged = {}
        )
        binding.recyclerViewProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun setupSearch() {
        binding.searchViewProducts.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query)
                binding.searchViewProducts.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }

    private fun filter(query: String?) {
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()
        val filteredList = if (normalizedQuery.isBlank()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name.lowercase().contains(normalizedQuery)
            }
        }
        productAdapter.updateData(filteredList)
    }

    private fun loadProducts() {
        setLoadingState(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val productsFromApi = withContext(Dispatchers.IO) {
                    val service = RetrofitClient.createProductService(requireContext())
                    service.getProducts()
                }
                allProducts = productsFromApi
                productAdapter.updateData(allProducts)
            } catch (e: Exception) {
                Log.e("ProductFragment", "Error al cargar productos: ${e.message}", e)
                Toast.makeText(context, "Error al cargar productos", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        (activity as? HomeActivity)?.setDrawerLocked(isLoading) // Lock/Unlock Drawer
        binding.recyclerViewProducts.isVisible = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
