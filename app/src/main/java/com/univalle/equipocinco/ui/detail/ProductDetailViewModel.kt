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

/**
 * ViewModel para la pantalla de detalle de un producto específico.
 * Gestiona la carga y eliminación de un producto individual.
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    /**
     * StateFlow privado que contiene el producto actualmente cargado.
     * Null si no hay producto cargado o si ocurrió un error.
     */
    private val _product = MutableStateFlow<ProductDto?>(null)

    /**
     * StateFlow público que expone el producto a la UI.
     * La UI observa este flow para actualizar los detalles mostrados.
     */
    val product: StateFlow<ProductDto?> = _product.asStateFlow()

    /**
     * StateFlow privado que indica si se está cargando el producto.
     */
    private val _isLoading = MutableStateFlow(false)

    /**
     * StateFlow público que expone el estado de carga a la UI.
     * Útil para mostrar shimmer effects o progress indicators.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Carga un producto específico desde Firestore.
     *
     * @param productId El ID único del producto en Firestore
     *
     * Proceso:
     * 1. Valida que el productId no esté vacío
     * 2. Activa el indicador de carga
     * 3. Consulta el repositorio para obtener el producto
     * 4. Actualiza el StateFlow con el producto obtenido (o null si no existe)
     * 5. Desactiva el indicador de carga
     *
     * Si productId está vacío, no hace nada (early return).
     */
    fun loadProduct(productId: String) {
        if (productId.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _product.value = repository.getProductById(productId)
            _isLoading.value = false
        }
    }

    /**
     * Elimina el producto actualmente cargado.
     *
     * @param onDeleted Callback que se ejecuta si la eliminación fue exitosa
     * @param onError Callback que se ejecuta si ocurrió un error
     *
     * Validaciones:
     * - Verifica que haya un producto cargado (current != null)
     * - Llama a onError si no hay producto cargado
     *
     * Proceso:
     * 1. Obtiene el producto actual del StateFlow
     * 2. Si no hay producto, ejecuta onError y termina
     * 3. Ejecuta la eliminación en el repositorio
     * 4. Si es exitoso, ejecuta el callback onDeleted (navegar atrás)
     * 5. Si falla, ejecuta el callback onError (mostrar mensaje)
     * 6. Captura excepciones y ejecuta onError si ocurren
     *
     * Uso típico:
     * ```
     * viewModel.deleteCurrentProduct(
     *     onDeleted = {
     *         // Navegar de regreso al home
     *         findNavController().navigateUp()
     *     },
     *     onError = {
     *         // Mostrar mensaje de error
     *         Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
     *     }
     * )
     * ```
     */
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
