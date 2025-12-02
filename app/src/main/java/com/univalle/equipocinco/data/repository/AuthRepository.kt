package com.univalle.equipocinco.data.repository

import com.google.firebase.auth.FirebaseUser
import com.univalle.equipocinco.data.remote.firebase.FirebaseAuthService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: FirebaseAuthService
) {

    fun isUserLoggedIn(): Boolean = authService.isUserLoggedIn()

    fun getCurrentUser(): FirebaseUser? = authService.getCurrentUser()

    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return authService.registerUser(email, password)
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return authService.loginUser(email, password)
    }

    fun logout() {
        authService.logout()
    }
}