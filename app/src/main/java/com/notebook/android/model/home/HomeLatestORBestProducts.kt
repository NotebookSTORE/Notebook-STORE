package com.notebook.android.model.home

import com.notebook.android.data.db.entities.BestSellerHome
import com.notebook.android.data.db.entities.LatestProductHome

data class HomeLatestORBestProducts(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var bestseller: List<BestSellerHome>? = null,
    var latestproduct: List<LatestProductHome>? = null
)