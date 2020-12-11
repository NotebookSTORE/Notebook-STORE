package com.notebook.android.ui.productDetail.listener

import com.notebook.android.model.productDetail.RatingReviewData

interface RatingViewAllListener {
    fun onApiCallStarted()
    fun onSuccess(it: RatingReviewData)
    fun onFailure(msg:String)
    fun onApiFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
}