package com.notebook.android.model.productDetail

import com.notebook.android.data.db.entities.ProductDetailEntity

data class ProductDetailData(
    var error:Boolean,
    var msg:String,
    var status:Int,
    var product:ProductDetailEntity,
    var productImg:List<ProductImageData> ?= null,
    var return_days:Int ?= null){
    data class ProductImageData(
        var id:Int,
        var product_id:Int,
        var title:String,
        var image:String ?= null,
        var status:Int,
        var created_at:String ?= null,
        var updated_at:String ?= null,
        var deleted_at:String ?= null)
}