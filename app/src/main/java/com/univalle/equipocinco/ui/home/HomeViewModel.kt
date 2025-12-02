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

/**
 * ViewModel para la pantalla principal (Home) del inventario.
 * Gestiona la carga y visualización de la lista de productos.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val authService: FirebaseAuthService
) : ViewModel() {

    /**
     * StateFlow privado que contiene la lista mutable de productos.
     */
    private val _products = MutableStateFlow<List<ProductDto>>(emptyList())

    /**
     * StateFlow público de solo lectura que expone la lista de productos a la UI.
     * Los observers recibirán actualizaciones cuando la lista cambie.
     */
    val products: StateFlow<List<ProductDto>> = _products.asStateFlow()

    /**
     * StateFlow privado que indica si se están cargando los productos.
     */
    private val _isLoading = MutableStateFlow(false)

    /**
     * StateFlow público que indica el estado de carga a la UI.
     * Útil para mostrar/ocultar indicadores de progreso.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Carga la lista de productos desde el repositorio.
     *
     * Proceso:
     * 1. Verifica que el usuario esté autenticado
     * 2. Si no está autenticado, limpia la lista y termina
     * 3. Si está autenticado, activa el indicador de carga
     * 4. Obtiene los productos del repositorio como Flow
     * 5. Actualiza el StateFlow de productos con cada emisión
     * 6. Desactiva el indicador de carga al completar
     * 7. Maneja errores y garantiza que el loading se desactive
     */
    fun loadProducts() {
        // Verificar autenticación ANTES de cargar
        val currentUser = authService.getCurrentUser()
        if (currentUser == null) {
            _products.value = emptyList()
            _isLoading.value = false
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Recolecta el Flow de productos y actualiza el StateFlow
                repository.getAllProducts().collect { productList ->
                    _products.value = productList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // Log para debugging en caso de error
                android.util.Log.e("HomeViewModel", "Error loading products", e)
                _products.value = emptyList()
                _isLoading.value = false
            }
        }
    }
}