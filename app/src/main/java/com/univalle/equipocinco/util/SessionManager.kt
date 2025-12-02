package com.univalle.equipocinco.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

class SessionManager(context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun setLoggedIn(value: Boolean) {
        // Firebase Auth mantiene la sesión automáticamente
        // Este método existe solo para compatibilidad
    }

    fun clearSession() {
        auth.signOut()
    }

    fun getUserId(): String? {
        return auth.currentUser?.uid
    }
}