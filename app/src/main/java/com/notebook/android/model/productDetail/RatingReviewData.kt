package com.notebook.android.model.productDetail

import com.notebook.android.data.db.entities.RatingReviews

data class RatingReviewData(
    var error:Boolean,
    var msg:String ?= null,
    var status:Int,
    var productrating:List<RatingReviews> ?= null,
    var ratingcount:Int,
    var ratingtotalsum:Float,
    var average:String
)