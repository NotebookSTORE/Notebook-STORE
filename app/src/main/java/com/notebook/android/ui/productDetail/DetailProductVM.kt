package com.notebook.android.ui.productDetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.data.db.entities.Wishlist
import com.notebook.android.model.coupon.CouponData
import com.notebook.android.ui.dashboard.cart.CartResponseListener
import com.notebook.android.ui.productDetail.listener.*
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.wait

class DetailProductVM(
    val prodDetailRepo: ProdDetailRepo
) : ViewModel() {

    lateinit var discProdListener: DiscountProdResponseListener
    lateinit var cartRespListener:CartResponseListener
    lateinit var ratingViewListener:RatingViewAllListener
    lateinit var rateProdListener:RateProdListener
    lateinit var favListener:FavouritesListener

    lateinit var pinCheckListener:PinCheckListener

    fun getAllCouponDataFromDB() = prodDetailRepo.getAllCouponDataFromDB()
    fun getAllDiscountProdFromDB() = prodDetailRepo.getAllDiscountProducts()
    fun getUserData() = prodDetailRepo.getUserData()
    fun getCartData() = prodDetailRepo.getCartData()
    fun getProductDetailLiveData() = prodDetailRepo.getProductDetailData()

    var couponCanApplyData:MutableLiveData<List<CouponData.CouponCanApply>> = MutableLiveData()

    fun deleteUser(){
        viewModelScope.launch(Dispatchers.IO) {
            prodDetailRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            prodDetailRepo.clearCartTable()
            prodDetailRepo.clearAddressTable()
            prodDetailRepo.clearOrderTable()
            prodDetailRepo.clearFavouriteTable()
        }
    }

    fun checkPincodeAvailability(pincode: String){
        Coroutines.main{
            try {
                val prodResponse = prodDetailRepo.checkPincodeAvailabilit(pincode)
                prodResponse.let {
                    if(it.status == 1){
//                        favListener.onSuccessFavourites(it, type)
                        discProdListener.pinSuccessful(it.msg, it.estimated_delvery_date)
                    }else{
                        discProdListener.onDeliveryNotAvailable(it.msg)
                    }
                }
            }catch (e: ApiException){
                discProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                discProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

     fun getDiscProductUsingDiscontValue(discount:Int){

        Coroutines.main{
            try {
                withContext(Dispatchers.IO){
                    prodDetailRepo.clearDiscountedProductTable()
                }
//                discProdListener.onApiCallStarted()
                val prodResponse = prodDetailRepo.getSimilarDiscountedProducts(discount)
                prodResponse.let {
                    if(it.status == 1){
                        discProdListener.onSuccess(it.product)
                        prodDetailRepo.insertAllDiscProducts(it.product?:ArrayList())
                    }else{
                        discProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                discProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                discProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getApplyCouponData(userID:Int, prodID: String){
        Coroutines.main{
            try {
                prodDetailRepo.clearCouponTable()
                val prodResponse = prodDetailRepo.getApplyCouponData(userID, prodID)
                prodResponse.let {
                    if(it.status == 1){
                        prodDetailRepo.insertCouponDat(it.coupon?:ArrayList())
                        couponCanApplyData.value = it.coupan_can_apply?:ArrayList()
                    }else if(it.status == 2){
                        discProdListener.onInvalidCredential()
                    }else{
                        discProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                discProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                discProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getFreeDeliveryData(){
        Coroutines.main{
            try {
                val prodBenefitResp = prodDetailRepo.getFreeDeliveryData()
                prodBenefitResp.let {
                    if(it.status == 1){
                        discProdListener.onSuccessFreeDeliveryData(it.freedelivery?:ArrayList())
                    }else{
                        discProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                discProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                discProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun rateProductWithoutImage(userID:Int, token:String, prodID:Int, name:String,
                                email:String, rating:Float, image:String, message:String){
        Coroutines.main{
            try {
                rateProdListener.onApiCallStarted()
                val prodResponse = prodDetailRepo.ratingProductCallToServerWithoutImage(userID, token,
                    prodID, name, email, rating,image, message)
                prodResponse.let {
                    if(it.status == 1){
                        rateProdListener.onSuccess(it.msg!!)
                    }else if(it.status == 2){
                        rateProdListener.onInvalidCredential()
                    }else{
                        rateProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                rateProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                rateProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun rateProduct(userID: RequestBody, token: RequestBody, prodID: RequestBody, name: RequestBody,
                                email: RequestBody, rating: RequestBody, image: MultipartBody.Part, message: RequestBody){
        Coroutines.main{
            try {
                rateProdListener.onApiCallStarted()
                val prodResponse = prodDetailRepo.ratingProductCallToServer(userID, token, prodID,
                    name, email, rating,image, message)
                prodResponse.let {
                    if(it.status == 1){
                        rateProdListener.onSuccess(it.msg!!)
                    }else if(it.status == 2){
                        rateProdListener.onInvalidCredential()
                    }else{
                        rateProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                rateProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                rateProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun bulkEnquiryProduct(name:String, phone:String, prodName:String,
                           email:String, quantity:Int){
        Coroutines.main{
            try {
                rateProdListener.onApiCallStarted()
                val prodResponse = prodDetailRepo.productBulkEnquiry(name, phone, prodName, email, quantity)
                prodResponse.let {
                    if(it.status == 1){
                        rateProdListener.onSuccess(it.msg!!)
                    }else{
                        rateProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                rateProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                rateProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getProductCouponData(prodID:String){
        Coroutines.main{
            try {
                discProdListener.onApiCallStarted()
                val prodResponse = prodDetailRepo.getProductCouponData(prodID)
                prodResponse.let {
                    if(it.status == 1){
                        discProdListener.onCouponDataSuccess(it.productcoupon?:ArrayList())
                    }else{
                        discProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                discProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                discProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun clearProductTable(){
        viewModelScope.launch(Dispatchers.IO) {
            prodDetailRepo.clearProductDetailTable()
        }
    }

    fun getProductDetailData(prodID:String){
        Coroutines.main{
            try {
                discProdListener.onApiCallStarted()
                val prodResponse = prodDetailRepo.productDetailDataFromServer(prodID)
                prodResponse.let {
                    if(it.status == 1){
                        discProdListener.onSuccessProductData(it)
                        val prodEntityObj = it.product
                        prodEntityObj.return_days = it.return_days
                        Log.e("returnDaysProduct", " :: ${it.return_days}")
                        val prodImageListToString = Gson().toJson(it.productImg)
                        prodEntityObj.prodImageListString  = prodImageListToString
                        prodDetailRepo.insertProductDataIntoDB(prodEntityObj)
                    }else{
                        discProdListener.onProductDetailFailure(it.msg)
                    }
                }
            }catch (e: ApiException){
                discProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                discProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getRatingSingleData(prodID:String){
        Coroutines.main{
            try {
                discProdListener.onApiCallStarted()
                prodDetailRepo.clearRatingReviewsTable()
                val prodResponse = prodDetailRepo.getProductReviewData(prodID)
                prodResponse.let {
                    if(it.status == 1){
                        discProdListener.onProductRatingData(it)
                    }else{
                        discProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                discProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                discProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getRatingReviewsData(prodID:String){
        Coroutines.main{
            try {
//                prodDetailRepo.clearRatingReviewsTable()
//                ratingViewListener.onApiCallStarted()
                val prodResponse = prodDetailRepo.getRatingProdListingData(prodID)
                prodResponse.let {
                    if(it.status == 1){
                        ratingViewListener.onSuccess(it)
                    }else{
                        ratingViewListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                ratingViewListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                ratingViewListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun addItemsToCart(userID: Int, token: String, prodID: String?,  prodQty: Int?, updateProd:Int) {
        Coroutines.main{
            try {
                discProdListener.onApiCallStarted()
                val prodResponse = prodDetailRepo.addProductToCart(userID, token, prodID, prodQty, updateProd)
                prodResponse.let {
                    if(it.status == 1){
                        discProdListener.onCartItemAdded(it.msg!!)
                        getCartData(userID, token)
                    }else if(it.status == 2){
                        discProdListener.onInvalidCredential()
                    }else{
                        discProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                discProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                discProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun updateCartItem(userID: Int, token: String, prodID: String,  prodQty: Int, updateProd:Int) {
        Coroutines.main{
            try {
                cartRespListener.onUpdateOrDeleteCartStart()
                val prodResponse = prodDetailRepo.addProductToCart(userID, token, prodID, prodQty,updateProd)
                prodResponse.let {
                    if(it.status == 1){
                        cartRespListener.onCartProductItemAdded("Cart updated successfully")
//                        getCartData(userID, token)
                    }else if(it.status == 2){
                        cartRespListener.onInvalidCredential()
                    }else{
                        cartRespListener.onFailureUpdateORDeleteCart(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                cartRespListener.onApiFailureUpdateORDeleteCart(e.message!!)
            }catch (e: NoInternetException){
                cartRespListener.onNoInternetAvailableUpdateORDeleteCart(e.message!!)
            }
        }
    }

    fun getCartData(userID: Int, token: String) {
        Coroutines.main{
            try {
                discProdListener.onApiCallStarted()
                val prodResponse = prodDetailRepo.getCartData(userID, token)
                prodResponse.let {
                    if(it.status == 1){
                        discProdListener.onCartItemAdded(it.msg!!)
                        if(it.cartdata != null){
                            prodDetailRepo.insertCartList(it.cartdata!!)
                        }
                    }else if(it.status == 2){
                        discProdListener.onInvalidCredential()
                    }else{
                        discProdListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                discProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                discProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun insertFavourites(favItems:Wishlist) {
        Coroutines.main{
            prodDetailRepo.insertFavourites(favItems)
        }
    }
}