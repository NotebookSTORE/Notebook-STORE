package com.notebook.android.model.home

data class FreeDeliveryData(
    var status: Int ?= null,
    var error: Boolean,
    var msg: String ?= null,
    var freedelivery: List<FreeDelivery> ?= null
) {
    data class FreeDelivery(
        var title:String,
        var price:Int
    )
}