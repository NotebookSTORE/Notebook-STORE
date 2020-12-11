package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "MerchantBanner", indices = [Index(value = ["id"], unique = true)])
data class MerchantBanner(
    @PrimaryKey(autoGenerate = false)
    var id:Int ?= null,
    var title: String? = null,
    var description: String? = null,
    var image: String? = null,
    var status: Int? = null
) {
}