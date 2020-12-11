package com.notebook.android.ui.myOrder.listener

import com.notebook.android.data.db.entities.OrderHistory

interface OrderHistoryListener {
    fun onApiCallStarted()
    fun onSuccessResponse(orderHistory: List<OrderHistory>)
    fun onSuccessCancelReturn(msg:String)
    fun onFailureResponse(msg:String)
    fun onApiFailureResponse(msg:String)
    fun onInternetNotAvailable(msg:String)
    fun onInvalidCredential()
}