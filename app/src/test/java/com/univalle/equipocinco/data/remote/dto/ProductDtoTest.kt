package com.univalle.equipocinco.data.remote.dto

import org.junit.Assert.*
import org.junit.Test

class ProductDtoTest {

    @Test
    fun `constructor vacio debe crear producto con valores por defecto`() {
        // When
        val product = ProductDto()

        // Then
        assertEquals("", product.id)
        assertEquals(0, product.code)
        assertEquals("", product.name)
        assertEquals(0.0, product.price, 0.001)
        assertEquals(0, product.quantity)
    }

    @Test
    fun `constructor con parametros debe crear producto correctamente`() {
        // When
        val product = ProductDto(
            id = "123",
            code = 1001,
            name = "Laptop",
            price = 1500.0,
            quantity = 5
        )

        // Then
        assertEquals("123", product.id)
        assertEquals(1001, product.code)
        assertEquals("Laptop", product.name)
        assertEquals(1500.0, product.price, 0.001)
        assertEquals(5, product.quantity)
    }

    @Test
    fun `getTotal debe calcular correctamente precio por cantidad`() {
        // Given
        val product = ProductDto(
            id = "1",
            code = 100,
            name = "Mouse",
            price = 25.50,
            quantity = 10
        )

        // When
        val total = product.getTotal()

        // Then
        assertEquals(255.0, total, 0.001)
    }

    @Test
    fun `getTotal con cantidad cero debe retornar cero`() {
        // Given
        val product = ProductDto(
            id = "1",
            code = 100,
            name = "Teclado",
            price = 50.0,
            quantity = 0
        )

        // When
        val total = product.getTotal()

        // Then
        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun `getTotal con precio cero debe retornar cero`() {
        // Given
        val product = ProductDto(
            id = "1",
            code = 100,
            name = "Monitor",
            price = 0.0,
            quantity = 5
        )

        // When
        val total = product.getTotal()

        // Then
        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun `getTotal con valores decimales debe calcular correctamente`() {
        // Given
        val product = ProductDto(
            id = "1",
            code = 100,
            name = "Cable USB",
            price = 9.99,
            quantity = 7
        )

        // When
        val total = product.getTotal()

        // Then
        assertEquals(69.93, total, 0.001)
    }
}