package com.utt.foodorderapp.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utt.foodorderapp.domain.usecase.AuthUseCase
import com.utt.foodorderapp.model.User
import com.utt.foodorderapp.presentation.common.UiState

class AuthViewModel(
        private val authUseCase: AuthUseCase = AuthUseCase()
) : ViewModel() {

    private val _signInState = MutableLiveData<UiState<User>>(UiState.Idle)
    val signInState: LiveData<UiState<User>> = _signInState

    private val _signUpState = MutableLiveData<UiState<User>>(UiState.Idle)
    val signUpState: LiveData<UiState<User>> = _signUpState

    private val _googleSignInState = MutableLiveData<UiState<User>>(UiState.Idle)
    val googleSignInState: LiveData<UiState<User>> = _googleSignInState

    private val _googleSignUpState = MutableLiveData<UiState<User>>(UiState.Idle)
    val googleSignUpState: LiveData<UiState<User>> = _googleSignUpState

    fun signIn(email: String, password: String) {
        _signInState.value = UiState.Loading
        authUseCase.signIn(email, password) { isSuccess, profile, errorMessage ->
            if (isSuccess && profile != null) {
                _signInState.postValue(UiState.Success(profile))
            } else {
                _signInState.postValue(UiState.Error(errorMessage ?: "Sign in failed"))
            }
        }
    }

    fun signUp(email: String, password: String, role: String) {
        _signUpState.value = UiState.Loading
        authUseCase.signUp(email, password, role) { isSuccess, profile, errorMessage ->
            if (isSuccess && profile != null) {
                _signUpState.postValue(UiState.Success(profile))
            } else {
                _signUpState.postValue(UiState.Error(errorMessage ?: "Sign up failed"))
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _googleSignInState.value = UiState.Loading
        authUseCase.signInWithGoogle(idToken) { isSuccess, profile, errorMessage ->
            if (isSuccess && profile != null) {
                _googleSignInState.postValue(UiState.Success(profile))
            } else {
                _googleSignInState.postValue(UiState.Error(errorMessage ?: "Google sign in failed"))
            }
        }
    }

    fun signUpWithGoogle(idToken: String, role: String) {
        _googleSignUpState.value = UiState.Loading
        authUseCase.signUpWithGoogle(idToken, role) { isSuccess, profile, errorMessage ->
            if (isSuccess && profile != null) {
                _googleSignUpState.postValue(UiState.Success(profile))
            } else {
                _googleSignUpState.postValue(UiState.Error(errorMessage ?: "Google sign up failed"))
            }
        }
    }
}
