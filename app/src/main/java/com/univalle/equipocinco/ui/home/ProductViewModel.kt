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

/**
 * ViewModel compartido para operaciones CRUD de productos.
 * Utilizado por AddProductFragment y EditProductFragment.
 * Proporciona acceso reactivo a la lista de productos y valor total del inventario.
 */
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    /**
     * StateFlow que mantiene la lista completa de productos del inventario.
     *
     * - Se convierte automáticamente de Flow a StateFlow usando stateIn()
     * - SharingStarted.Lazily: comienza a recolectar cuando hay al menos un suscriptor
     * - Valor inicial: lista vacía
     * - Se actualiza automáticamente cuando cambia la base de datos
     */
    val productsFlow: StateFlow<List<ProductDto>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * StateFlow que mantiene el valor total del inventario.
     *
     * - Calcula la suma de (precio × cantidad) de todos los productos
     * - Se actualiza automáticamente cuando cambian los productos
     * - Valor inicial: 0.0
     */
    val totalFlow: StateFlow<Double> = repository.getTotalInventoryValue()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    /**
     * Busca un producto específico por su ID.
     *
     * @param id El identificador único del producto en Firestore
     * @return Flow que emite el producto encontrado o null si no existe
     *
     * Uso típico:
     * ```
     * viewModel.getProductById("123").collect { product ->
     *     if (product != null) {
     *         // Mostrar producto
     *     }
     * }
     * ```
     */
    fun getProductById(id: String): Flow<ProductDto?> {
        return repository.getAllProducts().map { products ->
            products.find { it.id == id }
        }
    }

    /**
     * Agrega un nuevo producto al inventario.
     *
     * @param product El producto a agregar (el ID será generado por Firestore)
     *
     * - Ejecuta en Dispatchers.IO para operaciones de red
     * - El resultado se propaga automáticamente a través de productsFlow
     * - Los errores se manejan en el repositorio
     */
    fun addProduct(product: ProductDto) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertProduct(product)
        }
    }

    /**
     * Actualiza un producto existente en el inventario.
     *
     * @param product El producto con los datos actualizados (debe incluir ID válido)
     *
     * - Ejecuta en Dispatchers.IO para operaciones de red
     * - Actualiza todos los campos del producto en Firestore
     * - Los cambios se reflejan automáticamente en productsFlow
     */
    fun updateProduct(product: ProductDto) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProduct(product)
        }
    }

    /**
     * Elimina un producto del inventario.
     *
     * @param productId El ID del producto a eliminar
     *
     * - Ejecuta en Dispatchers.IO para operaciones de red
     * - Elimina el documento completo de Firestore
     * - La eliminación se refleja automáticamente en productsFlow
     * - El totalFlow se recalcula automáticamente
     */
    fun deleteProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProduct(productId)
        }
    }
}