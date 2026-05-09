package com.utt.foodorderapp.model

class Feedback {

    var id: Long = 0
    var name: String? = null
    var phone: String? = null
    var email: String? = null
    var comment: String? = null
    var rating = 5
    var restaurantId: Long = 0
    var foodId: Long = 0
    var createdAt: Long = 0

    constructor() {}

    constructor(id: Long, name: String?, phone: String?, email: String?, comment: String?, rating: Int, restaurantId: Long, foodId: Long, createdAt: Long) {
        this.id = id
        this.name = name
        this.phone = phone
        this.email = email
        this.comment = comment
        this.rating = rating
        this.restaurantId = restaurantId
        this.foodId = foodId
        this.createdAt = createdAt
    }
}