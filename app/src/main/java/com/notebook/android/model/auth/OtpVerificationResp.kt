package com.notebook.android.model.auth

data class OtpVerificationResp(
    var error:Boolean,
    var msg:String ?= null,
    var status:Int ?= null,
    var responseData:Int ?= null,
    var token:String ?= null
)