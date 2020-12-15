package com.notebook.android.data.db.entities

import java.io.Serializable

data class OrderSummaryProduct(
    var id: String,
    var cartQuantity:Int,
    var cartTotalAmount:Float,
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
    var offer_price: Int,
    var product_code: String? = null,
    var product_condition: String? = null,
    var discount: Int,
    var latest: Int? = null,
    var best: Int? = null,
    var brandtitle: String? = null,
    var colortitle: String? = null,
    var delivery_charges:Float?=null,
    var isBuyNow:Int,
    var can_free_delivery:String ?= null,
) : Serializable {

    fun isFreeDeliveryAvailable() = can_free_delivery?.toInt() == 1

}

/* var category_id: Int? = null,
    var subcategory_id: Int? = null,
    var subsubsubcategory_id: Int? = null,
    var brand_id: Int? = null,*/