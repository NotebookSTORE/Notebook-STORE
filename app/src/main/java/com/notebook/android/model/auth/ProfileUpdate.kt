package com.notebook.android.model.autdh

import com.notebook.android.data.db.entities.User

data class ProfileUpdate( var status:Int,
                          var error:Boolean,
                          var msg:String ?= null,
                          var userverifieddata: User?= null,
var user:User ?= null)