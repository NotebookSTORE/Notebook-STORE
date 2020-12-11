package com.notebook.android.model.home

import com.notebook.android.data.db.entities.Banner

data class BannerData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var bannerresponse: List<Banner>? = null
)