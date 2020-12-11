package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.notebook.android.data.db.entities.OrderHistory

@Dao
interface OrderHistoryDao {

    @Query("SELECT * FROM orderhistory")
    fun getAllOrderHistory(): LiveData<List<OrderHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOrderHistory(orderHistoryData: List<OrderHistory>)

    @Query("DELETE FROM orderhistory")
    suspend fun clearOrderHistoryTable()
}