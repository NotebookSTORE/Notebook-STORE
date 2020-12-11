package com.notebook.android.model.address

import com.notebook.android.data.db.entities.User

data class MakeDefaultAddress(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var user:User
) {
}