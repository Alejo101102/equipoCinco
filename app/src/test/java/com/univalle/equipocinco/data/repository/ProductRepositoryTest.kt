package com.univalle.equipocinco.data.repository


import com.univalle.equipocinco.data.remote.dto.ProductDto
import com.univalle.equipocinco.data.remote.firebase.FirestoreService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProductRepositoryTest {

    @Mock
    private lateinit var mockFirestoreService: FirestoreService

    private lateinit var repository: ProductRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = ProductRepository(mockFirestoreService)
    }

    @Test
    fun `getAllProducts debe retornar flow de productos`() = runTest {
        // Given
        val testProducts = listOf(
            ProductDto("1", 100, "Producto 1", 50.0, 10),
            ProductDto("2", 200, "Producto 2", 100.0, 5)
        )
        whenever(mockFirestoreService.getAllProducts())
            .thenReturn(flowOf(testProducts))

        // When
        val result = repository.getAllProducts().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Producto 1", result[0].name)
        assertEquals("Producto 2", result[1].name)
        verify(mockFirestoreService).getAllProducts()
    }

    @Test
    fun `getAllProducts debe retornar lista vacia cuando no hay productos`() = runTest {
        // Given
        whenever(mockFirestoreService.getAllProducts())
            .thenReturn(flowOf(emptyList()))

        // When
        val result = repository.getAllProducts().first()

        // Then
        assertTrue(result.isEmpty())
        verify(mockFirestoreService).getAllProducts()
    }

    @Test
    fun `getProductById debe retornar producto cuando existe`() = runTest {
        // Given
        val productId = "123"
        val expectedProduct = ProductDto(productId, 100, "Test", 50.0, 10)
        whenever(mockFirestoreService.getProductById(productId))
            .thenReturn(expectedProduct)

        // When
        val result = repository.getProductById(productId)

        // Then
        assertNotNull(result)
        assertEquals(productId, result?.id)
        assertEquals("Test", result?.name)
        verify(mockFirestoreService).getProductById(productId)
    }

    @Test
    fun `getProductById debe retornar null cuando producto no existe`() = runTest {
        // Given
        val productId = "999"
        whenever(mockFirestoreService.getProductById(productId))
            .thenReturn(null)

        // When
        val result = repository.getProductById(productId)

        // Then
        assertNull(result)
        verify(mockFirestoreService).getProductById(productId)
    }

    @Test
    fun `insertProduct debe retornar success con id generado`() = runTest {
        // Given
        val product = ProductDto("", 100, "Nuevo", 75.0, 20)
        val generatedId = "generated-123"
        whenever(mockFirestoreService.insertProduct(product))
            .thenReturn(Result.success(generatedId))

        // When
        val result = repository.insertProduct(product)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(generatedId, result.getOrNull())
        verify(mockFirestoreService).insertProduct(product)
    }

    @Test
    fun `insertProduct debe retornar failure cuando falla`() = runTest {
        // Given
        val product = ProductDto("", 100, "Nuevo", 75.0, 20)
        val exception = Exception("Error de red")
        whenever(mockFirestoreService.insertProduct(product))
            .thenReturn(Result.failure(exception))

        // When
        val result = repository.insertProduct(product)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `updateProduct debe retornar success cuando actualiza correctamente`() = runTest {
        // Given
        val product = ProductDto("1", 100, "Actualizado", 80.0, 15)
        whenever(mockFirestoreService.updateProduct(product))
            .thenReturn(Result.success(Unit))

        // When
        val result = repository.updateProduct(product)

        // Then
        assertTrue(result.isSuccess)
        verify(mockFirestoreService).updateProduct(product)
    }

    @Test
    fun `deleteProduct debe retornar success cuando elimina correctamente`() = runTest {
        // Given
        val productId = "123"
        whenever(mockFirestoreService.deleteProduct(productId))
            .thenReturn(Result.success(Unit))

        // When
        val result = repository.deleteProduct(productId)

        // Then
        assertTrue(result.isSuccess)
        verify(mockFirestoreService).deleteProduct(productId)
    }

    @Test
    fun `getTotalInventoryValue debe retornar flow de total`() = runTest {
        // Given
        val expectedTotal = 1500.0
        whenever(mockFirestoreService.getTotalInventoryValue())
            .thenReturn(flowOf(expectedTotal))

        // When
        val result = repository.getTotalInventoryValue().first()

        // Then
        assertEquals(expectedTotal, result, 0.001)
        verify(mockFirestoreService).getTotalInventoryValue()
    }
}