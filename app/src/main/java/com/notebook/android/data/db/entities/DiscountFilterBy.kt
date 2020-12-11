package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "DiscountFilterBy", indices = [Index(value = ["id"], unique = true)])
data class DiscountFilterBy(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var discount:Int ?= null,
    var isDiscountSelected:Boolean = false
) {
}