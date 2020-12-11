package com.notebook.android.model.cart

data class CartDelete(
    var status: Int ?= null,
    var error: Boolean,
    var msg: String ?= null,
    var cartdata:Int
)