package com.utt.foodorderapp.model

import java.io.Serializable

class Address : Serializable {
    var id: Long = 0
    var label: String? = null
    var recipientName: String? = null
    var phone: String? = null
    var fullAddress: String? = null
    var lat: Double = 0.0
    var lng: Double = 0.0
    var isDefault: Boolean = false

    constructor()

    constructor(id: Long, label: String?, recipientName: String?, phone: String?,
                fullAddress: String?, lat: Double, lng: Double, isDefault: Boolean) {
        this.id = id
        this.label = label
        this.recipientName = recipientName
        this.phone = phone
        this.fullAddress = fullAddress
        this.lat = lat
        this.lng = lng
        this.isDefault = isDefault
    }
}
