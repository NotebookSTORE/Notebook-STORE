package com.notebook.android.ui.drawerFrag.listener

import com.notebook.android.model.helpSupport.HelpSupportData

interface ContactUsListener {
    fun onApiCallStarted()
    fun onSuccess(msg:String)
    fun onSuccesHelpSupportData(helpSupportData: HelpSupportData.HelpSupportMain)
    fun onFailure(error:String)
    fun onApiFailure(msg: String)
    fun onNoInternetAvailable(msg: String)
}