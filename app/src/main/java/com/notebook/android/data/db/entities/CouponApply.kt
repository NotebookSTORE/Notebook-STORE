package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "CouponApply", indices = [Index(value = ["id"], unique = true)])
data class CouponApply(
    @PrimaryKey(autoGenerate = false)
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