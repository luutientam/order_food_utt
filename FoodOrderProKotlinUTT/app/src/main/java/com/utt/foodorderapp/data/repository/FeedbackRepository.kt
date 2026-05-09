package com.utt.foodorderapp.data.repository

import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.model.Feedback

class FeedbackRepository {

    fun submitFeedback(feedback: Feedback, callback: (DatabaseError?) -> Unit) {
        ControllerApplication.getInstance().feedbackDatabaseReference.child(feedback.id.toString())
                .setValue(feedback) { error, _ -> callback(error) }
    }

    fun observeFeedbacks(callback: (List<Feedback>) -> Unit, onError: (String) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = ArrayList<Feedback>()
                for (child in snapshot.children) {
                    val feedback = child.getValue(Feedback::class.java) ?: continue
                    result.add(0, feedback)
                }
                callback(result)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }
        ControllerApplication.getInstance().feedbackDatabaseReference.addValueEventListener(listener)
        return listener
    }

    fun removeFeedbackListener(listener: ValueEventListener?) {
        if (listener == null) return
        ControllerApplication.getInstance().feedbackDatabaseReference.removeEventListener(listener)
    }

    fun deleteFeedback(feedbackId: Long) {
        ControllerApplication.getInstance().feedbackDatabaseReference.child(feedbackId.toString()).removeValue()
    }
}
