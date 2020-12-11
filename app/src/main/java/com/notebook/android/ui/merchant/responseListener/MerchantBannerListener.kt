package com.notebook.android.ui.merchant.responseListener

interface MerchantBannerListener {
    fun onApiCallStarted()
    fun onSuccessResponse()
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
}