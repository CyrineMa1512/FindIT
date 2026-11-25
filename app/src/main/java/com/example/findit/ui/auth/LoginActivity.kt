package com.example.findit.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.findit.ui.main.MainActivity
import com.example.findit.ui.theme.FindItTheme
import com.example.findit.viewmodel.AuthViewModel

class LoginActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Auto-navigate to MainActivity if user is already authenticated
        if (viewModel.isUserLoggedIn()) {
            navigateToMain()
            return
        }

        setContent {
            FindItTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = {
                        val intent = Intent(this, RegisterActivity::class.java)
                        startActivity(intent)
                    },
                    onLoginSuccess = {
                        navigateToMain()
                    }
                )
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
