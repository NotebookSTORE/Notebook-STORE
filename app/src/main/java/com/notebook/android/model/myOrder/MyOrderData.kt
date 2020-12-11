package com.notebook.android.model.myOrder

import com.notebook.android.data.db.entities.OrderHistory

data class MyOrderData(
    var status: Int? = null,
    var error: Boolean,
    var msg: String? = null,
    var cartdata: ArrayList<OrderHistory>
)