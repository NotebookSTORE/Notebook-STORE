package com.notebook.android.model.wallet

data class AddWalletResponse(
    var status: Int,
    var error: Boolean,
    var message: String? = null,
    var cftoken: String? = null,
    var msg: String? = null,
    var amount:Float ?= null,
    var totalwalletamount:Float ?= null,
    var orderid:String ?= null
) {
}