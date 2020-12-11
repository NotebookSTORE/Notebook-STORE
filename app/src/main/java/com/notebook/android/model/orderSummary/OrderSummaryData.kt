package com.notebook.android.model.orderSummary

data class OrderSummaryData(
    var totalItem:Int,
    var totalItemAmount:Float,
    var paymentType:Int
) {

    /*Status me 1 pe success aur 0 pe failed aur type me 1 pe direct aur 0 pe cart se*/
}