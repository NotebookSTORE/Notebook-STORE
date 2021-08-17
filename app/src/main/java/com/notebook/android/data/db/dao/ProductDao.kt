package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notebook.android.data.db.entities.*

@Dao
interface ProductDao {

    //(1). All Latest Product Function here...
    @Query("SELECT * FROM latestproduct")
    fun getAllLatestProducts(): LiveData<List<LatestProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLatestProducts(latest : List<LatestProduct>)


    //(2). All Best Seller Product Function here...
    @Query("SELECT * FROM bestseller")
    fun getAllBestSellerProducts(): LiveData<List<BestSeller>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBestSellerProducts(bestSeller : List<BestSeller>)


    //(3). All Home SubSub Category Product Function here...
    @Query("SELECT * FROM homesubsubcategoryproduct")
    fun getAllHomeSSCategoryProducts(): LiveData<List<HomeSubSubCategoryProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHomeSSCategoryProducts(bestSeller : List<HomeSubSubCategoryProduct>)

    @Query("DELETE FROM homesubsubcategoryproduct")
    suspend fun clearHomeSSCategoryTable()


    //(4). All category Product Function here...
    @Query("SELECT * FROM categoryproduct")
    fun getAllHomeCategoryProducts(): LiveData<List<CategoryProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHomeCategoryProducts(bestSeller : List<CategoryProduct>)

    @Query("DELETE FROM categoryproduct")
    suspend fun clearCategoryTable()


    //(5).Product detail data Function here...
    @Query("SELECT * FROM productdetailentity")
    fun getProductDetailData(): LiveData<ProductDetailEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductDetailData(prodData : ProductDetailEntity)

    @Query("DELETE FROM productdetailentity")
    suspend fun clearProductDetailData()

    @Query("DELETE FROM productdetailentity WHERE id = :id")
    suspend fun deleteProductById(id:Int)
}