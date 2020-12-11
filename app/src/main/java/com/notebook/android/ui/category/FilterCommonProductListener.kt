package com.notebook.android.ui.category

interface FilterCommonProductListener {
    fun onApiCallStarted()
    fun onApiCartCallStarted()
    fun onSuccess(isListSizeGreater:Boolean)
    fun onCartItemAdded(cartMsg:String)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onInvalidCredential()
    fun onGetBannerImageData(imgUrl:String)
    fun onNoInternetAvailable(msg:String)
}