package com.notebook.android.ui.auth.responseListener

import com.notebook.android.data.db.entities.User
import com.notebook.android.model.auth.RegistrationResponse

interface AuthLoginListener {
    fun onApiCallStarted()
    fun onSuccess(user: User)
    fun onSuccessSocial(user:User, imageUpdate:Int, loginType:Int)
    fun onFailure(msg:String)
    fun onLoginUserExist(status:Int, imageUpdate:Int, loginType:Int)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
}