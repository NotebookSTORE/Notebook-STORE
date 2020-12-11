package com.notebook.android.model.orderSummary

data class OrderData(
    var title:String,
    var type:String,
    var returnPolicy:String ?= null,
    var returnTill:String ?= null
) {
}