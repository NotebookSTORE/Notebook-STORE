package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notebook.android.data.db.entities.MerchantBenefit

@Dao
interface MerchantDao {

    @Query("SELECT * FROM merchantbenefit")
    fun getAllMerchantBenefit(): LiveData<List<MerchantBenefit>>

    @Query("SELECT * FROM merchantbenefit WHERE merchantType = :merchantTypes")
    fun getAllMerchantBenefitAccToType(merchantTypes:String): LiveData<List<MerchantBenefit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMerchantBenefit(merchBenefitData : List<MerchantBenefit>)

    @Query("DELETE FROM merchantbenefit")
    suspend fun clearMerchantBenefitTable()
}