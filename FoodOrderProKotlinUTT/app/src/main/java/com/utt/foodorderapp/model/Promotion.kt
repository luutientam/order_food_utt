package com.utt.foodorderapp.model

import java.io.Serializable

class Promotion : Serializable {
    var code: String? = null
    var title: String? = null
    var discountPercent = 0
    var minOrderAmount = 0
    var maxDiscountAmount = 0
    var isActive = true

    constructor()

    constructor(
            code: String?,
            title: String?,
            discountPercent: Int,
            minOrderAmount: Int,
            maxDiscountAmount: Int,
            isActive: Boolean
    ) {
        this.code = code
        this.title = title
        this.discountPercent = discountPercent
        this.minOrderAmount = minOrderAmount
        this.maxDiscountAmount = maxDiscountAmount
        this.isActive = isActive
    }
}
