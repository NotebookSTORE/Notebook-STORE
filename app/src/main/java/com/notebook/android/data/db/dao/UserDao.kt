package com.notebook.android.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.notebook.android.data.db.entities.User

@Dao
interface UserDao {

    //Airport Data Table operation here....
    @Query("SELECT * FROM user")
    fun getUser(): LiveData<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user : User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM user")
    suspend fun deleteUser()

    @Query("UPDATE user SET profile_image = :profileImage ,dob= :dateTime,name= :username WHERE id LIKE :userID ")
    suspend fun updateItem(userID: Int, profileImage: String, dateTime: String, username: String?): Int

    /*@Query("SELECT * FROM user WHERE uid = $CURRENT_USER_ID")
    fun getUsers() : LiveData<User>*/
}