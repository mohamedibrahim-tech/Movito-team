package com.movito.movito.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class to hold the authentication state
data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: FirebaseUser? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun signUpWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState(user = result.user)
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
            }
        }
    }

    fun signInWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState(user = result.user)
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                _authState.value = AuthState(user = result.user)
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState()
    }
}
