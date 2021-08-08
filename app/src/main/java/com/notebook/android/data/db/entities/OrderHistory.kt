package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

const val ORDER_HISTORY_ID = 0
@Entity(tableName = "OrderHistory", indices = [Index(value = ["orderHistoryID"], unique = true)])
data class OrderHistory(
    var cartproduct_id: Int? = null,
    var original_price: Int? = null,
    var delivery_cost: Int? = null,
    var orderId: String,
    var cartprice: Float,
    var cartquantity: Int,
    var deliveryStatus: String? = null,
    var cartstatus: Int? = null,
    var carttotalamount: Float,
    var cartdiscount: Int? = null,
    var keyfeature: List<String>? = null,
    var material: String? = null,
    var title: String? = null,
    var alias: String? = null,
    var image: String? = null,
    var status: Int? = null,
    var address: String? = null,
    var city: String? = null,
    var state: String? = null,
    var quantity: Int,
    var price: Float,
    var offer_price: Int,
    var country: String? = null,
    var zipcode: String? = null,
    var discount: Int,
    var paymentmethod: String? = null,
    var best: Int? = null,
    var brandtitle: String? = null,
    var colortitle: String? = null,
    var rating: Float,
    var message: String? = null,
    var delivered_date: String? = null,
    var expected_date: String? = null,
    var return_date: String? = null,
    var created_at: String? = null,
    var cancel_status: Int,
    var return_status: Int,
    var cancel_description: String? = null,
    var ret_reason: String? = null,
    var can_reason: String? = null,
    var cancel_date: String? = null,
    var cancel_approve_date: String? = null,
    var tracking_url: String? = null
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var orderHistoryID: Int = ORDER_HISTORY_ID
}