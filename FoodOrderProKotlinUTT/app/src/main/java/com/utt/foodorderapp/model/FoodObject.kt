package com.utt.foodorderapp.model

import java.io.Serializable

class FoodObject : Serializable {

    var id: Long = 0
    var name: String? = null
    var description: String? = null
    var restaurantId: Long = 0
    var restaurantName: String? = null
    var categoryId: Long = 0
    var categoryName: String? = null
    var price = 0
    var sale = 0
    var image: String? = null
    var banner: String? = null
    var isPopular = false
    var images: List<Image>? = null

    constructor() {}

    constructor(id: Long, name: String?, description: String?, restaurantId: Long, restaurantName: String?,
                categoryId: Long, categoryName: String?, price: Int, sale: Int,
                image: String?, banner: String?, popular: Boolean) {
        this.id = id
        this.name = name
        this.description = description
        this.restaurantId = restaurantId
        this.restaurantName = restaurantName
        this.categoryId = categoryId
        this.categoryName = categoryName
        this.price = price
        this.sale = sale
        this.image = image
        this.banner = banner
        isPopular = popular
    }
}