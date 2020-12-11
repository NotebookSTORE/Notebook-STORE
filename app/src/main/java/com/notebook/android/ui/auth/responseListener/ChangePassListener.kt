package com.notebook.android.ui.auth.responseListener

interface ChangePassListener {
    fun onSuccess(status:Boolean)
    fun onFailure(msg:String)
}