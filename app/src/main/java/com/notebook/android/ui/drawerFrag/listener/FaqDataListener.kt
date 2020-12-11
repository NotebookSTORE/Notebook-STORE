package com.notebook.android.ui.drawerFrag.listener

interface FaqDataListener {
    fun onApiCallStarted()
    fun onSuccess(msg: String?)
    fun onFailure(msg:String)
}