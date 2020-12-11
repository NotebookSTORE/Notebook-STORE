package com.notebook.android.model.cart

import com.notebook.android.data.db.entities.Cart
import com.notebook.android.data.db.entities.User

data class CartResponseData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var user: User ?= null,
    var cartdata: List<Cart>? = null
) {
}