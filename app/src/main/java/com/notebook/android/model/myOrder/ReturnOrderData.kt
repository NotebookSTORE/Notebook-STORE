package com.notebook.android.model.myOrder

data class ReturnOrderData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null
) {
}