package com.utt.foodorderapp.model

import java.io.Serializable

class Order : Serializable {

    var id: Long = 0
    var name: String? = null
    var email: String? = null
    var phone: String? = null
    var address: String? = null
    var amount = 0
    var foods: String? = null
    var payment = 0
    var paymentStatus = PAYMENT_STATUS_UNPAID
    var paymentTransactionId: String? = null
    var isCompleted = false
    var status = STATUS_NEW
    var shipperId: String? = null
    var shipperEmail: String? = null
    var issueNote: String? = null
    var customerId: String? = null
    var deliveryLat = 0.0
    var deliveryLng = 0.0
    var shipperLat = 0.0
    var shipperLng = 0.0
    var originalAmount = 0
    var discountAmount = 0
    var promotionCode: String? = null

    constructor() {}

    constructor(id: Long, name: String?, email: String?, phone: String?,
                address: String?, amount: Int, foods: String?, payment: Int, completed: Boolean,
                status: Int = STATUS_NEW, deliveryLat: Double = 0.0, deliveryLng: Double = 0.0,
                originalAmount: Int = 0, discountAmount: Int = 0, promotionCode: String? = null,
                customerId: String? = null, shipperLat: Double = 0.0, shipperLng: Double = 0.0,
                paymentStatus: Int = PAYMENT_STATUS_UNPAID, paymentTransactionId: String? = null) {
        this.id = id
        this.name = name
        this.email = email
        this.phone = phone
        this.address = address
        this.amount = amount
        this.foods = foods
        this.payment = payment
        this.paymentStatus = paymentStatus
        this.paymentTransactionId = paymentTransactionId
        isCompleted = completed
        this.status = status
        this.deliveryLat = deliveryLat
        this.deliveryLng = deliveryLng
        this.originalAmount = if (originalAmount > 0) originalAmount else amount
        this.discountAmount = discountAmount
        this.promotionCode = promotionCode
        this.customerId = customerId
        this.shipperLat = shipperLat
        this.shipperLng = shipperLng
    }

    fun getStatusValue(): Int {
        if (status == STATUS_NEW && isCompleted) {
            return STATUS_SUCCESS
        }
        return status
    }

    fun isCancelableByUser(): Boolean {
        return getStatusValue() == STATUS_NEW
    }

    fun syncCompletedValue() {
        isCompleted = getStatusValue() == STATUS_SUCCESS
    }

    fun canShipperTake(currentUserId: String?): Boolean {
        if (currentUserId.isNullOrEmpty()) {
            return false
        }
        val currentStatus = getStatusValue()
        return (currentStatus == STATUS_PREPARING || currentStatus == STATUS_NEW)
                && shipperId.isNullOrEmpty()
    }

    fun isAssignedToShipper(currentUserId: String?): Boolean {
        if (currentUserId.isNullOrEmpty()) {
            return false
        }
        return shipperId == currentUserId
    }

    companion object {
        const val PAYMENT_STATUS_UNPAID = 0
        const val PAYMENT_STATUS_PAID = 1
        const val STATUS_NEW = 0
        const val STATUS_PREPARING = 1
        const val STATUS_DELIVERING = 2
        const val STATUS_SUCCESS = 3
        const val STATUS_CANCEL = 4
        const val STATUS_FAIL = 5
    }
}