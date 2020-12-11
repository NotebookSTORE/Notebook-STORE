package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Cart", indices = [Index(value = ["cartproduct_id"], unique = true)])
data class Cart(
    @PrimaryKey(autoGenerate = false)
    var cartproduct_id: String,
    var original_price: Float? = null,
    var cartprice: Float? = null,
    var cartquantity: Int,
    var cartstatus: Int,
    var carttotalamount: Float? = null,
    var cartdiscount: Int? = null,
    var keyfeature:List<String> ?= null,
    var material: String? = null,
    var title: String? = null,
    var alias: String? = null,
    var image: String? = null,
    var status: Int? = null,
    var short_description: String? = null,
    var description: String? = null,
    var data_sheet: String? = null,
    var quantity: Int,
    var price: Float,
    var offer_price: Int? = null,
    var product_code: String? = null,
    var product_condition: String? = null,
    var discount: Int? = null,
    var latest: Int? = null,
    var best: Int? = null,
    var brandtitle: String? = null,
    var colortitle: String? = null,
    var customerRating: Float ?= null,
    var reviewCount:Int ?= null,
    var delivery_charges:Float?=null,
    var can_free_delivery:String ?= null,
    var can_cashon:String ?= null
) {
}