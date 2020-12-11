package com.notebook.android.model.home

import com.notebook.android.data.db.entities.DiscountedProduct

data class DiscountedProdData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var product: List<DiscountedProduct>? = null
)