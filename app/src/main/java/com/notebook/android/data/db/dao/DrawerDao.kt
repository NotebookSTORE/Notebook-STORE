package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notebook.android.data.db.entities.Category
import com.notebook.android.data.db.entities.DrawerCategory
import com.notebook.android.data.db.entities.DrawerSubCategory
import com.notebook.android.data.db.entities.FaqExpandable

@Dao
interface DrawerDao { //(1). All Category Function here...

    //Airport Data Table operation here....
    @Query("SELECT * FROM drawercategory")
    fun getDrawerCategoryData(): LiveData<List<DrawerCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDrawerCategoryData(allCategory : List<DrawerCategory>)

    @Query("DELETE FROM drawercategory")
    suspend fun clearCategoryTable()


    //(2). Faq query function here..

    //Airport Data Table operation here....
    @Query("SELECT * FROM faqexpandable")
    fun getAllFaqData(): LiveData<List<FaqExpandable>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFaqData(faqData : List<FaqExpandable>)

    @Query("DELETE FROM faqexpandable")
    suspend fun clearFaqTableData()

}