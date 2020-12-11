package com.notebook.android.ui.merchant.responseListener

import com.notebook.android.data.db.entities.Banner

interface MerchantBenefitListener {
    fun onApiCallStarted()
    fun onSuccessResponse(successMsg: String, primeSubscriptionCharge:String)
    fun onSuccessBannerResponse(bannerresponse: List<Banner>?)
    fun onSuccessDefaultAddress(defaultAddr:String)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
}