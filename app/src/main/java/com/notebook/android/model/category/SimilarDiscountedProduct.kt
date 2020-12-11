package com.notebook.android.model.category

import com.notebook.android.data.db.entities.Product

data class SimilarDiscountedProduct(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var product: List<Product>? = null
)