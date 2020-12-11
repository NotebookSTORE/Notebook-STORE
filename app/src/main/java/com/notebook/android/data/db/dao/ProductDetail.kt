package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.notebook.android.data.db.entities.Cart
import com.notebook.android.data.db.entities.CouponApply
import com.notebook.android.data.db.entities.DiscountedProduct
import com.notebook.android.data.db.entities.RatingReviews


@Dao
interface ProductDetail {

    //(1). All Latest Offer Function here...

    //Airport Data Table operation here....
    @Query("SELECT * FROM discountedproduct")
    fun getAllDiscountedProducts(): LiveData<List<DiscountedProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDiscProducts(latestOffers : List<DiscountedProduct>)

    @Query("DELETE FROM discountedproduct")
    suspend fun clearDiscountedProductTable()



    //(2). All Cart Data Function here...

    //Airport Data Table operation here....
    @Query("SELECT * FROM cart")
    fun getAllCartProduct(): LiveData<List<Cart>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCartProduct(cartData : List<Cart>)

    @Query("DELETE FROM cart WHERE cartproduct_id LIKE  :cartID")
    suspend fun deleteCartItem(cartID:Int)

    @Query("DELETE FROM cart")
    suspend fun clearCartTable()

    //(3). All Cart Data Function here...

    //Airport Data Table operation here....
    @Query("SELECT * FROM ratingreviews")
    fun getAllRatingReviewsData(): LiveData<List<RatingReviews>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRatingReviewsData(ratingData : List<RatingReviews>)

    @Query("DELETE FROM ratingreviews")
    suspend fun clearRatingReviewsTable()


    //(4). All Apply coupon Function here...
    @Query("SELECT * FROM couponapply")
    fun getAllCouponData(): LiveData<List<CouponApply>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCouponData(latestOffers : List<CouponApply>)

    @Query("DELETE FROM couponapply")
    suspend fun clearCouponTable()
}