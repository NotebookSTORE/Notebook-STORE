package com.notebook.android.model.orderSummary

data class AfterPaymentRawData(
    var status:Int,
    var type:Int,
var token:String,
var userID:Int,
    var orderId:String,
    var amount:Float,
    var is_buy_now:Int,
    var txtMsg:String,
    var primeupdate:Int
)

/*type -> payment mode
* 0 -> cod
* 1 -> cashfree
* 2 -> wallet
*
*
* isBuyNow -> 1 -> from product page
* 0 - > from cart page*/