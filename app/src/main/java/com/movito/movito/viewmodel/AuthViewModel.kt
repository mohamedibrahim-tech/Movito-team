package com.movito.movito.viewmodel

import android.util.Patterns
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
    val message: String? = null,
    val user: FirebaseUser? = null,
    val isInitialCheckDone: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _navigationChannel = Channel<Unit>()
    val navigationFlow = _navigationChannel.receiveAsFlow()

    // FIX: Prevent login unless email is verified
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser

        _authState.value = _authState.value.copy(
            user = if (user != null && user.isEmailVerified) user else null,
            isInitialCheckDone = true
        )
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // SIGN UP with Email Verification
    fun signUpWithEmailPassword(email: String, password: String) {
        if (!isValidEmail(email)) {
            _authState.value = _authState.value.copy(error = "Invalid email format.")
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, message = null)

            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    user.sendEmailVerification().await()

                    // FIX: Prevent auto-login after signup
                    auth.signOut()

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        message = "Verification email sent. Please verify and login ."
                    )
                }

            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage
                )
            }
        }
    }

    // LOGIN (Only if email verified)
    fun signInWithEmailPassword(email: String, password: String) {
        if (!isValidEmail(email)) {
            _authState.value = _authState.value.copy(error = "Invalid email format.")
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {

                    // FIX: Block login if not verified
                    if (!user.isEmailVerified) {
                        auth.signOut()
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = "Please verify your email first."
                        )
                        return@launch
                    }

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = user
                    )

                    _navigationChannel.send(Unit)
                }

            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage
                )
            }
        }
    }

    // Google Sign In
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()

                val user = result.user

                if (user != null) {
                    _authState.value = _authState.value.copy(isLoading = false, user = user)
                    _navigationChannel.send(Unit)
                }

            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (!isValidEmail(email)) {
            _authState.value = _authState.value.copy(error = "Invalid email format.")
            return
        }
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, message = null)

            try {
                auth.sendPasswordResetEmail(email).await()

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    message = "Password reset email sent."
                )

            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState(
            user = null,
            isInitialCheckDone = true
        )
        FavoritesViewModel.getInstance().resetForNewUser() // reset FavoritesViewMode
    }

    fun resetState() {
        _authState.value = AuthState(
            user = auth.currentUser?.takeIf { it.isEmailVerified },
            isInitialCheckDone = true
        )
    }
}
