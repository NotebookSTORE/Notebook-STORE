package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "MerchantBenefit", indices = [Index(value = ["id"], unique = true)])
data class MerchantBenefit(
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var title:String ?=null,
    var description: String ?=null,
    var image:String ?=null,
    var merchantType:String
)