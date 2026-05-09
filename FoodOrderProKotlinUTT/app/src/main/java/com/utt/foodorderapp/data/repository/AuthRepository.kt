package com.utt.foodorderapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    callback(task.isSuccessful, parseAuthError(task.exception))
                }
    }

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    callback(task.isSuccessful, parseAuthError(task.exception))
                }
    }

    fun signInWithGoogleIdToken(idToken: String, callback: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    callback(task.isSuccessful, parseAuthError(task.exception))
                }
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    private fun parseAuthError(throwable: Throwable?): String? {
        val firebaseCode = (throwable as? FirebaseAuthException)?.errorCode
        return when (firebaseCode) {
            "ERROR_INVALID_EMAIL" -> ERROR_INVALID_EMAIL
            "ERROR_EMAIL_ALREADY_IN_USE" -> ERROR_EMAIL_ALREADY_IN_USE
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND",
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_INVALID_LOGIN_CREDENTIALS" -> ERROR_INVALID_CREDENTIALS
            "ERROR_USER_DISABLED" -> ERROR_USER_DISABLED
            "ERROR_TOO_MANY_REQUESTS" -> ERROR_TOO_MANY_REQUESTS
            "ERROR_NETWORK_REQUEST_FAILED" -> ERROR_NETWORK
            else -> throwable?.message
        }
    }

    companion object {
        const val ERROR_INVALID_EMAIL = "auth-invalid-email"
        const val ERROR_EMAIL_ALREADY_IN_USE = "auth-email-already-in-use"
        const val ERROR_INVALID_CREDENTIALS = "auth-invalid-credentials"
        const val ERROR_USER_DISABLED = "auth-user-disabled"
        const val ERROR_TOO_MANY_REQUESTS = "auth-too-many-requests"
        const val ERROR_NETWORK = "auth-network-request-failed"
    }
}
