package com.notebook.android.ui.category.productParts.responseListener

import com.notebook.android.data.db.entities.LatestProduct

interface LatestProductListener {
    fun onApiCallStarted()
    fun onSuccess(latestProd:List<LatestProduct>?)
    fun onFailure(msg:String)
}