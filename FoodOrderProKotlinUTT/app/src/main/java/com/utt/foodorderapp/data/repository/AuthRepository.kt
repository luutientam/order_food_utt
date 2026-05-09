package com.utt.foodorderapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    callback(task.isSuccessful, task.exception?.message)
                }
    }

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    callback(task.isSuccessful, task.exception?.message)
                }
    }

    fun signInWithGoogleIdToken(idToken: String, callback: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    callback(task.isSuccessful, task.exception?.message)
                }
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }
}
