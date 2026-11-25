package com.example.findit.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.findit.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAuthSuccess: Boolean = false,
    val showForgotPasswordDialog: Boolean = false,
    val resetPasswordEmail: String = ""
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun onFirstNameChange(firstName: String) {
        _uiState.update { it.copy(firstName = firstName, errorMessage = null) }
    }

    fun onLastNameChange(lastName: String) {
        _uiState.update { it.copy(lastName = lastName, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onResetPasswordEmailChange(email: String) {
        _uiState.update { it.copy(resetPasswordEmail = email, errorMessage = null) }
    }

    fun setForgotPasswordDialogVisible(visible: Boolean) {
        _uiState.update {
            it.copy(
                showForgotPasswordDialog = visible,
                resetPasswordEmail = if (visible) it.email else "",
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun resetAuthState() {
        _uiState.value = AuthUiState()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.getCurrentUser() != null
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Veuillez saisir votre adresse e-mail.") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Adresse e-mail invalide.") }
            return
        }

        if (password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Veuillez saisir votre mot de passe.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        authRepository.signIn(email, password) { success, error ->
            if (success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthSuccess = true,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthSuccess = false,
                        errorMessage = error ?: "Échec de la connexion. Vérifiez vos identifiants."
                    )
                }
            }
        }
    }

    fun register() {
        val firstName = _uiState.value.firstName.trim()
        val lastName = _uiState.value.lastName.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        if (firstName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Veuillez saisir votre prénom.") }
            return
        }

        if (lastName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Veuillez saisir votre nom.") }
            return
        }

        if (email.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Veuillez saisir votre adresse e-mail.") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Adresse e-mail invalide.") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Le mot de passe doit contenir au moins 6 caractères.") }
            return
        }

        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Les mots de passe ne correspondent pas.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        authRepository.signUp(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            role = "etudiant"
        ) { success, error ->
            if (success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthSuccess = true,
                        errorMessage = null,
                        successMessage = "Compte créé avec succès!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthSuccess = false,
                        errorMessage = error ?: "Échec de la création du compte."
                    )
                }
            }
        }
    }

    fun sendPasswordReset() {
        val resetEmail = _uiState.value.resetPasswordEmail.trim()
        if (resetEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
            _uiState.update { it.copy(errorMessage = "Veuillez saisir une adresse e-mail valide.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        authRepository.sendPasswordResetEmail(resetEmail) { success, error ->
            if (success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showForgotPasswordDialog = false,
                        successMessage = "E-mail de réinitialisation envoyé à $resetEmail",
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error ?: "Impossible d'envoyer l'e-mail de réinitialisation."
                    )
                }
            }
        }
    }

    fun logout() {
        authRepository.signOut()
        resetAuthState()
    }
}
