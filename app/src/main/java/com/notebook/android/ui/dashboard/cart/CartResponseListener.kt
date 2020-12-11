package com.notebook.android.ui.dashboard.cart

import com.notebook.android.data.db.entities.Cart

interface CartResponseListener {
    fun onApiCallStarted()
    fun onUpdateOrDeleteCartStart()
    fun onSuccessCart(prod: List<Cart>?)
    fun onCartEmpty(isEmpty:Boolean)
    fun onCartProductItemAdded(success: String?)
    fun onCartItemDeleted(msg: String)
    fun onFailure(msg:String, isAddCart:Boolean)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
    fun onInvalidCredential()

    fun onFailureUpdateORDeleteCart(msg:String)
    fun onApiFailureUpdateORDeleteCart(msg:String)
    fun onNoInternetAvailableUpdateORDeleteCart(msg:String)
}