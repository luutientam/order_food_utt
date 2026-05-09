package com.utt.foodorderapp.data.repository

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.model.Order

class OrderRepository {

    fun createOrder(order: Order, callback: (DatabaseError?) -> Unit) {
        ControllerApplication.getInstance().bookingDatabaseReference
                .child(order.id.toString())
                .setValue(order) { error, _ -> callback(error) }
    }

    fun observeOrders(callback: (List<Order>) -> Unit, onError: (String) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = ArrayList<Order>()
                for (child in snapshot.children) {
                    val order = child.getValue(Order::class.java) ?: continue
                    result.add(0, order)
                }
                callback(result)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }
        ControllerApplication.getInstance().bookingDatabaseReference.addValueEventListener(listener)
        return listener
    }

    fun observeOrdersByChild(
            onAdded: (Order) -> Unit,
            onChanged: (Order) -> Unit,
            onRemoved: (Order) -> Unit
    ): ChildEventListener {
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(Order::class.java)?.let(onAdded)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(Order::class.java)?.let(onChanged)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                snapshot.getValue(Order::class.java)?.let(onRemoved)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        ControllerApplication.getInstance().bookingDatabaseReference.addChildEventListener(listener)
        return listener
    }

    fun removeOrderValueListener(listener: ValueEventListener?) {
        if (listener == null) return
        ControllerApplication.getInstance().bookingDatabaseReference.removeEventListener(listener)
    }

    fun removeOrderChildListener(listener: ChildEventListener?) {
        if (listener == null) return
        ControllerApplication.getInstance().bookingDatabaseReference.removeEventListener(listener)
    }

    fun updateOrder(orderId: Long, payload: Map<String, Any>, callback: (DatabaseError?) -> Unit) {
        ControllerApplication.getInstance().bookingDatabaseReference.child(orderId.toString())
                .updateChildren(payload) { error, _ -> callback(error) }
    }

    fun getOrder(orderId: Long, callback: (Order?) -> Unit) {
        ControllerApplication.getInstance().bookingDatabaseReference.child(orderId.toString())
                .get()
                .addOnSuccessListener { callback(it.getValue(Order::class.java)) }
                .addOnFailureListener { callback(null) }
    }
}
