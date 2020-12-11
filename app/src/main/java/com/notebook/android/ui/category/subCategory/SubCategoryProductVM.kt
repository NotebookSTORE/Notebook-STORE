package com.notebook.android.ui.category.subCategory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.model.filter.FilterRequestData
import com.notebook.android.ui.dashboard.frag.fragHome.subCa.SubCategProdResponseListener
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubCategoryProductVM(
    val subCategRepo:SubCategProdRepo
) : ViewModel() {

    lateinit var subCategResponseListener:SubCategProdResponseListener
//    fun getallSubCategoryFromDB() = subCategRepo.getAllSubCategoryProduct()
    fun getAllCategoryProdFromDB() = subCategRepo.getAllCategoryProduct()
    fun getUserData() = subCategRepo.getUser()

   /* fun getSubCategoryProductData(subCategID:Int){
        Coroutines.main{
            try {
                subCategResponseListener.onApiCallStarted()
                *//*viewModelScope.launch(Dispatchers.IO){
                    subCategRepo.clearSubCategTable()
                }*//*
                val prodResponse = subCategRepo.subCategoryProductAccToID(subCategID)
                prodResponse.let {
                    if(it.status == 1){
                        subCategResponseListener.onGetSubCategoryBannerData(it.subcategory?:ArrayList())
                        subCategResponseListener.onSuccess(it.product)
//                        subCategRepo.insertSubCategoryProdcutIntoDB(it.product?:ArrayList())
//                        getFilterData()
                    }else{
                        subCategResponseListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                subCategResponseListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                subCategResponseListener.onFailure(e.message!!)
            }
        }
    }

    fun getCategoryWiseProducts(categID:Int){
        Coroutines.main{
            try {
                subCategResponseListener.onApiCallStarted()
                subCategRepo.clearCategoryTableData()

                val prodResponse = subCategRepo.getProductCategoryWise(categID)
                prodResponse.let {
                    if(it.status == 1){
                        subCategResponseListener.onSuccessCategory(it.product)
                        subCategResponseListener.onGetCategoryBannerData(it.banner?:ArrayList())
                        subCategRepo.insertCategoryProductIntoDB(it.product?:ArrayList())
                    }else{
                        subCategResponseListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                subCategResponseListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                subCategResponseListener.onFailure(e.message!!)
            }
        }
    }*/

    fun getProductFilterByWise(filterData:FilterRequestData){
       /* Coroutines.main {
            try {
                subCategResponseListener.onApiCallStarted()
                val bdResponse = subCategRepo.getProductAccToFilterByData(filterData)
                bdResponse.let {
                    if(it.status == 1){
                        subCategResponseListener.onGetSubCategoryBannerData(it.banner?:ArrayList())
                        subCategResponseListener.onSuccess(it.product)
                    }else{
                        subCategResponseListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e:ApiException){
                subCategResponseListener.onFailure(e.message?:"")
            }catch (e:NoInternetException){
                subCategResponseListener.onNoInternetAvailable(e.message?:"")
            }
        }*/
    }

    fun addItemsToCart(userID: Int, token: String, prodID: Int?,  prodQty: Int?, updateProd:Int) {
        Coroutines.main{
            try {

                val prodResponse = subCategRepo.addProductToCart(userID, token, prodID, prodQty, updateProd)
                prodResponse.let {
                    if(it.status == 1){
//                        dashboardApiListener??.onCartItemAdded("Item added successfully ")
                        getCartData(userID, token)
                    }else{
                        subCategResponseListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                subCategResponseListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                subCategResponseListener.onFailure(e.message!!)
            }
        }
    }

    fun getCartData(userID: Int, token: String) {
        Coroutines.main{
            try {

                val prodResponse = subCategRepo.getCartData(userID, token)
                prodResponse.let {
                    if(it.status == 1){
//                        cartRespListener?.onSuccess(it.cartdata)
                        if(it.cartdata != null){
                            subCategRepo.insertCartList(it.cartdata!!)
                        }
                    }else{
//                        dashboardApiListener?.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                subCategResponseListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                subCategResponseListener.onFailure(e.message!!)
            }
        }
    }

    fun getFilterData(filterDataFromPage:String, parameter:Int){
        Coroutines.main{
            try {
                val filterResponse = subCategRepo.getFilter(filterDataFromPage, parameter)
                filterResponse.let {
                    if(it.status == 1){
                        viewModelScope.launch(Dispatchers.IO){
                            subCategRepo.clearBrandFilterTable()
                            subCategRepo.clearColorFilterTable()
                            subCategRepo.clearDiscountFilterTable()
                            subCategRepo.clearPriceFilterTable()
                            subCategRepo.clearCouponFilterTable()
                            subCategRepo.clearRatingFilterTable()

                            subCategRepo.insertBrandFilter(it.brand?:ArrayList())
                            subCategRepo.insertColorFilter(it.color?:ArrayList())
                            subCategRepo.insertDiscountFilter(it.discount?:ArrayList())
                            subCategRepo.insertPriceFilter(it.price?:ArrayList())
                            subCategRepo.insertRatingFilter(it.rating?:ArrayList())
                            subCategRepo.insertCouponFilter(it.coupons?:ArrayList())
                        }
                    }else{
                        subCategResponseListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                subCategResponseListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                subCategResponseListener.onFailure(e.message!!)
            }
        }
    }

    /*fun getSortingSubCategoryData(subCategID:Int, sortValue:Int){
        Coroutines.main{
            try {
                subCategResponseListener.onApiCallStarted()
                val prodResponse = subCategRepo.sortSubCategoryProductAccToID(subCategID, sortValue)
                prodResponse.let {
                    if(it.status == 1){
                        subCategResponseListener.onGetSubCategoryBannerData(it.subcategory?:ArrayList())
                        subCategResponseListener.onSuccess(it.product)
//                        subCategRepo.insertSubCategoryProdcutIntoDB(it.product?:ArrayList())
                    }else{
                        subCategResponseListener.onFailure(it.msg!!)
                    }
                }
            }catch (e: ApiException){
                subCategResponseListener.onFailure(e.message!!)
            }catch (e: NoInternetException){
                subCategResponseListener.onFailure(e.message!!)
            }
        }
    }*/
}