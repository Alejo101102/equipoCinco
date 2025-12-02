package com.univalle.equipocinco.ui.home

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.univalle.equipocinco.data.remote.dto.ProductDto
import com.univalle.equipocinco.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
class ProductViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockRepository: ProductRepository

    @Mock
    private lateinit var mockContext: Context

    private lateinit var viewModel: ProductViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `productsFlow debe emitir lista de productos`() = runTest {
        // Given
        val testProducts = listOf(
            ProductDto("1", 100, "Producto 1", 50.0, 10),
            ProductDto("2", 200, "Producto 2", 100.0, 5)
        )
        whenever(mockRepository.getAllProducts())
            .thenReturn(flowOf(testProducts))

        // When
        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // Then
        assertEquals(testProducts, viewModel.productsFlow.value)
    }

    @Test
    fun `productsFlow debe emitir lista vacia cuando no hay productos`() = runTest {
        // Given
        whenever(mockRepository.getAllProducts())
            .thenReturn(flowOf(emptyList()))

        // When
        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.productsFlow.value.isEmpty())
    }

    @Test
    fun `totalFlow debe emitir valor total del inventario`() = runTest {
        // Given
        val expectedTotal = 1500.0
        whenever(mockRepository.getTotalInventoryValue())
            .thenReturn(flowOf(expectedTotal))

        // When
        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // Then
        assertEquals(expectedTotal, viewModel.totalFlow.value, 0.001)
    }

    @Test
    fun `totalFlow debe emitir cero cuando inventario esta vacio`() = runTest {
        // Given
        whenever(mockRepository.getTotalInventoryValue())
            .thenReturn(flowOf(0.0))

        // When
        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // Then
        assertEquals(0.0, viewModel.totalFlow.value, 0.001)
    }

    @Test
    fun `getProductById debe retornar producto correcto`() = runTest {
        // Given
        val productId = "123"
        val testProducts = listOf(
            ProductDto("123", 100, "Test Product", 50.0, 10),
            ProductDto("456", 200, "Other Product", 75.0, 5)
        )
        whenever(mockRepository.getAllProducts())
            .thenReturn(flowOf(testProducts))

        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // When
        val result = viewModel.getProductById(productId).first()

        // Then
        assertNotNull(result)
        assertEquals(productId, result?.id)
        assertEquals("Test Product", result?.name)
    }

    @Test
    fun `getProductById debe retornar null cuando producto no existe`() = runTest {
        // Given
        val productId = "999"
        val testProducts = listOf(
            ProductDto("123", 100, "Test Product", 50.0, 10)
        )
        whenever(mockRepository.getAllProducts())
            .thenReturn(flowOf(testProducts))

        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // When
        val result = viewModel.getProductById(productId).first()

        // Then
        assertNull(result)
    }

    @Test
    fun `addProduct debe llamar a repository insertProduct`() = runTest {
        // Given
        val newProduct = ProductDto("", 300, "New Product", 25.0, 15)
        whenever(mockRepository.getAllProducts())
            .thenReturn(flowOf(emptyList()))
        whenever(mockRepository.getTotalInventoryValue())
            .thenReturn(flowOf(0.0))
        whenever(mockRepository.insertProduct(newProduct))
            .thenReturn(Result.success("generated-id"))

        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // When
        viewModel.addProduct(newProduct)
        advanceUntilIdle()

        // Then
        verify(mockRepository).insertProduct(newProduct)
    }

    @Test
    fun `updateProduct debe llamar a repository updateProduct`() = runTest {
        // Given
        val updatedProduct = ProductDto("123", 100, "Updated", 60.0, 12)
        whenever(mockRepository.getAllProducts())
            .thenReturn(flowOf(emptyList()))
        whenever(mockRepository.getTotalInventoryValue())
            .thenReturn(flowOf(0.0))
        whenever(mockRepository.updateProduct(updatedProduct))
            .thenReturn(Result.success(Unit))

        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // When
        viewModel.updateProduct(updatedProduct)
        advanceUntilIdle()

        // Then
        verify(mockRepository).updateProduct(updatedProduct)
    }

    @Test
    fun `deleteProduct debe llamar a repository deleteProduct`() = runTest {
        // Given
        val productId = "123"
        whenever(mockRepository.getAllProducts())
            .thenReturn(flowOf(emptyList()))
        whenever(mockRepository.getTotalInventoryValue())
            .thenReturn(flowOf(0.0))
        whenever(mockRepository.deleteProduct(productId))
            .thenReturn(Result.success(Unit))

        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // When
        viewModel.deleteProduct(productId)
        advanceUntilIdle()

        // Then
        verify(mockRepository).deleteProduct(productId)
    }

    @Test
    fun `productsFlow debe actualizarse cuando se agregan productos`() = runTest {
        // Given
        val initialProducts = listOf(
            ProductDto("1", 100, "Product 1", 50.0, 10)
        )
        val updatedProducts = listOf(
            ProductDto("1", 100, "Product 1", 50.0, 10),
            ProductDto("2", 200, "Product 2", 75.0, 5)
        )

        val productsFlow = MutableStateFlow(initialProducts)
        whenever(mockRepository.getAllProducts())
            .thenReturn(productsFlow)

        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // When
        productsFlow.value = updatedProducts
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.productsFlow.value.size)
        assertEquals("Product 2", viewModel.productsFlow.value[1].name)
    }

    @Test
    fun `totalFlow debe actualizarse cuando cambia el inventario`() = runTest {
        // Given
        val totalFlow = MutableStateFlow(1000.0)
        whenever(mockRepository.getTotalInventoryValue())
            .thenReturn(totalFlow)

        viewModel = ProductViewModel(mockRepository, mockContext)
        advanceUntilIdle()

        // When
        totalFlow.value = 2000.0
        advanceUntilIdle()

        // Then
        assertEquals(2000.0, viewModel.totalFlow.value, 0.001)
    }
}