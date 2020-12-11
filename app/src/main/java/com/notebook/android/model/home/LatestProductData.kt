package com.notebook.android.model.home

import com.notebook.android.data.db.entities.LatestProduct

data class LatestProductData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var latestproduct: List<LatestProduct>? = null
) {
}