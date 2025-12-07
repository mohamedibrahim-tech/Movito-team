package com.movito.movito.viewmodel

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

/**
 * Data class representing the authentication state of the application.
 *
 * **Author**: Movito Development Team Member [Yossef Mohamed](https://github.com/yossefsayedhassan/)
 *
 * @property isLoading Indicates if an authentication operation is in progress
 * @property error Error message if authentication failed, `null` otherwise
 * @property message Success message if authentication succeeded, `null` otherwise
 * @property user The currently authenticated Firebase user, `null` if not authenticated
 * @property isInitialCheckDone Indicates if the initial auth state check has been completed
 *
 * @since 14 Nov 2025
 */
data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val user: FirebaseUser? = null,
    val isInitialCheckDone: Boolean = false
)

/**
 * [ViewModel] responsible for managing user authentication operations.
 *
 * This [ViewModel] handles:
 * - User registration (sign-up) with email verification
 * - User login with email/password
 * - Password reset functionality
 * - Authentication state observation
 * - Multi-language support for error/success messages
 *
 * **Author**: Movito Development Team Member [Yossef Mohamed](https://github.com/yossefsayedhassan/)
 *
 * @property auth [FirebaseAuth] instance for authentication operations
 * @since 14 Nov 2025
 */
class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _navigationChannel = Channel<Unit>()
    val navigationFlow = _navigationChannel.receiveAsFlow()

    // Localized message getters
    private fun invalidEmailFormatMsg() = if (LanguageManager.currentLanguage.value == "ar")
        "تنسيق البريد غير صالح." else "Invalid email format."
    private fun passwordResetMsg() = if(LanguageManager.currentLanguage.value == "ar")
        "تم ارسال بريد اعادة التعيين." else "Password reset email sent."

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

    /**
     * Validates an email address using a regex pattern.
     *
     * **Author**: Movito Development Team Member [Yossef Mohamed](https://github.com/yossefsayedhassan/)
     *
     * @param email The email address to validate
     * @return `true` if the email is valid, `false` otherwise
     *
     * @since 15 Nov 2025
     */
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailPattern.matches(email)
    }

    /**
     * Registers a new user with email and password, and sends email verification.
     *
     * This function:
     * 1. Validates the email format
     * 2. Creates a new user account in Firebase
     * 3. Sends an email verification link to the user
     * 4. Signs out immediately to prevent auto-login without verification
     * 5. Updates the authentication state with success/error messages
     *
     * **Author**: Movito Development Team Member [Yossef Mohamed](https://github.com/yossefsayedhassan/)
     *
     * @param email User's email address
     * @param password User's password (minimum `6` characters required by `Firebase)
     *
     * @see FirebaseAuth.createUserWithEmailAndPassword
     * @see FirebaseUser.sendEmailVerification
     *
     * @since 14 Nov 2025
     */
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

                    // Prevent auto-login after signup - require email verification first
                    auth.signOut()

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        message =  if (LanguageManager.currentLanguage.value == "ar")
                            "تم إرسال بريد إلكتروني للتحقق. يُرجى التحقق وتسجيل الدخول."
                        else "Verification email sent. Please verify and login."
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

    /**
     * Authenticates a user with email and password.
     *
     * This function:
     * 1. Validates the email format
     * 2. Checks if the user's email has been verified
     * 3. Signs in the user if email is verified
     * 4. Signs out and shows error if email is not verified
     *
     * **Author**: Movito Development Team Member [Yossef Mohamed](https://github.com/yossefsayedhassan/)
     *
     * @param email User's email address
     * @param password User's password (minimum `6` characters required by `Firebase)
     *
     * @see FirebaseAuth.createUserWithEmailAndPassword
     *
     * @param email User's email address
     * @param password User's password
     *
     * @see FirebaseAuth.signInWithEmailAndPassword
     *
     * @since 14 Nov 2025
     */
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
                    // Block login if email is not verified
                    if (!user.isEmailVerified) {
                        auth.signOut()
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error =  if (LanguageManager.currentLanguage.value == "ar")
                                "رجاءً تحقق من بريدك الإلكتروني أولًا."
                            else "Please verify your email first."
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

    /**
     * Sends a password reset email to the specified email address.
     *
     * **Author**: Movito Development Team Member [Yossef Mohamed](https://github.com/yossefsayedhassan/)
     *
     * @param email The email address to send the reset link to
     *
     * @see FirebaseAuth.sendPasswordResetEmail
     *
     * @since 15 Nov 2025
     */
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
                    message = passwordResetMsg()
                )

            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /**
     * Signs out the current user and resets authentication state.
     * Also resets the [FavoritesViewModel] to clear user-specific data.
     *
     * **Author**: Movito Development Team Member [Mohamed Ibrahim](https://github.com/mohamedibrahim-tech)
     *
     * @since 15 Nov 2025
     */
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState(
            user = null,
            isInitialCheckDone = true
        )
        FavoritesViewModel.getInstance().resetForNewUser() // reset FavoritesViewModel
    }

    /**
     * Resets the authentication state to reflect the current Firebase auth state.
     * Used to clear temporary messages and errors after they've been displayed.
     *
     * **Author**: Movito Development Team Member [Yossef Mohamed](https://github.com/yossefsayedhassan/)
     *
     * @since 15 Nov 2025
     */
    fun resetState() {
        _authState.value = AuthState(
            user = auth.currentUser?.takeIf { it.isEmailVerified },
            isInitialCheckDone = true
        )
    }
}