package com.notebook.android.ui.productDetail.listener

import com.notebook.android.model.cashfree.CFTokenResponse

interface FavouritesListener {
//    fun onApiCallStarted()
    fun onSuccessFavourites(it: CFTokenResponse, type: String)
    fun onFailureFavourites(msg:String)
}