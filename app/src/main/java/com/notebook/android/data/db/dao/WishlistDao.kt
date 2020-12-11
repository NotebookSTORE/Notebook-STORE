package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notebook.android.data.db.entities.Wishlist

@Dao
interface WishlistDao {

    @Query("SELECT * FROM wishlist")
    fun getAllWishlistProducts(): LiveData<List<Wishlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWishlistProducts(bestSeller : List<Wishlist>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavProduct(favProduct : Wishlist) /*: LiveData<Long>*/

    @Query("Delete FROM wishlist WHERE favId LIKE  :favID")
    suspend fun deleteFavById(favID:Int)

    @Query("DELETE FROM wishlist")
    suspend fun clearWishlistTable()
}