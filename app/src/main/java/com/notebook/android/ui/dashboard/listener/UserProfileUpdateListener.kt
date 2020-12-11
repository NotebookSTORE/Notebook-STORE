package com.notebook.android.ui.dashboard.listener

import com.notebook.android.data.db.entities.User

interface UserProfileUpdateListener {
    fun onApiCallStarted()
    fun onApiCallOtpVerifyStarted()
    fun onSuccess(user:User?)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onOtpSuccess(resp: User?)
    fun otpVerifyWhenProfileUpdate(otp:String?)
    fun onNoInternetAvailable(msg:String)
    fun onSuccessLogout()
    fun onInvalidCredential()
    fun walletAmount(amount:String)
}