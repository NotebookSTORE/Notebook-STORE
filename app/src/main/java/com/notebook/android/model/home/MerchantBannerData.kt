package com.notebook.android.model.home

import com.notebook.android.data.db.entities.MerchantBanner

data class MerchantBannerData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var Merchantbanner: List<MerchantBanner>? = null
)