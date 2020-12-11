package com.notebook.android.ui.merchant.responseListener

import com.notebook.android.data.db.entities.User
import com.notebook.android.model.merchant.RegularMerchantResponse

interface RegularRegisterRespListener {
    fun onStarted()
    fun onSuccessResponse(it: RegularMerchantResponse)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
    fun onRegularMerchantOTPVerify(user: User)
    fun onUpdatedRegularMerchant(user:User)
}