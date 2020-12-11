package com.notebook.android.model.orderSummary

data class WalletSuccess(
    var userID:Int,
    var token:String,
    var orderId:String,
    var status:Int
) {
}