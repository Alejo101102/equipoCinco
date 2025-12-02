package com.univalle.equipocinco.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.equipocinco.data.remote.dto.ProductDto
import com.univalle.equipocinco.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _product = MutableStateFlow<ProductDto?>(null)
    val product: StateFlow<ProductDto?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProduct(productId: String) {
        if (productId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            _product.value = repository.getProductById(productId)
            _isLoading.value = false
        }
    }

    fun deleteCurrentProduct(onDeleted: () -> Unit, onError: () -> Unit) {
        val current = _product.value ?: return onError()
        viewModelScope.launch {
            try {
                val result = repository.deleteProduct(current.id)
                if (result.isSuccess) {
                    onDeleted()
                } else {
                    onError()
                }
            } catch (_: Exception) {
                onError()
            }
        }
    }
}