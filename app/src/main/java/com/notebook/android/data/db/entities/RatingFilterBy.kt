package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "RatingFilterBy", indices = [Index(value = ["id"], unique = true)])
data class RatingFilterBy(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var title:String ?= null,
    var ratingvalue:String ?= null,
    var isRatingSelected:Boolean = false
) {
}