package com.notebook.android.ui.category.productParts.responseListener

import com.notebook.android.data.db.entities.BestSeller

interface BSProductListener {
    fun onApiCallStarted()
    fun onSuccess(bestSellerProd:List<BestSeller>?)
    fun onFailure(msg:String)
}