package com.univalle.equipocinco.data.remote.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.univalle.equipocinco.data.remote.dto.ProductDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authService: FirebaseAuthService
) {

    private fun getUserCollection(): CollectionReference {
        val userId = authService.getUserId()
            ?: throw IllegalStateException("User not authenticated. UID is null.")

        return firestore
            .collection("users")
            .document(userId)
            .collection("products")
    }


    fun getAllProducts(): Flow<List<ProductDto>> = callbackFlow {
        val collection = try {
            getUserCollection()
        } catch (e: Exception) {
            trySend(emptyList())
            close(e)
            return@callbackFlow
        }

        val listener = collection
            .orderBy("code")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }

                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ProductDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(products)
            }

        awaitClose { listener.remove() }
    }



    // Obtener producto por ID
    suspend fun getProductById(productId: String): ProductDto? {
        return try {
            val document = getUserCollection().document(productId).get().await()
            document.toObject(ProductDto::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    // Insertar producto
    suspend fun insertProduct(product: ProductDto): Result<String> {
        return try {
            val userId = authService.getUserId()
            android.util.Log.d("FirestoreService", "Inserting product for user: $userId")

            val docRef = getUserCollection().add(product).await()
            android.util.Log.d("FirestoreService", "Product inserted with ID: ${docRef.id}")

            Result.success(docRef.id)
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Error inserting product", e)
            Result.failure(e)
        }
    }

    // Actualizar producto
    suspend fun updateProduct(product: ProductDto): Result<Unit> {
        return try {
            getUserCollection().document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar producto
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            getUserCollection().document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Calcular valor total del inventario
    fun getTotalInventoryValue(): Flow<Double> = callbackFlow {
        val listener = getUserCollection().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val total = snapshot?.documents?.sumOf { doc ->
                val product = doc.toObject(ProductDto::class.java)
                product?.getTotal() ?: 0.0
            } ?: 0.0

            trySend(total)
        }

        awaitClose { listener.remove() }
    }
}