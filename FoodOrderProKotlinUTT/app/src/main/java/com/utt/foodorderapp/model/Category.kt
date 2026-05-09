package com.utt.foodorderapp.model

import java.io.Serializable

class Category : Serializable {
    var id: Long = 0
    var name: String? = null
    var isActive = true

    constructor()

    constructor(id: Long, name: String?, isActive: Boolean) {
        this.id = id
        this.name = name
        this.isActive = isActive
    }
}
