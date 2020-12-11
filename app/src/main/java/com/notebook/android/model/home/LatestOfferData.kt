package com.notebook.android.model.home

import com.notebook.android.data.db.entities.LatestOffer

data class LatestOfferData(
    var status: Int ?= null,
    var error: Boolean,
    var msg: String ?= null,
    var latestoffer: List<LatestOffer> ?= null
)