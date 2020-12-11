package com.notebook.android.ui.dashboard.listener

interface PolicyDataListener {
    fun onApiCallStarted()
    fun onSuccess(apiResponse:String)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onInternetNotAvailable(msg:String)
}