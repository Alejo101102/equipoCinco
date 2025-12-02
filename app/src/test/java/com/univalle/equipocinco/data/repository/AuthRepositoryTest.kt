package com.univalle.equipocinco.data.repository

import com.google.firebase.auth.FirebaseUser
import com.univalle.equipocinco.data.remote.firebase.FirebaseAuthService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AuthRepositoryTest {

    @Mock
    private lateinit var mockAuthService: FirebaseAuthService

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = AuthRepository(mockAuthService)
    }

    @Test
    fun `isUserLoggedIn debe retornar true cuando usuario esta logueado`() {
        // Given
        whenever(mockAuthService.isUserLoggedIn()).thenReturn(true)

        // When
        val result = repository.isUserLoggedIn()

        // Then
        assertTrue(result)
        verify(mockAuthService).isUserLoggedIn()
    }

    @Test
    fun `isUserLoggedIn debe retornar false cuando usuario no esta logueado`() {
        // Given
        whenever(mockAuthService.isUserLoggedIn()).thenReturn(false)

        // When
        val result = repository.isUserLoggedIn()

        // Then
        assertFalse(result)
    }

    @Test
    fun `getCurrentUser debe retornar usuario cuando esta logueado`() {
        // Given
        whenever(mockAuthService.getCurrentUser()).thenReturn(mockFirebaseUser)

        // When
        val result = repository.getCurrentUser()

        // Then
        assertNotNull(result)
        assertEquals(mockFirebaseUser, result)
    }

    @Test
    fun `getCurrentUser debe retornar null cuando no esta logueado`() {
        // Given
        whenever(mockAuthService.getCurrentUser()).thenReturn(null)

        // When
        val result = repository.getCurrentUser()

        // Then
        assertNull(result)
    }

    @Test
    fun `register debe retornar success con usuario cuando registro es exitoso`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password123"
        whenever(mockAuthService.registerUser(email, password))
            .thenReturn(Result.success(mockFirebaseUser))

        // When
        val result = repository.register(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockFirebaseUser, result.getOrNull())
        verify(mockAuthService).registerUser(email, password)
    }

    @Test
    fun `register debe retornar failure cuando falla`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "123"
        val exception = Exception("Password muy corta")
        whenever(mockAuthService.registerUser(email, password))
            .thenReturn(Result.failure(exception))

        // When
        val result = repository.register(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `login debe retornar success con usuario cuando login es exitoso`() = runTest {
        // Given
        val email = "test@test.com"
        val password = "password123"
        whenever(mockAuthService.loginUser(email, password))
            .thenReturn(Result.success(mockFirebaseUser))

        // When
        val result = repository.login(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockFirebaseUser, result.getOrNull())
    }

    @Test
    fun `logout debe llamar a authService logout`() {
        // When
        repository.logout()

        // Then
        verify(mockAuthService).logout()
    }
}