package com.notebook.android.model.merchant

import com.notebook.android.data.db.entities.User

data class PrimeMerchantResponse(
    var msg:String ?= null,
    var status:Int,
    var error:Boolean,
    var otp:String ?= null,
    var user: User ?= null
) {
}