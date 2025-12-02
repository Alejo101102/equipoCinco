package com.univalle.equipocinco.data.repository

import com.univalle.equipocinco.data.remote.dto.ProductDto
import com.univalle.equipocinco.data.remote.firebase.FirestoreService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {

    fun getAllProducts(): Flow<List<ProductDto>> {
        return firestoreService.getAllProducts()
    }

    suspend fun getProductById(id: String): ProductDto? {
        return firestoreService.getProductById(id)
    }

    suspend fun insertProduct(product: ProductDto): Result<String> {
        return firestoreService.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductDto): Result<Unit> {
        return firestoreService.updateProduct(product)
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return firestoreService.deleteProduct(productId)
    }

    fun getTotalInventoryValue(): Flow<Double> {
        return firestoreService.getTotalInventoryValue()
    }
}