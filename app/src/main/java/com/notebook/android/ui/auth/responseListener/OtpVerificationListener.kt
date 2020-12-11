package com.notebook.android.ui.auth.responseListener

interface OtpVerificationListener {
    fun otpVerifyData(otpValue:String)
    fun resendOtpCall(resend:Boolean)
}