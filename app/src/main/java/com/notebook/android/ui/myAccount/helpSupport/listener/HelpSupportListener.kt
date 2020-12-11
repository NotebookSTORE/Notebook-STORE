package com.notebook.android.ui.myAccount.helpSupport.listener

import com.notebook.android.model.helpSupport.HelpSupportData

interface HelpSupportListener {
    fun onApiCallStarted()
    fun onSuccesHelpSupportData(helpSupportData:HelpSupportData.HelpSupportMain)
    fun onSuccess(msg: String)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
    fun onInvalidCredential()
}