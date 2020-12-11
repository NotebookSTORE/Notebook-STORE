package com.notebook.android.model.cashfree

import java.io.Serializable

data class CFTokenResponse(
    var status:Int,
    var message:String,
    var cftoken:String,
    var msg:String ?= null,
    var error:Boolean,
    var amount:Float,
    var Amountafterdiscount:Float,
    var orderid:String
):Serializable {
}