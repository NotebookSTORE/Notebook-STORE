package com.notebook.android.model.address

import com.notebook.android.data.db.entities.Country

data class CountryData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var country:List<Country> ?= null
) {
}