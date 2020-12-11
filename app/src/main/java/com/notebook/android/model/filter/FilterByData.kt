package com.notebook.android.model.filter

import com.notebook.android.data.db.entities.*

data class FilterByData(
    var error:Boolean,
    var msg:String ?= null,
    var status:Int,
    var brand:List<BrandFilterBy> ?= null,
    var discount:List<DiscountFilterBy> ?= null,
    var rating:List<RatingFilterBy> ?= null,
    var color:List<ColorFilterBy> ?= null,
    var price:List<PriceFilterBy> ?= null,
    var coupons:List<CouponFilterBy> ?= null
) {
}