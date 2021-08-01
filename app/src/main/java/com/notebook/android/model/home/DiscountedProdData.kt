package com.notebook.android.model.home

import com.notebook.android.data.db.entities.DiscountedProduct


data class DiscountedProdData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var product: DiscountedPageDetail? = null,
)
{

}
data class DiscountedPageDetail(
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
    var data: List<DiscountedProduct>?
){

}