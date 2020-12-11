package com.notebook.android.model.address

import com.notebook.android.data.db.entities.Address

data class FetchAddresses(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var address:List<Address> ?= null
) {
}