package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notebook.android.data.db.entities.*

@Dao
interface FilterDao {

    //(1). All Brand FilterBy Function here...
    @Query("SELECT * FROM brandfilterby")
    fun getAllBrandFilter(): LiveData<List<BrandFilterBy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBrandFilter(brandFilter : List<BrandFilterBy>)

    @Query("DELETE FROM brandfilterby")
    suspend fun clearBrandFilterByTable()


    //(2). All Discount FilterBy Function here...
    @Query("SELECT * FROM discountfilterby")
    fun getAllDiscountFilter(): LiveData<List<DiscountFilterBy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDiscountFilter(discFilter : List<DiscountFilterBy>)

    @Query("DELETE FROM discountfilterby")
    suspend fun clearDiscountFilterByTable()


    //(3). All Color FilterBy Function here...
    @Query("SELECT * FROM colorfilterby")
    fun getAllColorFilter(): LiveData<List<ColorFilterBy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColorFilter(colorFilter : List<ColorFilterBy>)

    @Query("DELETE FROM colorfilterby")
    suspend fun clearColorFilterByTable()


    //(4). All Rating filter by Function here...
    @Query("SELECT * FROM ratingfilterby")
    fun getAllRatingFilter(): LiveData<List<RatingFilterBy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRatingFilter(ratingFilter : List<RatingFilterBy>)

    @Query("DELETE FROM ratingfilterby")
    suspend fun clearRatingFilterByTable()


    //(5). All Price filter by Function here...
    @Query("SELECT * FROM pricefilterby")
    fun getAllPriceFilter(): LiveData<List<PriceFilterBy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPriceFilter(priceFilter : List<PriceFilterBy>)

    @Query("DELETE FROM pricefilterby")
    suspend fun clearPriceFilterByTable()


    //(6). All Coupon FilterBy Function here...
    @Query("SELECT * FROM couponfilterby")
    fun getAllCouponFilter(): LiveData<List<CouponFilterBy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCouponFilter(couponFilter : List<CouponFilterBy>)

    @Query("DELETE FROM couponfilterby")
    suspend fun clearCouponFilterByTable()
}