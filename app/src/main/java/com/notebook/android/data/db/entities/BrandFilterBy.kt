package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "BrandFilterBy", indices = [Index(value = ["id"], unique = true)])
data class BrandFilterBy(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var title:String ?= null,
    var image:String ?= null,
    var isBrandSelected:Boolean = false
) {
}