package com.notebook.android.model.payment

data class PaymentData(
    var orderID:String,
    var orderAmount:Float,
    var msg:String?,
    var status:Int
)
/*,
    var orderStatus:String*/