package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.notebook.android.data.db.entities.*

@Dao
interface HomeDao {

    //(1). Banner Function here....
    @Query("SELECT * FROM banner")
    fun getAllBanner(): LiveData<List<Banner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBanner(allBanner : List<Banner>)

    @Query("DELETE FROM banner")
    suspend fun clearBannerTable()


    //(2). All Sub Category Function here...
    @Query("SELECT * FROM bestsellerhome")
    fun getBestSellelHome(): LiveData<List<BestSellerHome>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBestSellerHome(allBestSeller : List<BestSellerHome>)

    @Query("DELETE FROM bestsellerhome")
    suspend fun clearBestSellerHome()


    //(3). All Sub Category Function here...
    @Query("SELECT * FROM latestproducthome")
    fun getAllLatestProductHome(): LiveData<List<LatestProductHome>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLatestProductHome(allLatestProduct : List<LatestProductHome>)

    @Query("DELETE FROM latestproducthome")
    suspend fun clearLatestProductHome()

    //(4). All Brand Function here...
    @Query("SELECT * FROM brand")
    fun getAllBrands(): LiveData<List<Brand>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBrands(brands : List<Brand>)

    @Query("DELETE FROM brand")
    suspend fun clearBrandData()


    //(5). All Latest Offer Function here...
    @Query("SELECT * FROM latestoffer")
    fun getAllLatestOffers(): LiveData<List<LatestOffer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLatestOffers(latestOffers : List<LatestOffer>)

    @Query("DELETE FROM latestoffer")
    suspend fun clearLatestOfferHome()


    //(6). All Latest Offer Function here...
    @Query("SELECT * FROM merchantbanner")
    fun getAllMerchantBanner(): LiveData<List<MerchantBanner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMerchantBanner(latestOffers : List<MerchantBanner>)

    @Query("DELETE FROM merchantbanner")
    suspend fun clearMerchantBannerHome()
}