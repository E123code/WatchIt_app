package com.example.watchit_movieapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.watchit_movieapp.databinding.ActivityAutnLoginBinding
import com.example.watchit_movieapp.utilities.FireStoreManager
import com.example.watchit_movieapp.utilities.SignalManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class AuthLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutnLoginBinding

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().setRequireName(true).setAllowNewAccounts(true).build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.AuthScreenTheme)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityAutnLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        if (FirebaseAuth.getInstance().currentUser != null) {
            transactToNextScreen()
        } else {
            initViews()
        }


    }

    private fun initViews() {

        binding.loginBTN.setOnClickListener {
            signIn()
        }
    }


    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser

            val isNewUser = response?.isNewUser ?: false

            if (isNewUser) {
                if(user != null)
                    FireStoreManager.createUser(user)
            } else {
                SignalManager.getInstance().toast(
                    "Hello ${user?.displayName}",
                    SignalManager.ToastLength.SHORT
                )
                transactToNextScreen()
            }
        } else {
            if (response == null)
                SignalManager.getInstance().toast("Login failed", SignalManager.ToastLength.SHORT)
            else {
                SignalManager.getInstance().toast("Error", SignalManager.ToastLength.LONG)
                signIn()
            }
        }

    }




    private fun transactToNextScreen() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        finish()
    }


}