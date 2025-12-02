package com.univalle.equipocinco.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.equipocinco.data.remote.dto.ProductDto
import com.univalle.equipocinco.data.remote.firebase.FirebaseAuthService
import com.univalle.equipocinco.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val authService: FirebaseAuthService
) : ViewModel() {

    private val _products = MutableStateFlow<List<ProductDto>>(emptyList())
    val products: StateFlow<List<ProductDto>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProducts() {
        // ✅ Verificar autenticación ANTES de cargar
        val currentUser = authService.getCurrentUser()
        if (currentUser == null) {
            _products.value = emptyList()
            _isLoading.value = false
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.getAllProducts().collect { productList ->
                    _products.value = productList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // ✅ Log para debugging
                android.util.Log.e("HomeViewModel", "Error loading products", e)
                _products.value = emptyList()
                _isLoading.value = false
            }
        }
    }
}