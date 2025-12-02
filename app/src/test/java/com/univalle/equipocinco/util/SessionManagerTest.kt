package com.univalle.equipocinco.util

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class SessionManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Configurar mocks
        whenever(mockContext.getSharedPreferences("user_session", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        whenever(mockEditor.clear()).thenReturn(mockEditor)

        sessionManager = SessionManager(mockContext)
    }

    @Test
    fun `isLoggedIn debe retornar true cuando usuario esta logueado`() {
        // Given
        whenever(mockSharedPreferences.getBoolean("logged_in", false))
            .thenReturn(true)

        // When
        val result = sessionManager.isLoggedIn()

        // Then
        assertTrue(result)
        verify(mockSharedPreferences).getBoolean("logged_in", false)
    }

    @Test
    fun `isLoggedIn debe retornar false cuando usuario no esta logueado`() {
        // Given
        whenever(mockSharedPreferences.getBoolean("logged_in", false))
            .thenReturn(false)

        // When
        val result = sessionManager.isLoggedIn()

        // Then
        assertFalse(result)
    }

    @Test
    fun `setLoggedIn true debe guardar sesion`() {
        // When
        sessionManager.setLoggedIn(true)

        // Then
        verify(mockEditor).putBoolean("logged_in", true)
        verify(mockEditor).apply()
    }

    @Test
    fun `setLoggedIn false debe cerrar sesion`() {
        // When
        sessionManager.setLoggedIn(false)

        // Then
        verify(mockEditor).putBoolean("logged_in", false)
        verify(mockEditor).apply()
    }

    @Test
    fun `clearSession debe limpiar todas las preferencias`() {
        // When
        sessionManager.clearSession()

        // Then
        verify(mockEditor).clear()
        verify(mockEditor).apply()
    }
}