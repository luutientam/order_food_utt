package com.utt.foodorderapp.model

import java.io.Serializable

class Restaurant : Serializable {
    var id: Long = 0
    var name: String? = null
    var address: String? = null
    var phone: String? = null
    var image: String? = null
    var isActive = true

    constructor()

    constructor(id: Long, name: String?, address: String?, phone: String?, image: String?, isActive: Boolean) {
        this.id = id
        this.name = name
        this.address = address
        this.phone = phone
        this.image = image
        this.isActive = isActive
    }
}
