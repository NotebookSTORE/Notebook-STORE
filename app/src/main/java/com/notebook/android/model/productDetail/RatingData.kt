package com.notebook.android.model.productDetail

data class RatingData(
    var error:Boolean,
    var msg:String ?= null,
    var status:Int,
    var productrating:ProdRating ?= null,
    var ratingcount:Int,
    var ratingtotalsum:Float,
    var average:String
) {
    data class ProdRating(
        var id:Int,
        var user_id:Int,
        var rating:Float,
        var name:String,
        var email:String,
        var message:String,
        var product_id:Int,
        var status:Int,
        var currentdate:String)
}