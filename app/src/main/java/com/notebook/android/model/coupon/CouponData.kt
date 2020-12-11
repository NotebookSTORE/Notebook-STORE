package com.notebook.android.model.coupon

import com.notebook.android.data.db.entities.CouponApply

data class CouponData(
    var error:Boolean,
    var msg:String ?= null,
    var status:Int,
    var coupon:List<CouponApply> ?= null,
    var coupan_can_apply:List<CouponCanApply> ?= null
) {
    data class CouponCanApply(
        var id:Int ,
        var code: String,
        var percent: String ?= null,
        var discountedprice: String ?= null,
        var start_date: String? = null,
        var end_date: String? = null,
        var coupon_type: String? = null,
        var coupon_user_type: String? = null,
        var max_amount: String? = null,
        var email_can_avail: String? = null,
        var description: String? = null,
        var product_id:String ?= null
    )
}