package com.notebook.android.model.auth

import com.notebook.android.data.db.entities.User

class LogoutUserData( var status:Int,
                      var error:Boolean,
                      var msg:String,
                      var user: Int?= null) {
}