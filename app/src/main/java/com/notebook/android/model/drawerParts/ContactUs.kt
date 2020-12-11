package com.notebook.android.model.drawerParts

import com.notebook.android.data.db.entities.User

data class ContactUs(
    var error:Boolean,
    var msg:String ?= null,
    var status:Int,
    var user:User ?= null
) {
}