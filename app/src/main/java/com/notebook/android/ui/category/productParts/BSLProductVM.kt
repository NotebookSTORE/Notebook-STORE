package com.notebook.android.ui.category.productParts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.model.filter.FilterRequestData
import com.notebook.android.ui.category.productParts.responseListener.BSProductListener
import com.notebook.android.ui.category.productParts.responseListener.CategoryProductListener
import com.notebook.android.ui.category.productParts.responseListener.LatestProductListener
import com.notebook.android.ui.category.productParts.responseListener.SSCategoryProductListener
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BSLProductVM(
    val bslProductRepo:BSLProductRepo
) : ViewModel() {

    var bsProductListener:BSProductListener ?= null
    var latestProductListener:LatestProductListener ?= null
    var categProductListener:CategoryProductListener ?= null
    var ssCategProductListener:SSCategoryProductListener ?= null

    fun getUserData() = bslProductRepo.getUser()
    fun getBSProductFromDB() = bslProductRepo.getAllBestSellerProducts()
    fun getLatestProductFromDB() = bslProductRepo.getAllLatestProducts()
    fun getAllSSCategoryProdFromDB() = bslProductRepo.getAllSSCategoryProduct()

    fun getBestSellerProducts(){
        Coroutines.main{
            try {
                bsProductListener?.onApiCallStarted()
                val prodResponse = bslProductRepo.bestSellerViewAllProducts(0)
                prodResponse.let {
                    if(it.status == 1){
                        bsProductListener?.onSuccess(it.bestseller)
                        bslProductRepo.insertBSProductIntoDB(it.bestseller?:ArrayList())
                    }else{
                        bsProductListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                bsProductListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                bsProductListener?.onFailure(e.message!!)
            }
        }
    }

    fun getLatestProducts(){
        Coroutines.main{
            try {

                latestProductListener?.onApiCallStarted()
                val prodResponse = bslProductRepo.latestViewAllProducts(0)
                prodResponse.let {
                    if(it.status == 1){
                        latestProductListener?.onSuccess(it.latestproduct)

                        bslProductRepo.insertLatestProductIntoDB(it.latestproduct?:ArrayList())
                    }else{
                        latestProductListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                latestProductListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                latestProductListener?.onFailure(e.message!!)
            }
        }
    }

    fun getProductFilterByWise(filterData: FilterRequestData){
       /* Coroutines.main {
            try {
                bsProductListener.onApiCallStarted()
                val bdResponse = subCategRepo.getProductAccToFilterByData(filterData)
                bdResponse.let {
                    if(it.status == 1){
                        bsProductListener.onGetSubCategoryBannerData(it.banner?:ArrayList())
                        bsProductListener.onSuccess(it.product)
                    }else{
                        bsProductListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                bsProductListener.onFailure(e.message?:"")
            }catch (e:NoInternetException){
                bsProductListener.onNoInternetAvailable(e.message?:"")
            }
        }*/
    }

    fun getFilterData(filterDataFromPage:String, parameter:Int){
        Coroutines.main{
            try {
                val filterResponse = bslProductRepo.getFilter(filterDataFromPage, parameter)
                filterResponse.let {
                    if(it.status == 1){
                        viewModelScope.launch(Dispatchers.IO){
                            bslProductRepo.clearBrandFilterTable()
                            bslProductRepo.clearColorFilterTable()
                            bslProductRepo.clearDiscountFilterTable()
                            bslProductRepo.clearPriceFilterTable()
                            bslProductRepo.clearCouponFilterTable()
                            bslProductRepo.clearRatingFilterTable()

                            bslProductRepo.insertBrandFilter(it.brand?:ArrayList())
                            bslProductRepo.insertColorFilter(it.color?:ArrayList())
                            bslProductRepo.insertDiscountFilter(it.discount?:ArrayList())
                            bslProductRepo.insertPriceFilter(it.price?:ArrayList())
                            bslProductRepo.insertRatingFilter(it.rating?:ArrayList())
                            bslProductRepo.insertCouponFilter(it.coupons?:ArrayList())
                        }
                    }else{
                        bsProductListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                bsProductListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                bsProductListener?.onFailure(e.message!!)
            }
        }
    }

    fun addItemsToCart(userID: Int, token: String, prodID: Int?,  prodQty: Int?, updateProd:Int) {
        Coroutines.main{
            try {

                val prodResponse = bslProductRepo.addProductToCart(userID, token, prodID, prodQty, updateProd)
                prodResponse.let {
                    if(it.status == 1){
//                        dashboardApiListener?.onCartItemAdded("Item added successfully ")
                        getCartData(userID, token)
                    }else{
                        bsProductListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                bsProductListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                bsProductListener?.onFailure(e.message!!)
            }
        }
    }

    fun getCartData(userID: Int, token: String) {
        Coroutines.main{
            try {

                val prodResponse = bslProductRepo.getCartData(userID, token)
                prodResponse.let {
                    if(it.status == 1){
//                        bsProductListener?.onSuccess(it.cartdata)
                            bslProductRepo.insertCartList(it.cartdata!!)
                    }else{
                        bsProductListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                bsProductListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                bsProductListener?.onFailure(e.message!!)
            }
        }
    }

    fun getHomeSSCategoryProducts(ssCategID:Int){
        Coroutines.main{
            try {

                ssCategProductListener?.onApiCallStarted()
                bslProductRepo.clearHomeSSCategoryTableData()
                val prodResponse = bslProductRepo.getProductSSCategoryWise(ssCategID)
                prodResponse.let {
                    if(it.status == 1){
                        ssCategProductListener?.onSuccess(it.product)
                        bslProductRepo.insertSSCategoryProductIntoDB(it.product?:ArrayList())
                    }else{
                        ssCategProductListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                ssCategProductListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                ssCategProductListener?.onFailure(e.message!!)
            }
        }
    }
}