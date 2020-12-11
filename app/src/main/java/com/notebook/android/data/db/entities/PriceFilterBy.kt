package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "PriceFilterBy", indices = [Index(value = ["id"], unique = true)])
data class PriceFilterBy(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var price1:String ?= null,
    var price2:String ?= null
) {
}