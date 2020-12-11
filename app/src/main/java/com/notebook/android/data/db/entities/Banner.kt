package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val BANNER_ID = 0

@Entity(tableName = "Banner", indices = [Index(value = ["url"], unique = true)])
data class Banner(
    var image: String? = null,
    var url: String? = null,
    var banner_use_for:Int ?= null,
    var product_id:String ?= null
) {
    @PrimaryKey(autoGenerate = true)
    var bannerID:Int = BANNER_ID
}