package com.notebook.android.model.filter

data class FilterRequestData(
    var brand_id: List<Int>,
    var price1: Int,
    var price2: Int,
    var discount: List<Int>,
    var color_id: List<Int>,
    var rating: List<Int>,
    var coupon: List<Int>,
    var filter:String,
    var para:Int,
    var filterType: Int
)