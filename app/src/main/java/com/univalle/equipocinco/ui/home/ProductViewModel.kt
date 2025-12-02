package com.univalle.equipocinco.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.equipocinco.data.remote.dto.ProductDto
import com.univalle.equipocinco.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val productsFlow: StateFlow<List<ProductDto>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalFlow: StateFlow<Double> = repository.getTotalInventoryValue()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun getProductById(id: String): Flow<ProductDto?> {
        return repository.getAllProducts().map { products ->
            products.find { it.id == id }
        }
    }

    fun addProduct(product: ProductDto) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertProduct(product)
        }
    }

    fun updateProduct(product: ProductDto) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProduct(productId)
        }
    }
}