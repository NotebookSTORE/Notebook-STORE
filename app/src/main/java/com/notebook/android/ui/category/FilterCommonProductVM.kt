package com.notebook.android.ui.category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.data.db.entities.FilterProduct
import com.notebook.android.model.filter.FilterRequestData
import com.notebook.android.model.filter.PaginationData
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilterCommonProductVM(
    val filterProdRepo:FilterCommonProductRepository
) : ViewModel() {

    lateinit var filterProdListener:FilterCommonProductListener
    fun getUserData() = filterProdRepo.getUser()
    val getFilterCommonProdDataFromDB = MutableLiveData<List<FilterProduct>>()
    val getPageData = MutableLiveData<PaginationData>()

    fun deleteUser(){
        viewModelScope.launch(Dispatchers.IO) {
            filterProdRepo.deleteUser()
        }
    }

    fun clearCartTableFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            filterProdRepo.clearCartTable()
            filterProdRepo.clearAddressTable()
            filterProdRepo.clearOrderTable()
            filterProdRepo.clearFavouriteTable()
        }
    }

    fun getProductFilterByWise(filterData:FilterRequestData,pageNo:Int){
        Coroutines.main {
            try {
                filterProdListener.onApiCallStarted()
                val bdResponse = filterProdRepo.productAccordingToFilterByData(filterData,pageNo)
                bdResponse.let {
                    if(it.status == 1){
                        if(it.product?.data?.size!! > 0){
                            filterProdListener.onSuccess(true)
                        }else{
                            filterProdListener.onSuccess(false)
                        }
//                        filterProdRepo.clearFilterCommonProductTable()
//                        filterProdRepo.insertAllFilterCommonProduct(it.product?:ArrayList())
                        getFilterCommonProdDataFromDB.postValue(it.product?.data?:ArrayList())
                        getPageData.postValue(PaginationData.create(it.product))
                        if(it.banner?.isNotEmpty() == true){
                            filterProdListener.onGetBannerImageData(it.banner?.get(0)?.image?:"")
                        }else{
                            filterProdListener.onGetBannerImageData("")
                        }
                    }else if(it.status == 2){
                        filterProdListener.onInvalidCredential()
                    }else{
                        filterProdRepo.clearFilterCommonProductTable()
                        if(it.banner?.isNotEmpty() == true){
                            filterProdListener.onGetBannerImageData(it.banner?.get(0)?.image?:"")
                        }
                        filterProdListener.onSuccess(false)
                    }
                }
            }catch (e:Exception){
                filterProdListener.onApiFailure(e.message?:"")
            }catch (e:NoInternetException){
                filterProdListener.onNoInternetAvailable(e.message?:"")
            }
        }
    }

    fun addItemsToCart(userID: Int, token: String, prodID: Int?,  prodQty: Int?, updateProd:Int) {
        Coroutines.main{
            try {
                filterProdListener.onApiCartCallStarted()
                val prodResponse = filterProdRepo.addProductToCart(userID, token, prodID, prodQty, updateProd)
                prodResponse.let {
                    if(it.status == 1){
                        filterProdListener.onCartItemAdded("Item added successfully ")
                        getCartData(userID, token)
                    }else if(it.status == 2){
                    filterProdListener.onInvalidCredential()
                }else if(it.status == 2){
                        filterProdListener.onInvalidCredential()
                    }else{
                        filterProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                filterProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                filterProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getCartData(userID: Int, token: String) {
        Coroutines.main{
            try {
                val prodResponse = filterProdRepo.getCartData(userID, token)
                prodResponse.let {
                    if(it.status == 1){
                        if(it.cartdata != null){
                            filterProdRepo.insertCartList(it.cartdata!!)
                        }
                    }else if(it.status == 2){
                        filterProdListener.onInvalidCredential()
                    }else{
                        filterProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                filterProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                filterProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }

    fun getFilterData(filterDataFromPage:String, parameter:Int){
        Coroutines.main{
            try {
                val filterResponse = filterProdRepo.getFilter(filterDataFromPage, parameter)
                filterResponse.let {
                    if(it.status == 1){
                        viewModelScope.launch(Dispatchers.IO){
                            filterProdRepo.clearBrandFilterTable()
                            filterProdRepo.clearColorFilterTable()
                            filterProdRepo.clearDiscountFilterTable()
                            filterProdRepo.clearPriceFilterTable()
                            filterProdRepo.clearCouponFilterTable()
                            filterProdRepo.clearRatingFilterTable()

                            filterProdRepo.insertBrandFilter(it.brand?:ArrayList())
                            filterProdRepo.insertColorFilter(it.color?:ArrayList())
                            filterProdRepo.insertDiscountFilter(it.discount?:ArrayList())
                            filterProdRepo.insertPriceFilter(it.price?:ArrayList())
                            filterProdRepo.insertRatingFilter(it.rating?:ArrayList())
                            filterProdRepo.insertCouponFilter(it.coupons?:ArrayList())
                        }
                    }else{
                        filterProdListener.onFailure(it.msg?:"")
                    }
                }
            }catch (e: ApiException){
                filterProdListener.onApiFailure(e.message!!)
            }catch (e: NoInternetException){
                filterProdListener.onNoInternetAvailable(e.message!!)
            }
        }
    }
}