package com.notebook.android.model.auth

data class RegistrationResponse(
    var status:Int ?= null,
    var error:Boolean,
    var msg:String ?= null,
    var otp:String ?= null)