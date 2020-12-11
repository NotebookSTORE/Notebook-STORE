package com.notebook.android.ui.productDetail.listener

interface BulkOrderListener {
    fun onApiCallStarted()
    fun onSuccess(successMsg:String)
    fun onFailure(msg:String)
}