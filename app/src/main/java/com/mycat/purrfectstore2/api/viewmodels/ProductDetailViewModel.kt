package com.mycat.purrfectstore2.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycat.purrfectstore2.api.ProductService
import com.mycat.purrfectstore2.model.Product
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {
    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    fun fetchProductDetails(productId: Int, productService: ProductService) {
        if (_product.value != null) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = productService.getProductId(productId)
                _product.postValue(response)
            } catch (e: Exception) {
                _error.postValue("Error al cargar detalles: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
