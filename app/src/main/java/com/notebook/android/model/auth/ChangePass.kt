package com.notebook.android.model.auth

data class ChangePass(
    var status:Int ?= null,
    var error:Boolean,
    var msg:String ?= null
)