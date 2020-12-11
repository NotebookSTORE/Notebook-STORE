package com.notebook.android.model.orderSummary

data class PaymentSuccesData(
    var status: Int,
    var error: Boolean,
    var msg: String
)