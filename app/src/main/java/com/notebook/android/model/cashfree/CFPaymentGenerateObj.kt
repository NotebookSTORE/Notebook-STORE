package com.notebook.android.model.cashfree

import java.io.Serializable

data class CFPaymentGenerateObj(
    var orderId:String,
    var orderAmount:String,
    var customerPhone:String,
    var customerEmail:String,
    var cfToken:String
):Serializable {
}