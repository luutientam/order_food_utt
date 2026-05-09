package com.pro.foodorder.constant

interface Constant {
    companion object {
        const val GENERIC_ERROR = "General error, please try again later"
        const val CURRENCY = " 000 VNĐ"
        const val TYPE_PAYMENT_CASH = 1
        const val PAYMENT_METHOD_CASH = "Tiền mặt"
        const val ADMIN_EMAIL_FORMAT = "@admin.com"

        // Key Intent
        const val KEY_INTENT_FOOD_OBJECT = "food_object"
    }
}