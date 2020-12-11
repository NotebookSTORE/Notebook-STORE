package com.notebook.android.ui.filter

import androidx.lifecycle.ViewModel
import com.max.ecomaxgo.maxpe.view.flight.utility.Coroutines
import com.notebook.android.utility.ApiException
import com.notebook.android.utility.NoInternetException

class FilterByVM(
    val filterRepo: FilterRepo
) : ViewModel() {

    var filterResponseListener:FilterResponseListener ?= null

    fun getBrandFilterFromDB() = filterRepo.getBrandFilter()
    fun getColorFilterFromDB() = filterRepo.getColorFilter()
    fun getDiscountFilterFromDB() = filterRepo.getDiscountFilter()
    fun getRatingFilterFromDB() = filterRepo.getRatingFilter()
    fun getPriceFilterFromDB() = filterRepo.getPriceFilter()
    fun getCouponFilterFromDB() = filterRepo.getCouponFilter()

    /*fun setFilterRawData(filterData: FilterRequestRawData){
        filterRawData.value = filterData
    }*/

    fun getFilterData(){
        Coroutines.main{
            try {

               /* val filterResponse = filterRepo.getFilter()
                filterResponse.let {
                    if(it.status == 1){
                        filterRepo.insertBrandFilter(it.brand!!)
                        filterRepo.insertColorFilter(it.color!!)
                        filterRepo.insertDiscountFilter(it.discount!!)
                        filterRepo.insertPriceFilter(it.price!!)
                        filterRepo.insertRatingFilter(it.rating!!)
                    }else{
                        filterResponseListener?.onFailure(it.msg!!)
                    }
                }*/
            }catch (e: ApiException){
                filterResponseListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                filterResponseListener?.onFailure(e.message!!)
            }
        }
    }
}