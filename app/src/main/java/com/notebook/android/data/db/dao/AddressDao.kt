package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.Banner
import com.notebook.android.data.db.entities.Country

@Dao
interface AddressDao {

    //(1). Country Function here....
    @Query("SELECT * FROM country")
    fun getAllCountryData(): LiveData<List<Country>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCountryData(allBanner : List<Country>)

    @Query("DELETE FROM country")
    suspend fun clearCountryTable()


    //(2). Address Function here....
    @Query("SELECT * FROM address")
    fun getAllAddressData(): LiveData<List<Address>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAddressData(addrList : List<Address>)

    @Query("DELETE FROM address WHERE id LIKE  :addrID")
    suspend fun deleteAddress(addrID:Int)

    @Query("DELETE FROM address")
    suspend fun clearAddressTable()
}