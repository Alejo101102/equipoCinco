package com.univalle.equipocinco.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.univalle.equipocinco.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return authRepository.login(email, password)
    }

    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return authRepository.register(email, password)
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}