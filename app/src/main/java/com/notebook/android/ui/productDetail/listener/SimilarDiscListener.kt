package com.notebook.android.ui.productDetail.listener

import com.notebook.android.data.db.entities.Product

interface SimilarDiscListener {
    fun onApiCallStarted()
    fun onSuccess(prodList:List<Product>)
    fun onFailure(msg:String)
}