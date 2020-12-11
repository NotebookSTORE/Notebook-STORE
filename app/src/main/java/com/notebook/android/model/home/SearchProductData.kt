package com.notebook.android.model.home

import com.notebook.android.data.db.entities.SearchProduct

data class SearchProductData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var product: List<SearchProduct>
)