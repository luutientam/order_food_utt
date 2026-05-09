package com.utt.foodorderapp.data.repository

import com.google.firebase.database.DatabaseError
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.model.User

class UserRepository {

    fun getUserProfile(userId: String, callback: (User?) -> Unit) {
        val reference = ControllerApplication.getInstance().userDatabaseReference.child(userId)
        reference.get().addOnSuccessListener { snapshot ->
            callback(snapshot.getValue(User::class.java))
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun saveUserProfile(user: User, callback: (DatabaseError?) -> Unit) {
        val userId = user.uid ?: return callback(DatabaseError.fromStatus("missing-uid"))
        ControllerApplication.getInstance().userDatabaseReference
                .child(userId).setValue(user) { error, _ ->
                    callback(error)
                }
    }

    fun updateUserActive(userId: String, isActive: Boolean, callback: (DatabaseError?) -> Unit) {
        val map: MutableMap<String, Any> = HashMap()
        map["active"] = isActive
        ControllerApplication.getInstance().userDatabaseReference.child(userId).updateChildren(map) { error, _ ->
            callback(error)
        }
    }
}
