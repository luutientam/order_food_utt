package com.utt.foodorderapp.domain.usecase

import com.utt.foodorderapp.data.repository.AuthRepository
import com.utt.foodorderapp.data.repository.UserRepository
import com.utt.foodorderapp.model.User

class AuthUseCase(
        private val authRepository: AuthRepository = AuthRepository(),
        private val userRepository: UserRepository = UserRepository()
) {

    fun signUp(email: String, password: String, role: String, callback: (Boolean, User?, String?) -> Unit) {
        authRepository.signUp(email, password) { isSuccess, errorMessage ->
            if (!isSuccess) {
                callback(false, null, errorMessage)
            } else {
                val userId = authRepository.getCurrentUserId()
                val userEmail = authRepository.getCurrentUserEmail()
                if (userId.isNullOrEmpty() || userEmail.isNullOrEmpty()) {
                    callback(false, null, "Can not resolve account profile")
                } else {
                    val user = User(userEmail)
                    user.uid = userId
                    user.email = userEmail
                    user.role = role
                    user.isAdmin = role == User.ROLE_ADMIN
                    user.isActive = true
                    userRepository.saveUserProfile(user) { dbError ->
                        if (dbError != null) {
                            callback(false, null, dbError.message)
                            return@saveUserProfile
                        }
                        callback(true, user, null)
                    }
                }
            }
        }
    }

    fun signIn(email: String, password: String, callback: (Boolean, User?, String?) -> Unit) {
        authRepository.signIn(email, password) { isSuccess, errorMessage ->
            if (!isSuccess) {
                callback(false, null, errorMessage)
            } else {
                resolveCurrentProfile(callback)
            }
        }
    }

    fun signInWithGoogle(idToken: String, callback: (Boolean, User?, String?) -> Unit) {
        authRepository.signInWithGoogleIdToken(idToken) { isSuccess, errorMessage ->
            if (!isSuccess) {
                callback(false, null, errorMessage)
                return@signInWithGoogleIdToken
            }
            resolveCurrentProfile(callback)
        }
    }

    fun signUpWithGoogle(idToken: String, role: String, callback: (Boolean, User?, String?) -> Unit) {
        authRepository.signInWithGoogleIdToken(idToken) { isSuccess, errorMessage ->
            if (!isSuccess) {
                callback(false, null, errorMessage)
                return@signInWithGoogleIdToken
            }
            val userId = authRepository.getCurrentUserId()
            val userEmail = authRepository.getCurrentUserEmail()
            if (userId.isNullOrEmpty() || userEmail.isNullOrEmpty()) {
                callback(false, null, "Can not resolve account profile")
                return@signInWithGoogleIdToken
            }
            userRepository.getUserProfile(userId) { profile ->
                if (profile != null) {
                    callback(false, null, "google-account-exists")
                    return@getUserProfile
                }
                val user = User(userEmail)
                user.uid = userId
                user.email = userEmail
                user.role = role
                user.isAdmin = role == User.ROLE_ADMIN
                user.isActive = true
                userRepository.saveUserProfile(user) { dbError ->
                    if (dbError != null) {
                        callback(false, null, dbError.message)
                        return@saveUserProfile
                    }
                    callback(true, user, null)
                }
            }
        }
    }

    private fun resolveCurrentProfile(callback: (Boolean, User?, String?) -> Unit) {
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail()
        if (userId.isNullOrEmpty() || userEmail.isNullOrEmpty()) {
            callback(false, null, "Can not resolve account profile")
            return
        }
        userRepository.getUserProfile(userId) { profile ->
            if (profile != null) {
                profile.resolveRole()
                callback(profile.isActive, profile, if (profile.isActive) null else "locked-account")
                return@getUserProfile
            }
            val fallback = User(userEmail)
            fallback.uid = userId
            fallback.role = if (userEmail.contains("@admin.com")) User.ROLE_ADMIN else User.ROLE_CUSTOMER
            fallback.isAdmin = fallback.role == User.ROLE_ADMIN
            fallback.isActive = true
            userRepository.saveUserProfile(fallback) { dbError ->
                callback(dbError == null, fallback, dbError?.message)
            }
        }
    }
}
