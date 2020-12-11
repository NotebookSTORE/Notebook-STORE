package com.notebook.android.model.home

data class BenefitProductData(
    var status: Int ?= null,
    var error: Boolean,
    var msg: String ?= null,
    var benefit: List<BenefitData> ?= null
) {
    data class BenefitData(
        var id: Int,
        var title: String,
        var subtitle: String,
        var image: String)
}