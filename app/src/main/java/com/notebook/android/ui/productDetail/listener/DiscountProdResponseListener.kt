package com.notebook.android.ui.productDetail.listener

import com.notebook.android.data.db.entities.DiscountedProduct
import com.notebook.android.model.home.BenefitProductData
import com.notebook.android.model.home.FreeDeliveryData
import com.notebook.android.model.home.ProductCoupon
import com.notebook.android.model.productDetail.ProductDetailData
import com.notebook.android.model.productDetail.RatingData

interface DiscountProdResponseListener {
    fun onApiCallStarted()
    fun onSuccessProductData(prodData:ProductDetailData)
    fun onSuccess(prod: List<DiscountedProduct>?)
    fun onSuccessFreeDeliveryData(freeDeliveryData:List<FreeDeliveryData.FreeDelivery>)
    fun onCouponDataSuccess(couponProd:List<ProductCoupon.ProdCoupon>)
    fun onApiFailure(msg: String)
    fun onProductDetailFailure(msg:String)
    fun onFailure(msg:String)
    fun onNoInternetAvailable(msg:String)
    fun onCartItemAdded(success:String)
    fun onProductRatingData(it: RatingData)

    fun pinSuccessful(mesg:String, date:String)
    fun onDeliveryNotAvailable(mesg:String)
    fun onInvalidCredential()
}