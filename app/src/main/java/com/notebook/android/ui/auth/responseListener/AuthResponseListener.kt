package com.notebook.android.ui.auth.responseListener

import com.notebook.android.data.db.entities.User
import com.notebook.android.model.auth.RegistrationResponse

interface AuthResponseListener {
    fun onApiCallStarted()
    fun onSuccess(response: RegistrationResponse)
    fun onFailure(msg:String)
    fun onOtpSuccess(resp: User?)
    fun onApiFailure(msg:String)
    fun onInternetNotAvailable(msg:String)
}