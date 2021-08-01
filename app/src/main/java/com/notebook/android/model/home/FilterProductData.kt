package com.notebook.android.model.home

import com.notebook.android.data.db.entities.FilterProduct

data class FilterProductData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var product: PageDetail? = null,
    var banner: List<BannerData>? = null
) {
    data class BannerData(var image: String? = null)
}

data class PageDetail(
    var current_page: Int,
    var first_page_url: String,
    var from: Int,
    var last_page: Int,
    var last_page_url: String,
    var next_page_url: String,
    var prev_page_url: String,
    var path: String,
    var per_page: Int,
    var to: Int,
    var total: Int,
    var data: List<FilterProduct>?
)