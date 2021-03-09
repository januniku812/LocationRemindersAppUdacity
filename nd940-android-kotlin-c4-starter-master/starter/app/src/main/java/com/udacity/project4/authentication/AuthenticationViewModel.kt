package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AuthenticationViewModel: ViewModel() {
    private val _authenticationState = MutableLiveData<AuthenticationActivity.AuthenticationState>()
    val authenticationState: LiveData<AuthenticationActivity.AuthenticationState>
        get() = _authenticationState
}