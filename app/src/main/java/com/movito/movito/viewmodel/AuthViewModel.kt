package com.movito.movito.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.movito.movito.LanguageManager
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

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

   // private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _navigationChannel = Channel<Unit>()
    val navigationFlow = _navigationChannel.receiveAsFlow()

    private fun invalidEmailFormatMsg() = if (LanguageManager.currentLanguage.value == "ar") "تنسيق البريد غير صالح." else "Invalid email format."
    private fun PasswordResetMsg() = if(LanguageManager.currentLanguage.value == "ar") "تم ارسال بريد اعادة التعيين." else "Password reset email sent."
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

//    private fun isValidEmail(email: String): Boolean {
//        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
//    }
//private fun isValidEmail(email: String): Boolean {
//    return email.isNotEmpty() &&
//            email.contains("@") &&
//            email.contains(".") &&
//            email.indexOf("@") < email.lastIndexOf(".")
//}
private fun isValidEmail(email: String): Boolean {
    val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailPattern.matches(email)
}

    // SIGN UP with Email Verification
    fun signUpWithEmailPassword(email: String, password: String) {
        if (!isValidEmail(email)) {
            _authState.value = _authState.value.copy(error = invalidEmailFormatMsg())
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
                        message =  if (LanguageManager.currentLanguage.value == "ar") "تم إرسال بريد إلكتروني للتحقق. يُرجى التحقق وتسجيل الدخول." else "Verification email sent. Please verify and login."
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
            _authState.value = _authState.value.copy(error = invalidEmailFormatMsg())
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
                            error =  if (LanguageManager.currentLanguage.value == "ar") "رجاءً تحقق من بريدك الإلكتروني أولًا." else "Please verify your email first."
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

    fun sendPasswordResetEmail(email: String) {
        if (!isValidEmail(email)) {
            _authState.value = _authState.value.copy(error = invalidEmailFormatMsg())
            return
        }
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, message = null)

            try {
                auth.sendPasswordResetEmail(email).await()

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    message = PasswordResetMsg()
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
