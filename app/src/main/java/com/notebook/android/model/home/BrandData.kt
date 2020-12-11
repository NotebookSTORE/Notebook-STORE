package com.notebook.android.model.home

import com.notebook.android.data.db.entities.Brand

data class BrandData(
    var status: Int ?= null,
    var error: Boolean,
    var msg: String ?= null,
    var brand: List<Brand> ?= null
)