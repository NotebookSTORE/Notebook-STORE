package com.notebook.android.ui.splash

interface SplashResponseListener {
    fun onSuccess()
    fun onFailure(msg:String)
}