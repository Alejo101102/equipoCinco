package com.univalle.equipocinco.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.univalle.equipocinco.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel para la pantalla de login/registro.
 * Maneja la autenticación de usuarios con Firebase Auth.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * Intenta autenticar un usuario existente.
     *
     * @param email El correo electrónico del usuario
     * @param password La contraseña del usuario (mínimo 6 caracteres)
     * @return Result<FirebaseUser> Success con el usuario si login exitoso,
     *         Failure con excepción si falla
     *
     * Posibles errores:
     * - Email no registrado
     * - Contraseña incorrecta
     * - Usuario deshabilitado
     * - Error de red
     *
     * Función suspendida - debe llamarse desde una coroutine:
     * ```
     * viewLifecycleOwner.lifecycleScope.launch {
     *     val result = viewModel.login(email, password)
     *     if (result.isSuccess) {
     *         // Navegar al home
     *     } else {
     *         // Mostrar error
     *     }
     * }
     * ```
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return authRepository.login(email, password)
    }

    /**
     * Registra un nuevo usuario en Firebase Auth.
     *
     * @param email El correo electrónico del nuevo usuario (debe ser válido)
     * @param password La contraseña (mínimo 6 caracteres requerido por Firebase)
     * @return Result<FirebaseUser> Success con el usuario si registro exitoso,
     *         Failure con excepción si falla
     *
     * Validaciones de Firebase:
     * - Email debe tener formato válido
     * - Password debe tener al menos 6 caracteres
     * - Email no debe estar ya registrado
     *
     * Posibles errores:
     * - Email ya en uso
     * - Email inválido
     * - Password muy débil
     * - Error de red
     *
     * Nota: Si el registro es exitoso, el usuario queda automáticamente
     * autenticado y no necesita hacer login.
     */
    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return authRepository.register(email, password)
    }

    /**
     * Verifica si hay un usuario actualmente autenticado.
     *
     * @return true si hay una sesión activa, false si no hay usuario logueado
     *
     * Útil para:
     * - Decidir si mostrar login o home al abrir la app
     * - Validar estado antes de operaciones que requieren autenticación
     * - Debugging de estado de sesión
     *
     * Uso típico:
     * ```
     * override fun onViewCreated(...) {
     *     if (viewModel.isUserLoggedIn()) {
     *         // Ir al home
     *     } else {
     *         // Quedarse en login
     *     }
     * }
     * ```
     */
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}