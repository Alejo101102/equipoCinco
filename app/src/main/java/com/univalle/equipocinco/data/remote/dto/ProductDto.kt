package com.univalle.equipocinco.data.remote.dto

import com.google.firebase.firestore.DocumentId

data class ProductDto(
    @DocumentId
    val id: String = "",          // Firestore genera IDs automáticos
    val code: Int = 0,            // Código del producto (4 dígitos)
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", 0, "", 0.0, 0)
    
    fun getTotal(): Double = price * quantity
}