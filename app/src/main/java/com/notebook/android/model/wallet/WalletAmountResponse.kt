package com.notebook.android.model.wallet

data class WalletAmountResponse(
    var status: Int,
    var error: Boolean,
    var msg: String? = null,
    var amount:String ?= null
)