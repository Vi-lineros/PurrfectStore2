package com.mycat.purrfectstore2.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mycat.purrfectstore2.R
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.api.RetrofitClient
import com.mycat.purrfectstore2.databinding.FragmentProductsAdminBinding
import com.mycat.purrfectstore2.model.Product
import com.mycat.purrfectstore2.ui.HomeActivity
import com.mycat.purrfectstore2.ui.adapter.ProductAdapter
import kotlinx.coroutines.launch

class ProductsAdminFragment : Fragment() {

    private var _binding: FragmentProductsAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var productService: ProductService
    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsAdminBinding.inflate(inflater, container, false)
        productService = RetrofitClient.createProductService(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        loadProducts()

        binding.fabAddProduct.setOnClickListener {
            if (productAdapter.isSelectionMode) {
                showDeleteConfirmationDialog()
            } else {
                findNavController().navigate(R.id.action_productsAdminFragment_to_addProductFragment)
            }
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onProductClicked = { product ->
                if (productAdapter.isSelectionMode) {
                    productAdapter.toggleSelection(product.id)
                } else {
                    val action =
                        ProductsAdminFragmentDirections.actionProductsAdminFragmentToEditProductFragment(
                            product.id
                        )
                    findNavController().navigate(action)
                }
            },
            onProductLongClicked = { product ->
                if (!productAdapter.isSelectionMode) {
                    enterSelectionMode(product.id)
                }
            },
            onSelectionChanged = { count ->
                if (productAdapter.isSelectionMode) {
                    if (count == 0) {
                        exitSelectionMode()
                    } else {
                        updateToolbarTitle(count)
                    }
                }
            }
        )

        binding.recyclerViewProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun enterSelectionMode(productId: Int) {
        productAdapter.isSelectionMode = true
        productAdapter.toggleSelection(productId)
        binding.fabAddProduct.setImageResource(R.drawable.ic_delete)
        updateToolbarTitle(1)
        (activity as? HomeActivity)?.showCancelButton(true)
    }

    fun exitSelectionMode() {
        productAdapter.clearSelection()
        binding.fabAddProduct.setImageResource(R.drawable.ic_add)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.products_admin_title)
        (activity as? HomeActivity)?.showCancelButton(false)
    }

    private fun updateToolbarTitle(count: Int) {
        val productText = if (count == 1) "producto" else "productos"
        (activity as? AppCompatActivity)?.supportActionBar?.title = "$count $productText seleccionado(s)"
    }

    private fun showDeleteConfirmationDialog() {
        val selectedCount = productAdapter.selectedItems.size
        val productWord = if (selectedCount == 1) "producto" else "productos"
        val message = "¿Confirmar eliminación de  $selectedCount $productWord?"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage(message)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Aceptar") { _, _ ->
                deleteSelectedProducts()
            }
            .show()
    }

    private fun deleteSelectedProducts() {
        val selectedCount = productAdapter.selectedItems.size
        val productWord = if (selectedCount == 1) "Producto" else "Productos"
        val deleteWord = if (selectedCount == 1) "eliminado" else "eliminados"
        val itemsToDelete = productAdapter.selectedItems.toList()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                itemsToDelete.forEach { productId ->
                    productService.deleteProduct(productId)
                }
                Toast.makeText(requireContext(), "$productWord $deleteWord con éxito!", Toast.LENGTH_SHORT).show()
                exitSelectionMode()
                loadProducts()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        binding.searchViewProducts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val productsFromApi = productService.getProducts()
                allProducts = productsFromApi
                productAdapter.updateData(allProducts)
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading products: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? HomeActivity)?.showCancelButton(false)
        _binding = null
    }
}
