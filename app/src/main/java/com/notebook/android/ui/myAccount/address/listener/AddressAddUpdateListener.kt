package com.notebook.android.ui.myAccount.address.listener

interface AddressAddUpdateListener {
    fun onApiStarted()
    fun onApiCountryStarted()
    fun onSuccess()
    fun onSuccessCountry()
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
    fun onCallCountryDialogOpen()
    fun makeDefaultOrDeleteAddressSuccess()
    fun onInvalidCredential()
}