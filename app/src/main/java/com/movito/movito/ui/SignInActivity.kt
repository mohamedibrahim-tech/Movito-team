package com.movito.movito.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.movito.movito.MainActivity
import com.movito.movito.theme.MovitoTheme

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            // User is logged in, navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish SignInActivity so user can't go back to it
            return // Stop further execution of onCreate
        }

        enableEdgeToEdge()
        setContent {
            MovitoTheme {
                SignInScreen(
                    onSignInSuccess = {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onSignUpClicked = {
                        startActivity(Intent(this, SignUpActivity::class.java))
                    }
                )
            }
        }
    }
}