package com.notebook.android.model.address

data class AddAddress(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null
) {
}