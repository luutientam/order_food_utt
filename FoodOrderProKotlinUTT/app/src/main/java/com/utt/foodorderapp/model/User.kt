package com.utt.foodorderapp.model

import com.google.gson.Gson

class User {

    var uid: String? = null
    var email: String? = null
    var name: String? = null
    var phone: String? = null
    var role: String = ROLE_CUSTOMER
    var isActive = true
    var isAdmin = false

    constructor() {}
    constructor(email: String?) {
        this.email = email
    }

    fun resolveRole() {
        if (isAdmin) {
            role = ROLE_ADMIN
            return
        }
        if (role == ROLE_RESTAURANT) {
            role = ROLE_ADMIN
            return
        }
        if (role.isEmpty()) {
            role = ROLE_CUSTOMER
        }
    }

    fun isShipper(): Boolean {
        return role == ROLE_SHIPPER
    }

    fun isRestaurant(): Boolean {
        return false
    }

    fun isCustomer(): Boolean {
        return role == ROLE_CUSTOMER
    }

    fun toJSon(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_CUSTOMER = "customer"
        const val ROLE_RESTAURANT = "restaurant"
        const val ROLE_SHIPPER = "shipper"
    }
}