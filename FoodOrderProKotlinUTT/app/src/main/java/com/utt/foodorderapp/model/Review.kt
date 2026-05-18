package com.utt.foodorderapp.model

import java.io.Serializable

class Review : Serializable {
    var id: Long = 0
    var orderId: Long = 0
    var userEmail: String? = null
    var restaurantId: Long = 0
    var foodId: Long = 0
    var rating = 5
    var comment: String? = null
    var createdAt: Long = 0

    constructor()

    constructor(id: Long, userEmail: String?, restaurantId: Long, foodId: Long, rating: Int, comment: String?, createdAt: Long) {
        this.id = id
        this.userEmail = userEmail
        this.restaurantId = restaurantId
        this.foodId = foodId
        this.rating = rating
        this.comment = comment
        this.createdAt = createdAt
    }
}
