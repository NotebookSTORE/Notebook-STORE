package com.notebook.android.ui.merchant.responseListener

import com.notebook.android.data.db.entities.User
import com.notebook.android.model.merchant.PrimeMerchantResponse

interface PrimeRegisterRespListener {
    fun onStarted()
    fun onSuccessResponse(it: PrimeMerchantResponse)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
    fun onPrimeMerchantOTPVerify(user: User)
    fun onUpdatedRegularMerchant(user: User)
}