package com.movito.movito.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: FirebaseUser? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState(user = auth.currentUser))
    val authState: StateFlow<AuthState> = _authState

    private val _navigationChannel = Channel<Unit>()
    val navigationFlow = _navigationChannel.receiveAsFlow()

    // The listener now only syncs the user state for external changes (e.g., token revoked).
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _authState.value = _authState.value.copy(user = firebaseAuth.currentUser)
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun signUpWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = _authState.value.copy(isLoading = false, user = result.user)
                // Directly send navigation event on success
                if (result.user != null) {
                    _navigationChannel.send(Unit)
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun signInWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = _authState.value.copy(isLoading = false, user = result.user)
                // Directly send navigation event on success
                if (result.user != null) {
                    _navigationChannel.send(Unit)
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                _authState.value = _authState.value.copy(isLoading = false, user = result.user)
                // Directly send navigation event on success
                if (result.user != null) {
                    _navigationChannel.send(Unit)
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun resetState() {
        _authState.value = _authState.value.copy(isLoading = false, error = null)
    }
}