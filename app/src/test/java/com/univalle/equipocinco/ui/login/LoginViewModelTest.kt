package com.univalle.equipocinco.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseUser
import com.univalle.equipocinco.data.repository.AuthRepository
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
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockAuthRepository: AuthRepository

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(mockAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login exitoso debe retornar success con usuario`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        whenever(mockAuthRepository.login(email, password))
            .thenReturn(Result.success(mockFirebaseUser))

        // When
        val result = viewModel.login(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockFirebaseUser, result.getOrNull())
        verify(mockAuthRepository).login(email, password)
    }

    @Test
    fun `login fallido debe retornar failure con excepcion`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpass"
        val exception = Exception("Credenciales inválidas")
        whenever(mockAuthRepository.login(email, password))
            .thenReturn(Result.failure(exception))

        // When
        val result = viewModel.login(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockAuthRepository).login(email, password)
    }

    @Test
    fun `login con email vacio debe retornar failure`() = runTest {
        // Given
        val email = ""
        val password = "password123"
        val exception = Exception("Email vacío")
        whenever(mockAuthRepository.login(email, password))
            .thenReturn(Result.failure(exception))

        // When
        val result = viewModel.login(email, password)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `register exitoso debe retornar success con usuario`() = runTest {
        // Given
        val email = "newuser@example.com"
        val password = "password123"
        whenever(mockAuthRepository.register(email, password))
            .thenReturn(Result.success(mockFirebaseUser))

        // When
        val result = viewModel.register(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockFirebaseUser, result.getOrNull())
        verify(mockAuthRepository).register(email, password)
    }

    @Test
    fun `register fallido debe retornar failure`() = runTest {
        // Given
        val email = "existing@example.com"
        val password = "password123"
        val exception = Exception("Usuario ya existe")
        whenever(mockAuthRepository.register(email, password))
            .thenReturn(Result.failure(exception))

        // When
        val result = viewModel.register(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockAuthRepository).register(email, password)
    }

    @Test
    fun `register con password corto debe retornar failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "12345" // Menos de 6 caracteres
        val exception = Exception("Password debe tener al menos 6 caracteres")
        whenever(mockAuthRepository.register(email, password))
            .thenReturn(Result.failure(exception))

        // When
        val result = viewModel.register(email, password)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `isUserLoggedIn debe retornar true cuando usuario esta autenticado`() {
        // Given
        whenever(mockAuthRepository.isUserLoggedIn()).thenReturn(true)

        // When
        val result = viewModel.isUserLoggedIn()

        // Then
        assertTrue(result)
        verify(mockAuthRepository).isUserLoggedIn()
    }

    @Test
    fun `isUserLoggedIn debe retornar false cuando usuario no esta autenticado`() {
        // Given
        whenever(mockAuthRepository.isUserLoggedIn()).thenReturn(false)

        // When
        val result = viewModel.isUserLoggedIn()

        // Then
        assertFalse(result)
        verify(mockAuthRepository).isUserLoggedIn()
    }

    @Test
    fun `multiple login attempts deben llamar al repositorio correctamente`() = runTest {
        // Given
        val email = "test@example.com"
        val password1 = "wrongpass"
        val password2 = "correctpass"

        whenever(mockAuthRepository.login(email, password1))
            .thenReturn(Result.failure(Exception("Credenciales inválidas")))
        whenever(mockAuthRepository.login(email, password2))
            .thenReturn(Result.success(mockFirebaseUser))

        // When
        val result1 = viewModel.login(email, password1)
        val result2 = viewModel.login(email, password2)

        // Then
        assertTrue(result1.isFailure)
        assertTrue(result2.isSuccess)
        verify(mockAuthRepository, times(2)).login(anyString(), anyString())
    }
}