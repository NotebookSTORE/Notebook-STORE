package com.notebook.android.model.home

import com.notebook.android.data.db.entities.BestSeller

data class BestSellerProductData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var bestseller: List<BestSeller>? = null
) {
}