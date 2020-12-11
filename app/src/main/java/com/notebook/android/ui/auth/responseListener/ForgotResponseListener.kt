package com.notebook.android.ui.auth.responseListener

import com.notebook.android.data.db.entities.User
import com.notebook.android.model.auth.ForgotPass

interface ForgotResponseListener {
    fun onSuccess(response: ForgotPass)
    fun onFailure(msg:String)
    fun onOtpSuccess(resp: User)
}