package com.pro.foodorder.utils

import android.util.Patterns

object StringUtil {
    @JvmStatic
    fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) false else Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    @JvmStatic
    fun isEmpty(input: String?): Boolean {
        return input == null || input.isEmpty() || "" == input.trim { it <= ' ' }
    }

    @JvmStatic
    fun getDoubleNumber(number: Int): String {
        return if (number < 10) {
            "0$number"
        } else "" + number
    }
}