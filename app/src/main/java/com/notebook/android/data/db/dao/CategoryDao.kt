package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notebook.android.data.db.entities.*

@Dao
interface CategoryDao {

    //(1). All Category Function here...
    @Query("SELECT * FROM category")
    fun getAllCategory(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategory(allCategory : List<Category>)

    @Query("DELETE FROM category")
    suspend fun clearCategoryTable()


    //(2). All Sub Category Function here...
    @Query("SELECT * FROM subcategory")
    fun getAllSubCategory(): LiveData<List<SubCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSubCategory(allSubCategory : List<SubCategory>)

    @Query("DELETE FROM subcategory")
    suspend fun clearSubCategoryTable()


    //(3). All Sub Category Function here...
    @Query("SELECT * FROM subsubcategory")
    fun getAllSubSubCategory(): LiveData<List<SubSubCategory>>

    @Query("SELECT * FROM subsubcategory WHERE category_id=:categId AND subcategory_id=:subCategID")
    fun getAllSubSubCategoryAccToID(categId:Int, subCategID:Int): LiveData<List<SubSubCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSubSubCategory(allSubCategory : List<SubSubCategory>)

    @Query("DELETE FROM subsubcategory")
    suspend fun clearSubSubCategoryTable()


    //(4). All Sub Category Product data Function here...
    @Query("SELECT * FROM subcategoryproduct")
    fun getAllSubCategoryProduct(): LiveData<List<SubCategoryProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSubCategoryProduct(allSubCategoryProd : List<SubCategoryProduct>)

    @Query("DELETE FROM subcategoryproduct")
    suspend fun clearSubCategoryProductTable()


    //(5). All Filter Common Product Function here...
    @Query("SELECT * FROM filterproduct")
    fun getAllFilterCommonProduct(): LiveData<List<FilterProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFilterCommonProduct(allFilterProd : List<FilterProduct>)

    @Query("DELETE FROM filterproduct")
    suspend fun clearFilterCommonProductTable()


    //(6). All search product Function here...
    @Query("SELECT * FROM searchproduct")
    fun getAllSearchProduct(): LiveData<List<SearchProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSearchProduct(searchProd : List<SearchProduct>)

    @Query("DELETE FROM searchproduct")
    suspend fun clearSearchProductTable()
}