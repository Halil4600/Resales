package com.example.resales.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthenticationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var user: FirebaseUser? by mutableStateOf(auth.currentUser)
        private set
    var message: String by mutableStateOf("")
        private set
    var isBusy: Boolean by mutableStateOf(false)
        private set

    fun signIn(email: String, password: String) {
        val e = email.trim()
        val p = password.trim()
        if (e.isEmpty() || p.isEmpty()) {
            message = "Email and password required"
            return
        }
        isBusy = true
        auth.signInWithEmailAndPassword(e, p)
            .addOnCompleteListener { task ->
                isBusy = false
                if (task.isSuccessful) {
                    user = auth.currentUser
                    message = ""
                } else {
                    user = null
                    message = task.exception?.message ?: "Login failed"
                }
            }
    }

    fun signUp(email: String, password: String) {
        val e = email.trim()
        val p = password.trim()
        if (e.isEmpty() || p.isEmpty()) {
            message = "Email and password required"
            return
        }
        isBusy = true
        auth.createUserWithEmailAndPassword(e, p)
            .addOnCompleteListener { task ->
                isBusy = false
                if (task.isSuccessful) {
                    user = auth.currentUser
                    message = ""
                } else {
                    user = null
                    message = task.exception?.message ?: "Register failed"
                }
            }
    }

    fun signOut() {
        auth.signOut()
        user = null
    }
}
