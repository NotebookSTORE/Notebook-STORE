package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "RatingReviews", indices = [Index(value = ["id"], unique = true)])
data class RatingReviews(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var user_id:Int,
    var rating:Float,
    var email:String,
    var name:String,
    var image:String?=null,
    var message:String ?= null,
    var product_id:Int,
    var status:Int,
    var currentdate:String?=null
) {
}