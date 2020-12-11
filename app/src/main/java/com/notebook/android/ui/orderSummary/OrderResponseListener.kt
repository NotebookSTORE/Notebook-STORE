package com.notebook.android.ui.orderSummary

import com.notebook.android.model.cashfree.CFTokenResponse

interface OrderResponseListener {
    fun onApiCallStarted()
    fun onApiCallAfterPayment()
    fun onSuccessOrder(it: CFTokenResponse, type: String)
    fun onApiAfterWalletAddCall()
    fun onSuccess(orderID:String, status:Int, amount:Float, txtMsg:String)
    fun onSuccessCFToken()
    fun onWalletFailure(orderID:String, status:Int, amount:Float, txtMsg:String)
    fun walletAmount(amount:String)
    fun onSuccesPayment(orderID:String, status:Int, amount:Float, txtMsg:String)
    fun onFailurePayment(orderID:String, status:Int, amount:Float, txtMsg:String)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
}