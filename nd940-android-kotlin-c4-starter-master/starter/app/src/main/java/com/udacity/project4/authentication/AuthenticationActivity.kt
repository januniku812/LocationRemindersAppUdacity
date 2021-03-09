package com.udacity.project4.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    // private variables
    private val fireAuth = FirebaseAuth.getInstance()

    // viewModel
    private val authenticationViewModel: AuthenticationViewModel by lazy {
        ViewModelProvider(this).get(AuthenticationViewModel::class.java)
    }

    // onCreate() method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        // binding variable to access the authentication activity layout
        val binding: ActivityAuthenticationBinding = DataBindingUtil.setContentView(
            this, R.layout.activity_authentication)

        binding.loginButton.setOnClickListener {
            if(fireAuth.currentUser != null || authenticationViewModel.authenticationState == AuthenticationState.AUTHENTICATED){
                // if they are already logged in and just popped back, etc
                startActivity(Intent(this, RemindersActivity::class.java))
            }
            else {
                // if they not already logged in
                // start the sign in flow
                signInFlow()
            }
        }

        // observing the authentication state
        authenticationViewModel.authenticationState.observe(this, Observer {
           when(it){
               // if the user is ever logged out/unauthenticated they have to be redirected to the authentication activity
               // and "forced" to log in to resume
               AuthenticationState.UNAUTHENTICATED -> startActivity(Intent(this, AuthenticationActivity::class.java))
           }
        })
    }

    // enum class for the state of authentication
    enum class AuthenticationState{
        AUTHENTICATED, UNAUTHENTICATED
    }

    // sign in flow function that launches the flow
    private fun signInFlow(){
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val layout =
            AuthMethodPickerLayout.Builder(R.layout.activity_log_in)
                .setGoogleButtonId(R.id.google_button)
                .setEmailButtonId(R.id.email_button)
                .build()

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(layout)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }


    // companion object
    companion object{
        const val SIGN_IN_REQUEST_CODE = 1001
    }
}
