package com.notebook.android.model.home

data class ProductCoupon(
    var status: Int ?= null,
    var error: Boolean,
    var msg: String ?= null,
    var productcoupon: List<ProdCoupon> ?= null
) {

    data class ProdCoupon(
        var id: Int ?= null,
        var code: String ?= null,
        var percent: String ?= null,
        var description: String ?= null,
        var usertype:Int,
        var totalamount:String,
        var discountedprice:String?=null,
        var start_date:String ?= null,
        var end_date:String ?= null
    )
}