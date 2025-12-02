package com.univalle.equipocinco.ui.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.univalle.equipocinco.data.remote.dto.ProductDto
import com.univalle.equipocinco.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProductDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockRepository: ProductRepository

    private lateinit var viewModel: ProductDetailViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ProductDetailViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProduct debe cargar producto correctamente`() = runTest {
        // Given
        val productId = "123"
        val expectedProduct = ProductDto(
            id = productId,
            code = 100,
            name = "Test Product",
            price = 50.0,
            quantity = 10
        )
        whenever(mockRepository.getProductById(productId))
            .thenReturn(expectedProduct)

        // When
        viewModel.loadProduct(productId)
        advanceUntilIdle()

        // Then
        assertEquals(expectedProduct, viewModel.product.value)
        assertFalse(viewModel.isLoading.value)
        verify(mockRepository).getProductById(productId)
    }

    @Test
    fun `loadProduct debe actualizar isLoading correctamente`() = runTest {
        // Given
        val productId = "123"
        val product = ProductDto("123", 100, "Test", 50.0, 10)
        whenever(mockRepository.getProductById(productId))
            .thenReturn(product)

        // When - verificar que loading se activa
        viewModel.loadProduct(productId)

        // Then - loading debe ser true al inicio
        assertTrue(viewModel.isLoading.value)

        // When - esperar a que termine
        advanceUntilIdle()

        // Then - loading debe ser false al final
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadProduct con id vacio no debe cargar producto`() = runTest {
        // Given
        val emptyId = ""

        // When
        viewModel.loadProduct(emptyId)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.product.value)
        assertFalse(viewModel.isLoading.value)
        verify(mockRepository, never()).getProductById(anyString())
    }

    @Test
    fun `loadProduct cuando producto no existe debe retornar null`() = runTest {
        // Given
        val productId = "999"
        whenever(mockRepository.getProductById(productId))
            .thenReturn(null)

        // When
        viewModel.loadProduct(productId)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.product.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `deleteCurrentProduct exitoso debe llamar onDeleted`() = runTest {
        // Given
        val productId = "123"
        val product = ProductDto(productId, 100, "Test", 50.0, 10)
        var deletedCalled = false
        var errorCalled = false

        whenever(mockRepository.getProductById(productId))
            .thenReturn(product)
        whenever(mockRepository.deleteProduct(productId))
            .thenReturn(Result.success(Unit))

        viewModel.loadProduct(productId)
        advanceUntilIdle()

        // When
        viewModel.deleteCurrentProduct(
            onDeleted = { deletedCalled = true },
            onError = { errorCalled = true }
        )
        advanceUntilIdle()

        // Then
        assertTrue(deletedCalled)
        assertFalse(errorCalled)
        verify(mockRepository).deleteProduct(productId)
    }

    @Test
    fun `deleteCurrentProduct fallido debe llamar onError`() = runTest {
        // Given
        val productId = "123"
        val product = ProductDto(productId, 100, "Test", 50.0, 10)
        var deletedCalled = false
        var errorCalled = false

        whenever(mockRepository.getProductById(productId))
            .thenReturn(product)
        whenever(mockRepository.deleteProduct(productId))
            .thenReturn(Result.failure(Exception("Error al eliminar")))

        viewModel.loadProduct(productId)
        advanceUntilIdle()

        // When
        viewModel.deleteCurrentProduct(
            onDeleted = { deletedCalled = true },
            onError = { errorCalled = true }
        )
        advanceUntilIdle()

        // Then
        assertFalse(deletedCalled)
        assertTrue(errorCalled)
        verify(mockRepository).deleteProduct(productId)
    }

    @Test
    fun `deleteCurrentProduct sin producto cargado debe llamar onError`() = runTest {
        // Given
        var deletedCalled = false
        var errorCalled = false

        // When
        viewModel.deleteCurrentProduct(
            onDeleted = { deletedCalled = true },
            onError = { errorCalled = true }
        )
        advanceUntilIdle()

        // Then
        assertFalse(deletedCalled)
        assertTrue(errorCalled)
        verify(mockRepository, never()).deleteProduct(anyString())
    }

    @Test
    fun `deleteCurrentProduct con excepcion debe llamar onError`() = runTest {
        // Given
        val productId = "123"
        val product = ProductDto(productId, 100, "Test", 50.0, 10)
        var deletedCalled = false
        var errorCalled = false

        whenever(mockRepository.getProductById(productId))
            .thenReturn(product)
        whenever(mockRepository.deleteProduct(productId))
            .thenThrow(RuntimeException("Network error"))

        viewModel.loadProduct(productId)
        advanceUntilIdle()

        // When
        viewModel.deleteCurrentProduct(
            onDeleted = { deletedCalled = true },
            onError = { errorCalled = true }
        )
        advanceUntilIdle()

        // Then
        assertFalse(deletedCalled)
        assertTrue(errorCalled)
    }

    @Test
    fun `cargar producto multiple veces debe actualizar correctamente`() = runTest {
        // Given
        val productId1 = "123"
        val productId2 = "456"
        val product1 = ProductDto(productId1, 100, "Product 1", 50.0, 10)
        val product2 = ProductDto(productId2, 200, "Product 2", 75.0, 5)

        whenever(mockRepository.getProductById(productId1))
            .thenReturn(product1)
        whenever(mockRepository.getProductById(productId2))
            .thenReturn(product2)

        // When
        viewModel.loadProduct(productId1)
        advanceUntilIdle()
        val firstProduct = viewModel.product.value

        viewModel.loadProduct(productId2)
        advanceUntilIdle()
        val secondProduct = viewModel.product.value

        // Then
        assertEquals(product1, firstProduct)
        assertEquals(product2, secondProduct)
        assertNotEquals(firstProduct, secondProduct)
    }

    @Test
    fun `product inicial debe ser null`() {
        // Then
        assertNull(viewModel.product.value)
    }

    @Test
    fun `isLoading inicial debe ser false`() {
        // Then
        assertFalse(viewModel.isLoading.value)
    }
}