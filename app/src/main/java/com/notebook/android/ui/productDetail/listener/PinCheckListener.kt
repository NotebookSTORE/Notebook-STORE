package com.notebook.android.ui.productDetail.listener

interface PinCheckListener {
    fun pinSuccessful(mesg:String, date:String)
    fun onDeliveryNotAvailable(mesg:String)
}