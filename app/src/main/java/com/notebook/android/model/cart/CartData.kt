package com.notebook.android.model.cart

data class CartData(
    var status: Int ?= null,
    var error: Boolean,
    var msg: String ?= null
) {
}