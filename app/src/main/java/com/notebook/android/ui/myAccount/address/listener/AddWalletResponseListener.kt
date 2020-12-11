package com.notebook.android.ui.myAccount.address.listener

interface AddWalletResponseListener {
    fun onApiCallStarted()
    fun onApiAfterWalletAddCall()
    fun onSuccess(orderID:String, status:Int, amount:Float, txtMsg:String)
    fun onSuccessCFToken()
    fun onWalletFailure(orderID:String, status:Int, amount:Float, txtMsg:String)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onInvalidCredential()
    fun onNoInternetAvailable(msg:String)
}