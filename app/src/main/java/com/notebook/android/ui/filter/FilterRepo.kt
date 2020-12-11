package com.notebook.android.ui.filter

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.filter.FilterByData

class FilterRepo(
    val notebookApi: NotebookApi,
    val db:NotebookDatabase
) : SafeApiRequest(){

    suspend fun getFilter(filterDataFromPage:String, parameter:Int) : FilterByData{
        return apiRequest { notebookApi.filterByData(filterDataFromPage, parameter) }
    }

    suspend fun insertBrandFilter(brandFilter:List<BrandFilterBy>) = db.getFilterDao().insertAllBrandFilter(brandFilter)
    suspend fun insertColorFilter(colorFilter:List<ColorFilterBy>) = db.getFilterDao().insertColorFilter(colorFilter)
    suspend fun insertDiscountFilter(discountFilter:List<DiscountFilterBy>) = db.getFilterDao().insertAllDiscountFilter(discountFilter)
    suspend fun insertRatingFilter(ratingFilter:List<RatingFilterBy>) = db.getFilterDao().insertAllRatingFilter(ratingFilter)
    suspend fun insertPriceFilter(priceFilter:List<PriceFilterBy>) = db.getFilterDao().insertAllPriceFilter(priceFilter)
    suspend fun insertCouponFilter(couponFilter:List<CouponFilterBy>) = db.getFilterDao().insertAllCouponFilter(couponFilter)


    fun getBrandFilter() = db.getFilterDao().getAllBrandFilter()
    fun getColorFilter() = db.getFilterDao().getAllColorFilter()
    fun getRatingFilter() = db.getFilterDao().getAllRatingFilter()
    fun getDiscountFilter() = db.getFilterDao().getAllDiscountFilter()
    fun getPriceFilter() = db.getFilterDao().getAllPriceFilter()
    fun getCouponFilter() = db.getFilterDao().getAllCouponFilter()
}