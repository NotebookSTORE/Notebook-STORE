package com.notebook.android.ui.dashboard.searchProduct

import com.notebook.android.data.db.entities.SearchProduct

interface SearchProdResponseListener {
    fun onApiStarted()
    fun onCartItemAddCallStarted()
    fun onSuccess(prod: List<SearchProduct>)
    fun onCartItemAdded(cartMsg:String)
    fun onSuccessListEmpty(product: List<SearchProduct>)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onInvalidCredential()
    fun onNoInternetAvailable(msg:String)
}