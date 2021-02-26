package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.notebook.android.model.coupon.CouponData

@Entity(tableName = "CouponApply", indices = [Index(value = ["id"], unique = true)])
data class CouponApply(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    var code: String,
    var percent: String? = null,
    var discountedprice: String? = null,
    var start_date: String? = null,
    var end_date: String? = null,
    var coupon_type: String? = null,
    var coupon_user_type: String? = null,
    var max_amount: String? = null,
    var email_can_avail: String? = null,
    var description: String? = null,
    var product_id: String? = null
) {
    fun getCouponCanApply(): CouponData.CouponCanApply {
        return CouponData.CouponCanApply(
            id = id,
            code = code,
            percent = percent,
            discountedprice = discountedprice,
            start_date = start_date,
            end_date = end_date,
            coupon_type = coupon_type,
            coupon_user_type = coupon_user_type,
            max_amount = max_amount,
            email_can_avail = email_can_avail,
            description = description,
            product_id = product_id,
        )
    }
}