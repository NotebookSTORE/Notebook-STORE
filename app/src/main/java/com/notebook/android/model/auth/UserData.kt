package com.notebook.android.model.auth

import com.notebook.android.data.db.entities.User

data class UserData(
    var status:Int,
    var error:Boolean,
    var msg:String ?= null,
    var user:User ?= null,
var upgrade_require:String ?= null)