package com.notebook.android.ui.productDetail.listener

interface RateProdListener {
    fun onApiCallStarted()
    fun onSuccess(successMsg:String)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onInvalidCredential()
    fun onNoInternetAvailable(msg:String)
}