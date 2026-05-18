package com.utt.foodorderapp.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.utt.foodorderapp.ControllerApplication
import com.utt.foodorderapp.model.Address

/**
 * Sổ địa chỉ giao hàng theo từng user.
 * Đường dẫn: /addresses/{uid}/{addressId}
 */
class AddressRepository {

    private fun userRef(uid: String): DatabaseReference =
            ControllerApplication.getInstance().addressDatabaseReference.child(uid)

    fun observeAddresses(uid: String, onResult: (List<Address>) -> Unit, onError: (String) -> Unit): ValueEventListener {
        val ref = userRef(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<Address>()
                for (child in snapshot.children) {
                    val item = child.getValue(Address::class.java) ?: continue
                    list.add(item)
                }
                list.sortByDescending { it.isDefault }
                onResult(list)
            }
            override fun onCancelled(error: DatabaseError) { onError(error.message) }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeListener(uid: String, listener: ValueEventListener?) {
        if (listener == null) return
        userRef(uid).removeEventListener(listener)
    }

    fun saveAddress(uid: String, address: Address, callback: (DatabaseError?) -> Unit) {
        val ref = userRef(uid)
        if (address.id == 0L) address.id = System.currentTimeMillis()
        ref.child(address.id.toString()).setValue(address) { error, _ -> callback(error) }
    }

    fun deleteAddress(uid: String, addressId: Long, callback: (DatabaseError?) -> Unit) {
        userRef(uid).child(addressId.toString())
                .removeValue { error, _ -> callback(error) }
    }

    fun setDefault(uid: String, addressId: Long, callback: (DatabaseError?) -> Unit) {
        val ref = userRef(uid)
        ref.get().addOnSuccessListener { snapshot ->
            val updates = HashMap<String, Any>()
            for (child in snapshot.children) {
                val item = child.getValue(Address::class.java) ?: continue
                val isThisOne = item.id == addressId
                if (item.isDefault != isThisOne) {
                    updates["${item.id}/isDefault"] = isThisOne
                }
            }
            if (updates.isEmpty()) {
                callback(null)
            } else {
                ref.updateChildren(updates) { error, _ -> callback(error) }
            }
        }.addOnFailureListener {
            callback(DatabaseError.fromException(it))
        }
    }

    fun getDefaultAddress(uid: String, callback: (Address?) -> Unit) {
        userRef(uid).get().addOnSuccessListener { snapshot ->
            var fallback: Address? = null
            for (child in snapshot.children) {
                val item = child.getValue(Address::class.java) ?: continue
                if (item.isDefault) {
                    callback(item)
                    return@addOnSuccessListener
                }
                if (fallback == null) fallback = item
            }
            callback(fallback)
        }.addOnFailureListener { callback(null) }
    }
}
