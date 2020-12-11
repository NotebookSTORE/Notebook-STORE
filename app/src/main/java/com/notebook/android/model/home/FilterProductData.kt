package com.notebook.android.model.home

import com.notebook.android.data.db.entities.FilterProduct

data class FilterProductData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var product: List<FilterProduct>? = null,
    var banner: List<BannerData>?= null
) {
    data class BannerData(var image: String? = null)
}